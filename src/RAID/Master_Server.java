package RAID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Master_Server
{

	private static Scanner kb = new Scanner(System.in);
	private static volatile HashSet<File> files = new HashSet<>();
	private static volatile boolean exit = false;
	private static volatile Master_Server ms;
	private static volatile Master master;
	private static volatile Server server;

	public static void main(String[] args) throws IOException
	{
		print("RAID Server initializing...\n\n");
		ms = new Master_Server();
		master = ms.new Master();
		server = ms.new Server();
		server.start();
		master.start();
		menu();
		kb.close();
	}

	private static void print(Object o)
	{
		System.out.println(o);
	}

	private static void menu()
	{
		int choice;
		do
		{
			print("Main menu\n0. Shutdown RAID Server\n1. View stats");
			choice = choiceValidator(0, 2);
			switch (choice)
			{
				case 1:
					print(files.size() + " files are being stored");
					print("1 client is connected");
					print(master.getSlaveCount() + " slaves are connected");
					break;
				default:
					break;
			}
		} while (choice != 0);
		exit = true;
	}

	private static int choiceValidator(int low, int high)
	{
		int choice;
		do
		{
			choice = kb.nextInt();
			if (choice < low || choice > high)
				System.out.println("\nInvalid choice!\n" + low + " - " + high);
		} while (choice < low || choice > high);
		kb.nextLine();
		return choice;
	}

	private class Master extends Thread
	{
		private HashSet<String> slaves = new HashSet<>();

		public int getSlaveCount()
		{
			return slaves.size();
		}

	}

	private class Server extends Thread
	{
		private Socket socket;
		private ServerSocket listener;
		private PrintWriter out;
		private BufferedReader in;

		public void run()
		{
			files.add(new File("test1", "10/23/1910", "Moshe"));
			files.add(new File("test2", "09/17/2001", "Moshe"));
			try
			{

				listener = new ServerSocket(436);
				socket = listener.accept();
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				System.out.println("Server connected to client @ " + socket.getInetAddress().getHostAddress());
				listen();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		private void listen() throws IOException
		{
			while (!exit)
			{
				ArrayList<String> data = new ArrayList<>();
				int choice = Integer.parseInt(in.readLine());
				do
				{
					data.add(in.readLine());
				} while (!data.get(data.size() - 1).equals("$$$"));
				data.remove(data.size() - 1);
				send(choice, data);
			}
		}

		private void send(int choice, ArrayList<String> data)
		{
			switch (choice)
			{
				case 1:	// View list of files
					files.stream().forEachOrdered(out::println);
					break;
				case 2:	// Add file
					break;
				case 3:	// Get file
					break;
				case 4:	// Remove file
					break;
				default:
					out.println("ERROR!");
					break;
			}
			out.println("$$$");
		}
	}

}
