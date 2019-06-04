package RAID;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The {@code Client} class is a standalone class used for the client side of
 * the RAID system. The class is completely static since it contains its own
 * {@code main} method and should not be instantiated in another class.
 * <p>
 * {@code Client} works by connecting to the {@link RAID_Server} through the
 * {@link ConnectedClient} class. See the {@link #connectToRS()} method to see
 * how the connection is established.
 * 
 * @see RAID_Server
 * @see ConnectedClient
 * 
 * @author Moshe Hirsch
 *
 */

public class Client
{
	private static Socket socket;
	private static Scanner kb;
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	private static int modCount = -1;
	private static ArrayList<String> files = new ArrayList<>();

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException
	{
		kb = new Scanner(System.in);
		connectToRS();
		menu();
		kb.close();
	}

	private static void menu() throws IOException, ClassNotFoundException
	{
		int choice;
		do
		{
			print("\n\nWhat would you like to do?\n0. Exit\n1. View all files\n2. Add a file\n3. Get a file\n4. Remove a file");
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

	@SuppressWarnings("unchecked")
	private static ArrayList<String> getAllFileInfo() throws IOException, ClassNotFoundException
	{
		out.writeObject(ClientChoice.SendInfo);
		out.writeInt(modCount);
		out.flush();
		int serverModCount = in.readInt();
		if (modCount != serverModCount)
		{
			modCount = serverModCount;
			files = (ArrayList<String>) in.readObject();
		}
		return files;
	}

	private static void getFile() throws IOException, ClassNotFoundException
	{
		ArrayList<String> files = getAllFileInfo();
		if (files.size() == 1 && files.get(0).equals("No files..."))
			System.out.println(files.get(0));
		else
		{
			System.out.println("Which file would you like to get?");
			for (int i = 0; i < files.size(); i++)
				System.out.println((i + 1) + ". " + files.get(i));
			int choice = choiceValidator(1, files.size()) - 1;
			out.writeObject(ClientChoice.SendFile);
			String fileName = files.get(choice);
			fileName = fileName.substring(6, fileName.length());
			fileName = fileName.split(",")[0];
			out.writeUTF(fileName);
			out.flush();
			byte[] data = new byte[in.readInt()];
			in.readFully(data);
			saveFile(fileName, data);
			System.out.println("File received and saved to disk");
		}
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

	private static void delFile() throws IOException, ClassNotFoundException
	{
		ArrayList<String> files = getAllFileInfo();
		System.out.println("Which file would you like to delete?");
		for (int i = 0; i < files.size(); i++)
			System.out.println((i + 1) + ". " + files.get(i));
		int choice = choiceValidator(1, files.size()) - 1;
		out.writeObject(ClientChoice.DelFile);
		String fileName = files.get(choice).split("\\t+")[0];
		out.writeUTF(fileName);
		out.flush();
	}

	private static void addFile() throws IOException
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
		out.writeObject(ClientChoice.GetFile);
		String fileNames[] = filePath.split("[\\\\/]");
		out.writeUTF(fileNames[fileNames.length - 1]);
		out.writeInt(fileContent.length);
		out.flush();
		out.write(fileContent);
		out.flush();
		if (in.readBoolean())
			System.out.println("File succesfully sent");
		else
			System.out.println("Sorry, there was a problem. Please try again later");
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
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		out.writeUTF(name);
		out.flush();
		print("Connected to the RAID Server: " + socket.getInetAddress() + ", " + socket.getLocalPort());
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
