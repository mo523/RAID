package RAID;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
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
				System.out.println("\n\tClient connected " + ps);
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
						System.out.println("\tClient disconnected " + ps);
						synchronized (clients)
						{
							try
							{
								ps.close();
							}
							catch (IOException e1)
							{
							}
							clients.remove(ps);
						}
					}
					catch (ClassNotFoundException e)
					{
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

	/**
	 * This method returns an ArrayList of Strings of all the MetaFiles or an
	 * ArryList containing one String: "No files..."
	 * 
	 * @return
	 */
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

	public boolean addFile(String fileName, String name, byte[] data) throws IOException
	{
		// Passes file to Master
		if (master.addFile(fileName, name, data))
		{
			modCount++;
			return true;
		}
		else
			return false;
	}
	
	public boolean addEncryptedFile(String fileName, String name, byte[] data, String password) throws IOException, NoSuchAlgorithmException
	{
		// Passes file to Master
		if (master.addEncryptedFile(fileName, name, data,password))
		{
			modCount++;
			return true;
		}
		else
			return false;
	}

	public int getModCount()
	{
		return modCount;
	}

	public byte[] getFile(String fileName) throws IOException
	{
		return master.getFile(fileName);
	}

	public void delFile(String fileName) throws IOException
	{
		master.delFile(fileName);
	}

	public void shutdown()
	{
		// TODO Auto-generated method stub

	}
}
