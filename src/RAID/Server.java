package RAID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class Server extends Thread
{
	private Socket socket;
	private ServerSocket listener;
	private PrintWriter out;
	private BufferedReader in;
	private volatile HashSet<File> files;

	public Server(HashSet<File> files)
	{
		this.files = files;
	}

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
		while (true)
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
			case 1: // View list of files
				files.stream().forEachOrdered(out::println);
				break;
			case 2: // Add file
				break;
			case 3: // Get file
				break;
			case 4: // Remove file
				break;
			default:
				out.println("ERROR!");
				break;
		}
		out.println("$$$");
	}
}
