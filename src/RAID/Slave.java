package RAID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Slave
{
	private static Socket socket;
	private static Scanner kb;
	private static BufferedReader in;
	private static PrintWriter out;

	public static void main(String[] args) throws IOException
	{
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
			else
				System.out.println(data);
		}
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
