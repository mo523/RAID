package RAID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectedClient
{
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private String name;
	private Server server;

	public ConnectedClient(Socket socket, Server server) throws IOException
	{
		this.server = server;
		this.socket = socket;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		this.name = in.readLine();
	}

	void startListening() throws IOException
	{
		while (true)
		{
			int choice;
			try
			{
				choice = Integer.parseInt(in.readLine());
			}
			catch (NumberFormatException e)
			{
				choice = -1;
			}
			switch (choice)
			{
				case 0:
					throw new IOException();
				case 1:
					System.out.println(name + " requesting file info");
					sendInfo();
					break;
				case 2:
					getFile();
					break;
				case 3:
					sendFile();
					break;
				case 4:
					delFile();
				default:
					System.out.println("huh");
					break;
			}
		}
	}

	private void delFile() throws IOException
	{
		String fileName = in.readLine();
		server.delFile(fileName);
	}

	private void sendFile() throws IOException
	{
		String fileName = in.readLine();
		byte[] data = server.getFile(fileName);
		out.println(data.length);
		for (byte b : data)
			out.println(b);
	}

	private void getFile() throws IOException
	{
		String fileName = in.readLine();
		byte[] data = new byte[Integer.parseInt(in.readLine())];
		for (int i = 0; i < data.length; i++)
			data[i] = Byte.parseByte(in.readLine());
		// TODO add current datetime
		MetaFile file = new MetaFile(fileName, "today", name, 0, 1);
		server.addFile(file, data);
	}

	private void sendInfo() throws IOException
	{
		int clientModCount = Integer.parseInt(in.readLine());
		out.println(server.getModCount());
		if (clientModCount != server.getModCount())
		{
			for (String s : server.getFileInfo())
				out.println(s);
			System.out.println("Finished sending");
		}
		else
			System.out.println("Client modcount up to date :)");
	}

	public String getClientName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return "Name: " + name + ", IP: " + socket.getLocalAddress() + ", Port: " + socket.getPort();
	}
}