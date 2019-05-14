package RAID;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;

public class RAID_Server
{
	private static Scanner kb;
	private static volatile RAID_Server RAID;

	private volatile Master master;
	private volatile Server server;
	private volatile HashSet<File> files;

	public static void main(String[] args)
	{
		print("RAID Server initializing...\n");
		if (checkForBind())
			print("ERROR! Ports already in use");
		else
		{
			RAID = new RAID_Server();
			printIP();
			kb = new Scanner(System.in);
			RAID.menu();
			kb.close();
		}
		print("\nShutting down server...");
		System.exit(0);	// Needed until we get a better handle on the threads
	}

	private RAID_Server()
	{
		files = new HashSet<>();
		master = new Master(this);
		server = new Server(this);
		master.start();
		server.start();
		new Thread(() -> checkDisconnects()).start();
	}

	private void testing()
	{
		 //Test File, data says Hello World
		 File file = new File("Test", "Today", "Moshe", 0, 1);
		 file.setData(new byte[] { 72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100
		 });
		 files.add(file);
		 master.addFile(file);
	}

	public HashSet<File> getFiles()
	{
		return files;
	}

	public void addFile(File file)
	{
		// get file from server <-- connected client <-- client
		// and send it it to master --> connected slave --> slave
		master.addFile(file);
	}

	private void menu()
	{
		int choice;
		do
		{
			print("\nMain menu\n0. Shutdown RAID Server\n1. View stats\n2. Broadcast a message");
			choice = choiceValidator(-1, 3);
			switch (choice)
			{
				case -1:
					testing();
					break;
				case 1:
					master.checkForDisconnect();
					print(files.size() + " files are being stored");
					print(server.getClientCount() + " clients are connected");
					print(master.getSlaveCount() + " slaves are connected");
					break;
				case 2:
					broadcastMenu();
					break;
				default:
					break;
			}
		} while (choice != 0);
	}

	private void broadcastMenu()
	{
		System.out.println("What would you like to broadcast?");
		String msg = kb.nextLine();
		master.broadcastMessage(msg);
		System.out.println("Broadcasted: " + msg);
	}

	private void checkDisconnects()
	{
		while (true)
		{
			try
			{
				Thread.sleep(30000);
			}
			catch (InterruptedException e)
			{
			}
			if (master.getSlaveCount() > 0)
			{
				print("Heartbeating...");
				master.checkForDisconnect();
			}
		}
	}

	private static int choiceValidator(int low, int high)
	{
		int choice;
		do
		{
			try
			{
				choice = kb.nextInt();
				if (choice < low || choice > high)
					System.out.println("\nInvalid choice!\n" + low + " - " + high);
			}
			catch (Exception e)
			{
				System.out.println("ERROR! bad input");
				choice = low - 1;
			}

		} while (choice < low || choice > high);
		kb.nextLine();
		return choice;
	}

	private static void printIP()
	{
		try
		{
			print("Local IP address: " + Inet4Address.getLocalHost().getHostAddress());
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}

	private static boolean checkForBind()
	{
		try (ServerSocket ss1 = new ServerSocket(436); ServerSocket ss2 = new ServerSocket(345))
		{
		}
		catch (IOException e)
		{
			return true;
		}
		return false;
	}

	private static void print(Object o)
	{
		System.out.println(o.toString());
	}
}
