package RAID;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

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

	public void sendMessage(String msg) throws IOException
	{
		out.writeObject(SlaveCommand.Message);
		out.writeUTF(msg);
		out.flush();
	}

	@Override
	public String toString()
	{
		return "ip: " + socket.getLocalSocketAddress() + ", port: " + socket.getPort();
	}

	public void delFile(String fileName) throws IOException
	{
		out.writeObject(SlaveCommand.DelFile);
		out.writeUTF(fileName);
		out.flush();
	}

	public void shutdown() throws IOException
	{
		out.writeObject(SlaveCommand.Shutdown);
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

	public byte[] recoverFile(MetaFile file, HashMap<Integer, byte[]> parts) throws IOException
	{
		out.writeObject(SlaveCommand.RecoverFile);
		out.writeObject(file);
		for (Map.Entry<Integer, byte[]> fp : parts.entrySet())
		{
			out.writeInt(fp.getKey());
			out.flush();
			out.write(fp.getValue());
			out.flush();
		}
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		return data;
	}

	public void getFile(HashMap<Integer, byte[]> parts, String fileName) throws IOException
	{
		out.writeObject(SlaveCommand.GetFile);
		out.writeUTF(fileName);
		out.flush();
		boolean hasFile = in.readBoolean();
		if (hasFile)
		{
			byte[] data = new byte[in.readInt()];
			in.readFully(data);
			int partNumber = in.readInt();
			parts.put(partNumber, data);
		}
	}

	public byte[] buildFile(MetaFile file, HashMap<Integer, byte[]> parts) throws IOException
	{
		out.writeObject(SlaveCommand.BuildFile);
		out.writeObject(file);
		for (Map.Entry<Integer, byte[]> fp : parts.entrySet())
		{
			out.writeInt(fp.getKey());
			out.flush();
			out.write(fp.getValue());
			out.flush();
		}
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		System.out.println(this + " finished building file");
		return data;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, MetaFile> getPrevSessionFiles()
	{
		System.out.println("\t\tChecking for previous session files");
		try
		{
			Object tempObject = in.readObject();
			if (tempObject instanceof HashMap)
				return (HashMap<String, MetaFile>) tempObject;
		}
		catch (IOException | ClassNotFoundException e)
		{
		}
		return null;
	}
}
