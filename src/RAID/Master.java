package RAID;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;

public class Master extends Thread
{
	private volatile HashSet<PrioritySlave> slaves = new HashSet<>();

	public void run()
	{
		while (true)
		{
			try
			{
				ServerSocket ss = new ServerSocket(345);
				PrioritySlave ps = new PrioritySlave(ss.accept());
				System.out.println("Connected: " + ps);
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

	public void broadcastMessage(String msg)
	{
		for (PrioritySlave s : slaves)
			s.sendMessage(msg);
	}

	public void checkForDisconnect()
	{
		HashSet<PrioritySlave> disconnected = new HashSet<>();
		synchronized (slaves)
		{
			for (PrioritySlave s : slaves)
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
		return slaves.size();
	}

	public HashSet<PrioritySlave> getSockets()
	{
		return slaves;
	}
}
