package RAID;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

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

	void startListening() throws IOException, ClassNotFoundException, NoSuchAlgorithmException
	{
		while (true)
		{
			ClientChoice choice = (ClientChoice) in.readObject();
			switch (choice)
			{
				case ThrowIOException:
					throw new IOException();
				case SendInfo:
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
					break;
				case EncryptFile:
					encryptFile();
					break;
				default:
					System.out.println("huh");
					break;
			}
		}
	}

	private void delFile() throws IOException
	{
		String fileName = in.readUTF();
		System.out.println("\n" + name + " would like to delete: " + fileName);
		server.delFile(fileName);
	}

	private void sendFile() throws IOException
	{
		String fileName = in.readUTF();
		System.out.println("\n" + name + " requesting " + fileName);
		byte[] data = server.getFile(fileName);
		out.writeInt(data.length);
		out.flush();
		out.write(data);
		out.flush();
		System.out.println("Finished sending file to " + name);
	}

	private void getFile() throws IOException, ClassNotFoundException
	{
		String fileName = in.readUTF();
		System.out.println("\n" + name + " sending new file: " + fileName);
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		System.out.println("File received, passing on to slaves");
		out.writeBoolean(server.addFile(fileName, name, data));
		out.flush();
	}
	
	private void encryptFile() throws IOException, ClassNotFoundException, NoSuchAlgorithmException
	{
		String fileName = in.readUTF();
		System.out.println("\n" + name + " sending new file: " + fileName);
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		String password= in.readUTF();
		System.out.println("File received, passing on to slaves");
		out.writeBoolean(server.addEncryptedFile(fileName, name, data, password));
		out.flush();
	}

	private void sendInfo() throws IOException
	{
		System.out.println("\n" + name + " requesting file info");
		int clientModCount = in.readInt();
		out.writeInt(server.getModCount());
		out.flush();
		if (clientModCount != server.getModCount())
		{
			out.writeObject(server.getFileInfo());
			System.out.println("Finished sending");
		}
		else
			System.out.println("Client modcount up to date");
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