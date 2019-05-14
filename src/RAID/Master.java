package RAID;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;

public class Master extends Thread
{
	private volatile HashSet<ConnectedSlave> slaves;
	private volatile static RAID_Server RAID;

	public Master(RAID_Server RAID)
	{
		this.RAID = RAID;
		slaves = new HashSet<>();
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
				ServerSocket ss = new ServerSocket(345);
				ConnectedSlave ps = new ConnectedSlave(ss.accept(), this);
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

	public void addFile(File file)
	{
		synchronized (slaves)
		{
			for (ConnectedSlave cs : slaves)
				cs.sendFile(file);
		}
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
}
