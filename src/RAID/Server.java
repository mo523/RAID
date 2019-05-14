package RAID;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;

public class Server extends Thread
{

	private HashSet<ConnectedClient> clients;
	private volatile RAID_Server RAID;

	public Server(RAID_Server RAID)
	{
		this.RAID = RAID;
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

		ArrayList<String> files = new ArrayList<>();  
		if (RAID.getFiles().size() == 0)
			files.add("No files...");
		else
			for (File f : RAID.getFiles())
				files.add(f.toString());
		return files;
	}

	public void addFile(File file)
	{
		//Passes file up to RAID_Server to handle
		RAID.addFile(file);
	}
}
