package RAID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Client
{
	private static Socket socket;
	private static Scanner kb;
	private static PrintWriter out;
	private static BufferedReader in;

	public static void main(String[] args) throws IOException, InterruptedException
	{
		kb = new Scanner(System.in);
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
				case 2:
					addFile();
					break;
				default:
					break;
			}
		} while (choice != 0);
	}

	private static void addFile()
	{
		String filePath;
		// String fileName;
		// String fileDate;
		// String fileAuth;
		byte[] fileContent = null;
		do
		{
			do
			{
				print("File location?");
				filePath = kb.nextLine();
				if (!exists(filePath))
					System.out.println("Error! File does not exist");
			} while (!exists(filePath));
			try
			{
				fileContent = Files.readAllBytes(Path.of(filePath));
			}
			catch (IOException e)
			{
				System.out.println("File too big...");
			}
		} while (fileContent == null);
		out.println("2");
		out.println(filePath);
		out.println(fileContent.length);
		for (int i = 0; i < fileContent.length; i++)
			out.println(fileContent[i]);
	}

	public static boolean exists(String filePath)
	{
		return Files.exists(Path.of(filePath));
	}

	private static void getAllFiles() throws IOException
	{
		out.println("1");
		System.out.println(getData());
	}

	private static StringBuilder getData() throws IOException
	{
		String data;
		StringBuilder sb = new StringBuilder();
		do
		{
			data = in.readLine();
			if (!data.equals(""))
				sb.append(data + "\r\n");
		} while (!data.equals(""));
		return sb;
	}

	private static void connectToRS() throws IOException
	{
		print("Welcome to The RAID Client System");
		print("What is the IP address for the RAID Server? (l == localhost, m == moshehirsch.com)");
		String ip = kb.nextLine();
		if (ip.charAt(0) == 'l')
			ip = "127.0.0.1";
		else if (ip.charAt(0) == 'm')
			ip = "www.moshehirsch.com";
		print("What is your name?");
		String name = kb.nextLine();
		socket = new Socket(ip, 436);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out.println(name);
		print("Connected to the RAID Server: " + socket.getInetAddress() + " ," + socket.getLocalPort());
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
