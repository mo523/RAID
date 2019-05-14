package RAID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

public class Slave
{
	private static Socket socket;
	private static Scanner kb;
	private static BufferedReader in;
	private static PrintWriter out;
	private static HashSet<File> files;

	public static void main(String[] args) throws IOException
	{
		files = new HashSet<>();
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
				out.println("alive");
			else if (data.equals("File"))
				ReceiveFile();
			else
				System.out.println(data);
		}
	}

	private static void ReceiveFile() throws IOException
	{
		String addedBy = in.readLine();
		String dateAdded = in.readLine();
		String fileName = in.readLine();
		int partNumber = Integer.parseInt(in.readLine());
		int partsAmount = Integer.parseInt(in.readLine());
		File file = new File(fileName, dateAdded, addedBy, partNumber, partsAmount);
		byte[] data = new byte[Integer.parseInt(in.readLine())];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte) Byte.parseByte(in.readLine());
		file.setData(data);
		files.add(file);
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
		socket = new Socket(ip, 345);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		print("Connected to the RAID Server: " + socket.getInetAddress() + " ," + socket.getLocalPort());
	}

	private static void print(Object o)
	{
		System.out.println(o);
	}

}
