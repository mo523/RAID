package RAID;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Master extends Thread
{
	private volatile HashSet<ConnectedSlave> slaves;
	private volatile HashMap<String, MetaFile> files;

	public Master(HashMap<String, MetaFile> files)
	{
		slaves = new HashSet<>();
		this.files = files;
	}

	public void run()
	{
		listenForConnections();
	}

	public void listenForConnections()
	{
		while (true)
		{
			try
			{
				ServerSocket ss = new ServerSocket(536);
				ConnectedSlave ps = new ConnectedSlave(ss.accept());
				System.out.println("\tSlave connected;\n\t\t: " + ps);
				synchronized (slaves)
				{
					slaves.add(ps);
				}
				ss.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void addFile(String fileName, String name, byte[] data) throws IOException
	{
		checkForDisconnect();
		MetaFile file;
		synchronized (slaves)
		{
			int padding = 0;
			if (slaves.size() < 3)
			{
				file = new MetaFile(fileName, new Date().toString().substring(0, 16), name, -1, slaves.size(), padding,
						data.length + padding);
				System.out.println("Not enough slaves for parity backup, sending complete file to all slaves");
				MetaFile nFile = file.getNextMetaFile();
				for (ConnectedSlave cs : slaves)
				{
					cs.sendFile(nFile, data);
					nFile = nFile.getNextMetaFile();
				}
			}
			else
			{
				if (data.length % (slaves.size() - 1) != 0)
					padding = (slaves.size() - 1) - (data.length) % (slaves.size() - 1);
				file = new MetaFile(fileName, new Date().toString().substring(0, 16), name, -1, slaves.size(), padding,
						data.length + padding);
				PriorityQueue<ConnectedSlave> pq = getSlavePQ();
				ConnectedSlave currSlave = pq.poll();
				MetaFile nfile = file.getNextMetaFile();
				byte[][] splitData = currSlave.splitFile(nfile, data);
				for (int i = 0; i < splitData.length; i++)
				{
					currSlave = pq.poll();
					nfile = nfile.getNextMetaFile();
					currSlave.sendFile(nfile, splitData[i]);
				}
			}
		}
		files.put(file.getFileName(), file);

	}

	public void broadcastMessage(String msg)
	{
		for (ConnectedSlave s : slaves)
			s.sendMessage(msg);
	}

	public void checkForDisconnect() throws IOException
	{
		HashSet<ConnectedSlave> disconnected = new HashSet<>();
		synchronized (slaves)
		{
			for (ConnectedSlave s : slaves)
			{
				if (s.disconnected())
				{
					System.out.println("\t\t" + s.toString() + " is disconnected");
					disconnected.add(s);
				}
			}
			slaves.removeAll(disconnected);

			// Recover lost files
			if (!disconnected.isEmpty())
			{
				PriorityQueue<ConnectedSlave> pq = getSlavePQ();
				ConnectedSlave currSlave = pq.poll();
				for (MetaFile mf : files.values())
				{
					HashMap<Integer, byte[]> parts = new HashMap<>();
					for (ConnectedSlave cs : pq)
						cs.getFile(parts, mf.getFileName());
					byte[] data = currSlave.recoverFile(mf, parts);
					addFile(mf.getFileName(), mf.getAddedBy(), data);
				}
			}
		}
	}

	public int getSlaveCount()
	{
		return slaves.size();
	}

	public HashSet<ConnectedSlave> getSockets()
	{
		return slaves;
	}

	public byte[] getFile(String fileName) throws IOException
	{
		synchronized (slaves)
		{
			checkForDisconnect();
			HashMap<Integer, byte[]> parts = new HashMap<>();
			PriorityQueue<ConnectedSlave> pq = getSlavePQ();
			ConnectedSlave currSlave = pq.remove();
			MetaFile file = files.get(fileName);
			for (ConnectedSlave cs : pq)
				cs.getFile(parts, file.getFileName());
			return currSlave.buildFile(file, parts);
		}
	}

	public void delFile(String fileName) throws IOException
	{
		synchronized (slaves)
		{
			for (ConnectedSlave cs : slaves)
				cs.delFile(fileName);
		}
		files.remove(fileName);
	}

	private PriorityQueue<ConnectedSlave> getSlavePQ() throws IOException
	{
		PriorityQueue<ConnectedSlave> pq = new PriorityQueue<ConnectedSlave>();
		for (ConnectedSlave cs : slaves)
			cs.updateSpecs();
		for (ConnectedSlave cs : slaves)
			pq.add(cs);
		return pq;
	}

}
