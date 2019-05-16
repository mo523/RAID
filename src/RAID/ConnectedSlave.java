package RAID;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectedSlave
{
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public ConnectedSlave(Socket socket) throws IOException
	{
		this.socket = socket;
		in = new ObjectInputStream(socket.getInputStream());//new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new ObjectOutputStream(socket.getOutputStream());
	}

	public boolean disconnected()
	{
		boolean dead = false;
		try
		{
			out.writeObject(SlaveCommand.Heartbeat);
			dead = in.readUTF().equals("alive");
		}
		catch (IOException e)
		{
			dead = true;
		} 
		return dead;
	}

	public void sendFile(MetaFile file, byte[] data)
	{
		
//		out.println(file.getAddedBy());
//		out.println(file.getDateAdded());
//		out.println(file.getFileName());
//		out.println(file.getPartNumber());
//		out.println(file.getPartsAmount());
		try {
			out.writeObject(SlaveCommand.PutFile);
			out.writeObject(file);
			out.writeObject(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		out.println(data.length);
//		for (int i = 0; i < data.length; i++)
//			out.println(data[i]);

	}

	public void sendMessage(String msg)
	{
		try {
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
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
		byte[] data = null;
		try {
			data = (byte[]) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		//new byte[Integer.parseInt(in.readLine())];
//		for (int i = 0; i < data.length; i++)
//			data[i] = Byte.parseByte(in.readLine());
		return data;
	}

	public void delFile(String fileName)
	{
		try {
			out.writeObject(SlaveCommand.DelFile);
			out.writeUTF(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
