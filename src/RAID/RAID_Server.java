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
		print("RAID Server initializing...\n\n");
		kb = new Scanner(System.in);
		master = new Master();
		server = new Server(files);
		master.start();
		server.start();
		menu();
		kb.close();
	}

	private static void menu()
	{
		int choice;
		do
		{
			print("\nMain menu\n0. Shutdown RAID Server\n1. View stats");
			choice = choiceValidator(0, 3);
			switch (choice)
			{
				case 1:
					checkDisconnects();
					print(files.size() + " files are being stored");
					print("1 client is connected");
					print(master.getSlaveCount() + " slaves are connected");
					break;
				case 2:
					broadcastMessage("Hello");
					break;
				default:
					break;
			}
		} while (choice != 0);
	}

	private static void checkDisconnects()
	{
		master.checkForDisconnect();
	}

	private static void broadcastMessage(String msg)
	{
		master.broadcastMessage(msg);
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
		System.out.println(o);
	}
}
