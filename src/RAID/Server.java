package RAID;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Server extends Thread
{

	private HashSet<ConnectedClient> clients;
	private volatile HashMap<String, MetaFile> files;
	private volatile Master master;
	private volatile int modCount = 0;

	public Server(HashMap<String, MetaFile> files, Master master)
	{
		this.master = master;
		this.files = files;
		clients = new HashSet<>();
	}

	public void run()
	{
		new Thread(() -> listenForConnections()).start();
	}

	public void listenForConnections()
	{
		while (true)
		{
			try
			{
				ServerSocket ss = new ServerSocket(436);
				ConnectedClient ps = new ConnectedClient(ss.accept(), this);
				System.out.println("\tClient connected;\n\t\t" + ps);
				synchronized (clients)
				{
					clients.add(ps);
				}
				new Thread(() -> {
					try
					{
						ps.startListening();
					}
					catch (IOException e)
					{
						System.out.println("\tClient disconnected;\n\t\t" + ps);
						synchronized (clients)
						{
							clients.remove(ps);
						}
					}
				}).start();
				ss.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public int getClientCount()
	{
		return clients.size();
	}

	public ArrayList<String> getFileInfo()
	{
		ArrayList<String> fileInfo = new ArrayList<>();
		if (files.size() == 0)
			fileInfo.add("No files...");
		else
			for (MetaFile f : files.values())
				fileInfo.add(f.toString());
		return fileInfo;
	}

	public void addFile(MetaFile file, byte[] data)
	{
		modCount++;
		// Passes file to Master
		master.addFile(file, data);
	}

	public int getModCount()
	{
		return modCount;
	}

	public byte[] getFile(String fileName) throws IOException
	{
		return master.getFile(fileName);
	}

	public void delFile(String fileName)
	{
		master.delFile(fileName);
	}
}
