package RAID;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;

public class Server extends Thread
{

	private HashSet<ConnectedClient> clients;
	private volatile static RAID_Server RAID;

	public Server(RAID_Server RAID)
	{
		this.RAID = Server.RAID;
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

	public StringBuilder getFileInfo()
	{

		StringBuilder sb = new StringBuilder();
		if (RAID.getFiles().size() == 0)
			sb.append("No files...");
		else
			for (File f : RAID.getFiles())
				sb.append(f + "\r\n");
		return sb;
	}

	public void addFile(File file)
	{
		//Passes file up to RAID_Server to handle
		RAID.addFile(file);
	}
}
