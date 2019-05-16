package RAID;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Client
{
	private static Socket socket;
	private static Scanner kb;
	private static PrintWriter out;
	private static BufferedReader in;
	private static int modCount = -1;
	private static ArrayList<String> files = new ArrayList<>();

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
			print("What would you like to do?\n0. Exit\n1. View all files\n2. Add a file\n3. Get a file\n4. Remove a file");
			choice = choiceValidator(0, 4);
			switch (choice)
			{
				case 0:
					break;
				case 1:
					getAllFileInfo().forEach(System.out::println);
					break;
				case 2:
					addFile();
					break;
				case 3:
					getFile();
					break;
				case 4:
					delFile();
					break;
				default:
					break;
			}
		} while (choice != 0);
	}

	private static ArrayList<String> getAllFileInfo() throws IOException
	{
		out.println("1");
		out.println(modCount);
		int serverModCount = Integer.parseInt(in.readLine());
		if (modCount != serverModCount)
		{
			modCount = serverModCount;
			files = new ArrayList<>();
			do
				files.add(in.readLine());
			while (in.ready());
		}
		return files;
	}

	private static void getFile() throws IOException
	{
		ArrayList<String> files = getAllFileInfo();
		System.out.println("Which file would you like to get?");
		for (int i = 0; i < files.size(); i++)
			System.out.println((i + 1) + ". " + files.get(i));
		int choice = choiceValidator(1, files.size()) - 1;
		out.println("3");
		String fileName = files.get(choice).split("\\t+")[0];
		out.println(fileName);
		byte[] data = new byte[Integer.parseInt(in.readLine())];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte) Byte.parseByte(in.readLine());
		saveFile(fileName, data);
	}

	private static void saveFile(String fileName, byte[] data)
	{
		try (FileOutputStream fos = new FileOutputStream(fileName))
		{
			fos.write(data);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void delFile() throws IOException
	{
		ArrayList<String> files = getAllFileInfo();
		System.out.println("Which file would you like to delete?");
		for (int i = 0; i < files.size(); i++)
			System.out.println((i + 1) + ". " + files.get(i));
		int choice = choiceValidator(1, files.size()) - 1;
		out.println("4");
		String fileName = files.get(choice).split("\\t+")[0];
		out.println(fileName);
	}

	private static void addFile()
	{
		byte[] fileContent = null;
		String filePath;
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
				fileContent = Files.readAllBytes(Paths.get(filePath));
			}
			catch (IOException e)
			{
				System.out.println("File too big...");
			}
		} while (fileContent == null);
		out.println("2");
		String fileNames[] = filePath.split("[\\\\/]");
		out.println(fileNames[fileNames.length - 1]);
		out.println(fileContent.length);
		for (int i = 0; i < fileContent.length; i++)
			out.println(fileContent[i]);
	}

	public static boolean exists(String filePath)
	{
		return Files.exists(Paths.get(filePath));
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
