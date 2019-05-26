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
	private int currSpecs;

	public ConnectedSlave(Socket socket) throws IOException
	{
		this.socket = socket;
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
		currSpecs = -1;
	}

	public boolean disconnected()
	{
		boolean dead;
		try
		{
			out.writeObject(SlaveCommand.Heartbeat);
			dead = in.readBoolean();
		}
		catch (IOException e)
		{
			dead = true;
		}
		return dead;
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

	public void updateSpecs() throws IOException
	{
		out.writeObject(SlaveCommand.GetSpecs);
		currSpecs = in.readInt();
	}

	public int getCurrSpecs()
	{
		return currSpecs;
	}

	@Override
	public int compareTo(ConnectedSlave other)
	{
		return other.getCurrSpecs() - currSpecs;
	}

	public byte[][] splitFile(MetaFile file, byte[] data) throws IOException
	{
		out.writeObject(SlaveCommand.SplitFile);
		out.writeObject(file);
		out.writeInt(data.length);
		out.flush();
		out.write(data);
		out.flush();
		int splitLength = in.readInt();
		byte[][] splitData = new byte[file.getPartsAmount() - 1][splitLength];
		for (int i = 0; i < splitData.length; i++)
			in.readFully(splitData[i]);
		return splitData;
	}
}
