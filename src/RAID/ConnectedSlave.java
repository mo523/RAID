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

	public ConnectedSlave(Socket socket) throws IOException
	{
		this.socket = socket;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}

	public boolean disconnected()
	{
		boolean dead;
		try
		{
			out.println("heartbeat");
			dead = in.readLine() == "alive";
		}
		catch (IOException e)
		{
			dead = true;
		}
		return dead;
	}

	public void sendFile(MetaFile file, byte[] data)
	{
		out.println("putFile");
		out.println(file.getAddedBy());
		out.println(file.getDateAdded());
		out.println(file.getFileName());
		out.println(file.getPartNumber());
		out.println(file.getPartsAmount());
		out.println(data.length);
		for (int i = 0; i < data.length; i++)
			out.println(data[i]);
	}

	public void sendMessage(String msg)
	{
		out.println(msg);
	}

	@Override
	public String toString()
	{
		return "ip: " + socket.getLocalAddress() + ", port: " + socket.getPort();
	}

	public byte[] getFile(String fileName) throws IOException
	{
		out.println("getFile");
		out.println(fileName);
		byte[] data = new byte[Integer.parseInt(in.readLine())];
		for (int i = 0; i < data.length; i++)
			data[i] = Byte.parseByte(in.readLine());
		return data;
	}

	public void delFile(String fileName)
	{
		out.println("delFile");
		out.println(fileName);
	}
}
