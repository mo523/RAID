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

	public ConnectedSlave(Socket s) throws IOException
	{
		this.socket = s;
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
