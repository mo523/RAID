package RAID;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;

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

	public void addFile(MetaFile file, byte[] data)
	{
		synchronized (slaves)
		{
			for (ConnectedSlave cs : slaves)
				cs.sendFile(file, data);
		}
		files.put(file.getFileName(), file);
	}

	public void broadcastMessage(String msg)
	{
		for (ConnectedSlave s : slaves)
			s.sendMessage(msg);
	}

	public void checkForDisconnect()
	{
		HashSet<ConnectedSlave> disconnected = new HashSet<>();
		synchronized (slaves)
		{
			for (ConnectedSlave s : slaves)
			{
				if (s.disconnected())
				{
					System.out.println(s.toString() + " is disconnected");
					disconnected.add(s);
				}
			}
			slaves.removeAll(disconnected);
		}
	}

	public int getSlaveCount()
	{
		checkForDisconnect();
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
			ConnectedSlave cs = slaves.iterator().next();
			return cs.getFile(fileName);
		}
	}

	public void delFile(String fileName)
	{
		synchronized (slaves)
		{
			for (ConnectedSlave cs : slaves)
				cs.delFile(fileName);
		}
		files.remove(fileName);
	}
}
