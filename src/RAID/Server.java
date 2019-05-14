package RAID;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;

public class Server extends Thread
{

	private volatile HashSet<File> files;
	private HashSet<ConnectedClient> clients;

	public Server(HashSet<File> files)
	{
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
				ConnectedClient ps = new ConnectedClient(ss.accept(), files);
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
}
