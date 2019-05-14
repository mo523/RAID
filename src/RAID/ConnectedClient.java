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

				case -1:
					System.out.println("ERROR!");
					sendError();
					break;
				case 0:
					throw new IOException();
				case 1:
					System.out.println(name + " requesting file info");
					sendInfo();
					break;
				case 2:
					getFile();
					break;
				default:
					System.out.println("huh");
					break;
			}
		}
	}

	private void getFile() throws IOException
	{
		String fileName = in.readLine();
		byte[] data = new byte[Integer.parseInt(in.readLine())];
		for (int i = 0; i < data.length; i++)
			data[i] = Byte.parseByte(in.readLine());

		File file = new File(fileName, "today", name, 0, 1);
		file.setData(data);
		server.addFile(file);
	}

	private void sendError()
	{
		out.println("ERROR");
	}

	private void sendInfo()
	{
	
		out.println(server.getFileInfo());
		System.out.println("Finished sending");
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