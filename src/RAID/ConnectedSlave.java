package RAID;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectedSlave implements Comparable<ConnectedSlave>
{
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public ConnectedSlave(Socket socket) throws IOException
	{
		this.socket = socket;
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
	}

	public boolean disconnected()
	{
		boolean alive;
		try
		{
			out.writeObject(SlaveCommand.Heartbeat);
			alive = in.readBoolean();
		}
		catch (IOException e)
		{
			alive = true;
		}
		return !alive;
	}

	public void sendFile(MetaFile file, byte[] data) throws IOException
	{
		out.writeObject(SlaveCommand.PutFile);
		out.writeObject(file);
		out.writeInt(data.length);
		out.flush();
		out.write(data);
		out.flush();
	}

	public void sendMessage(String msg)
	{
		try
		{
			out.writeUTF(msg);
		}
		catch (IOException e)
		{
			System.out.println(this + ": slave threw error sending message");
		}
	}

	@Override
	public String toString()
	{
		return "ip: " + socket.getLocalAddress() + ", port: " + socket.getPort();
	}

	public byte[] getFile(String fileName) throws IOException
	{
		out.writeObject(SlaveCommand.GetFile);
		out.writeUTF(fileName);
		out.flush();
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		return data;
	}

	public void delFile(String fileName) throws IOException
	{
		out.writeObject(SlaveCommand.DelFile);
		out.writeUTF(fileName);
	}

	public int getSpecs() throws IOException
	{
		out.writeObject(SlaveCommand.GetSpecs);
		return in.readInt();
	}

	@Override
	public int compareTo(ConnectedSlave other)
	{
		try
		{
			return this.getSpecs() - other.getSpecs();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}
}
