package RAID;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

public class ConnectedClient
{
	private Socket socket;
	private String name;
	private Server server;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public ConnectedClient(Socket socket, Server server) throws IOException
	{
		this.server = server;
		this.socket = socket;
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
		this.name = in.readUTF();
	}

	void startListening() throws IOException, ClassNotFoundException
	{
		while (true)
		{

			ClientChoice choice = (ClientChoice) in.readObject();
			switch (choice)
			{
				case ThrowIOException:
					throw new IOException();
				case SendInfo:
					System.out.println(name + " requesting file info");
					sendInfo();
					break;
				case GetFile:
					getFile();
					break;
				case SendFile:
					sendFile();
					break;
				case DelFile:
					delFile();
				default:
					System.out.println("huh");
					break;
			}
		}
	}

	private void delFile() throws IOException
	{
		String fileName = in.readUTF();
		server.delFile(fileName);
	}

	private void sendFile() throws IOException
	{
		String fileName = in.readUTF();
		byte[] data = server.getFile(fileName);
		out.writeInt(data.length);
		out.flush();
		out.write(data);
		out.flush();
	}

	private void getFile() throws IOException, ClassNotFoundException
	{
		String fileName = in.readUTF();
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		MetaFile file = new MetaFile(fileName, new Date().toString().substring(0, 16), name, 0, 1);
		server.addFile(file, data);
	}

	private void sendInfo() throws IOException
	{
		int clientModCount = in.readInt();
		out.writeInt(server.getModCount());
		out.flush();
		if (clientModCount != server.getModCount())
		{
			out.writeObject(server.getFileInfo());
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

	public void close() throws IOException
	{
		socket.close();
	}
}