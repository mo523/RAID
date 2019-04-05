package RAID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client
{
	private static Socket socket;
	private static Scanner kb = new Scanner(System.in);
	private static PrintWriter out;
	private static BufferedReader in;

	public static void main(String[] args) throws UnknownHostException, IOException
	{
		connectToRS();
		menu();

		kb.close();
	}

	private static void menu() throws IOException
	{
		int choice;
		do
		{
			print("What would you like to do?\n1. View all files\n2. Add a file\n3. Get a file\n4. Remove a file");
			choice = choiceValidator(1, 4);
			switch (choice)
			{
				case 1:
					getAllFiles();
					break;
				default:
					break;
			}
		} while (choice != 0);
	}

	private static void getAllFiles() throws IOException
	{
		out.println("1");
		out.println("$$$");
		ArrayList<String> files = new ArrayList<>();
		do
		{
			files.add(in.readLine());
		} while (!files.get(files.size() - 1).equals("$$$"));
		files.remove(files.size() - 1);
		files.forEach(System.out::println);
	}

	private static void connectToRS() throws UnknownHostException, IOException
	{
		print("Welcome to The RAID System");
		print("What is the IP address for the RAID Server?");
		String ip = kb.nextLine();
		if (ip.charAt(0) == 'l')
			ip = "127.0.0.1";
		else if (ip.charAt(0) == 'm')
			ip = "www.moshehirsch.com";
		socket = new Socket(ip, 436);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		print("Connected to the RAID Server: " + socket.getInetAddress());
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

	private static void print(Object o)
	{
		System.out.println(o);
	}
}
