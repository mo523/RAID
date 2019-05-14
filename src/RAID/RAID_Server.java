package RAID;

import java.util.HashSet;
import java.util.Scanner;

public class RAID_Server
{
	private static Scanner kb;
	private static Master master;
	private static Server server;
	private static volatile HashSet<File> files = new HashSet<>();

	public static void main(String[] args)
	{
		files.add(new File("Test1", "Today1", "Moshe1", 0, 1));
		files.add(new File("Test2", "Today2", "Moshe2", 0, 1));
		print("RAID Server initializing...");
		kb = new Scanner(System.in);
		master = new Master(files);
		server = new Server(files);
		master.start();
		server.start();
		new Thread(() -> checkDisconnects()).start();
		menu();
		kb.close();
	}

	private static void menu()
	{
		int choice;
		do
		{
			print("\nMain menu\n0. Shutdown RAID Server\n1. View stats\n2. Broadcast a message");
			choice = choiceValidator(0, 3);
			switch (choice)
			{
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

	private static void print(Object o)
	{
		System.out.println(o.toString());
	}
}
