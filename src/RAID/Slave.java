package RAID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Slave
{
	private static Socket socket;
	private static BufferedReader in;
	private static PrintWriter out;

	public static void main(String[] args)
	{
		socket = connect();
		try
		{
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			while (true)
			{
				String data = in.readLine();
				if (data.equals("heartbeat"))
					out.println("alive");
				else
					System.out.println(data);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static Socket connect()
	{
		int port = 345;
		Socket master = null;
		boolean unused = false;
		while (!unused)
		{
			try
			{
				master = new Socket("192.168.1.40", port);
				System.out.println("Connected to: " + master.getInetAddress());
				unused = true;
			}
			catch (IOException e)
			{
				System.out.println("Port in use, trying: " + ++port);
			}
		}
		return master;
	}
}
