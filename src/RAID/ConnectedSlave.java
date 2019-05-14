package RAID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectedSlave
{
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private Master master;

	public ConnectedSlave(Socket socket, Master master) throws IOException
	{
		this.master = master;
		this.socket = socket;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}

	public boolean disconnected()
	{
		boolean alive;
		try
		{
			out.println("heartbeat");
			alive = in.readLine() == "alive";
		}
		catch (IOException e)
		{
			alive = true;
		}
		return alive;
	}

	public void sendFile(File file)
	{
		out.println("File");
		out.println(file.getAddedBy());
		out.println(file.getDateAdded());
		out.println(file.getFileName());
		out.println(file.getPartNumber());
		out.println(file.getPartsAmount());
		byte[] data = file.getData();
		out.println(data.length);
		for (int i = 0; i < data.length; i++)
			out.println(data[i]);
	}
	
	public void sendMessage(String msg)
	{
		out.println(msg);
	}

	public String receiveMessage()
	{
		try
		{
			return in.readLine();
		}
		catch (IOException e)
		{
			return "ERROR!";
		}
	}

	@Override
	public String toString()
	{
		return "ip: " + socket.getLocalAddress() + ", port: " + socket.getPort();
	}
}
