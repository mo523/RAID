package RAID;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

public class RAID_Server
{
	private static Scanner kb;
	private static volatile Master master;
	private static volatile Server server;
	private static volatile HashMap<String, MetaFile> files;

	public static void main(String[] args)
	{
		print("RAID Server initializing...\n");
		if (checkForBind())
			print("ERROR! Ports already in use");
		else
		{
			printIP();
			kb = new Scanner(System.in);
			files = new HashMap<>();
			master = new Master(files);
			server = new Server(files, master);
			master.start();
			server.start();
			new Thread(() -> checkDisconnects()).start();
			menu();
			kb.close();
		}
		print("\nShutting down server...");
		System.exit(0);	// Needed until we get a better handle on the threads
	}

	private static void testing()
	{
		// Test File, data says Hello World
		MetaFile file = new MetaFile("Hello.txt", "Test date", "Moshe", 0, 1);
		byte[] data = new byte[] { 72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100 };
		server.addFile(file, data);
	}

	private static void menu()
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

	private static void broadcastMenu()
	{
		System.out.println("What would you like to broadcast?");
		String msg = kb.nextLine();
		master.broadcastMessage(msg);
		System.out.println("Broadcasted: " + msg);
	}

	private static void checkDisconnects()
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
