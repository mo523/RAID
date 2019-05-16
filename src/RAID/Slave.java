package RAID;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

public class Slave
{
	private static Socket socket;
	private static Scanner kb;
	private static BufferedReader in;
	private static PrintWriter out;
	private static HashMap<String, MetaFile> metaFiles;

	public static void main(String[] args) throws IOException
	{
		metaFiles = new HashMap<>();
		kb = new Scanner(System.in);
		connectToRS();
		listen();
		kb.close();
	}

	private static void listen() throws IOException
	{
		while (true)
		{
			String data = in.readLine();
			if (data.equals("heartbeat"))
				heartbeat();
			else if (data.equals("putFile"))
				receiveFile();
			else if (data.equals("getFile"))
				sendFile();
			else if (data.equals("delFile"))
				delFile();
			else
				System.out.println(data);
		}
	}

	private static void heartbeat()
	{
		System.out.println("Master requesting a heartbeat");
		out.println("alive");
	}

	private static void receiveFile() throws IOException
	{
		String addedBy = in.readLine();
		String dateAdded = in.readLine();
		String fileName = in.readLine();
		int partNumber = Integer.parseInt(in.readLine());
		int partsAmount = Integer.parseInt(in.readLine());
		MetaFile file = new MetaFile(fileName, dateAdded, addedBy, partNumber, partsAmount);
		byte[] data = new byte[Integer.parseInt(in.readLine())];
		for (int i = 0; i < data.length; i++)
			data[i] = Byte.parseByte(in.readLine());
		metaFiles.put(fileName, file);
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(data);
		fos.close();
		System.out.println("Succesfully received file: " + fileName);
	}

	private static void sendFile() throws IOException
	{
		String fileName = in.readLine();
		byte[] data  = Files.readAllBytes(Paths.get(fileName));
		out.println(data.length);
		for (byte b : data)
			out.println(b);
		System.out.println("Master requesting file: " + fileName);
	}

	private static void delFile() throws IOException
	{
		String fileName = in.readLine();
		Files.delete(Paths.get(fileName));
		metaFiles.remove(fileName);
		System.out.println("Master requesting deletion of: " + fileName);
	}

	private static void connectToRS() throws IOException
	{
		print("Welcome to The RAID Slave System");
		print("What is the IP address for the RAID Server? (l == localhost, m == moshehirsch.com)");
		String ip = kb.nextLine();
		if (ip.charAt(0) == 'l')
			ip = "127.0.0.1";
		else if (ip.charAt(0) == 'm')
			ip = "www.moshehirsch.com";
		socket = new Socket(ip, 536);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		print("Connected to the RAID Server: " + socket.getInetAddress() + ", " + socket.getLocalPort());
	}

	private static void print(Object o)
	{
		System.out.println(o);
	}
}
