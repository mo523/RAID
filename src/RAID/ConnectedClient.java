package RAID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class ConnectedClient
{
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private String name;
	private Server server;
	private ObjectInputStream objectIn;
	private ObjectOutputStream objectOut;

	public ConnectedClient(Socket socket, Server server) throws IOException
	{
		this.server = server;
		this.socket = socket;
		objectIn = new ObjectInputStream(socket.getInputStream());
		objectOut = new ObjectOutputStream(socket.getOutputStream());
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		this.name = in.readLine();
	}

	void startListening() throws IOException, ClassNotFoundException
	{
		while (true)
		{
			/*
			 * int choice; try { choice = Integer.parseInt(in.readLine()); } catch
			 * (NumberFormatException e) { choice = -1; } switch (choice) { case 0: throw
			 * new IOException(); case 1: System.out.println(name +
			 * " requesting file info"); sendInfo(); break; case 2: getFile(); break; case
			 * 3: sendFile(); break; case 4: delFile(); default: System.out.println("huh");
			 * break; }
			 */
			
			ClientChoice choice = (ClientChoice) objectIn.readObject();
			switch (choice) {
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
		String fileName = in.readLine();
		server.delFile(fileName);
	}

	private void sendFile() throws IOException
	{
		String fileName = in.readLine();
//		byte[] data = server.getFile(fileName);
//		out.println(data.length);
//		for (byte b : data)
//			out.println(b);
		objectOut.writeObject(server.getFile(fileName));
	}

	private void getFile() throws IOException, ClassNotFoundException
	{
		String fileName = in.readLine();
		byte[] data = (byte[]) objectIn.readObject();/*new byte[Integer.parseInt(in.readLine())];
		for (int i = 0; i < data.length; i++)
			data[i] = Byte.parseByte(in.readLine());*/
		MetaFile file = new MetaFile(fileName, new Date().toString().substring(0, 16), name, 0, 1);
		server.addFile(file, data);
	}

	private void sendInfo() throws IOException
	{
		int clientModCount = objectIn.readInt();//Integer.parseInt(in.readLine());
		objectOut.writeInt(server.getModCount());//out.println(server.getModCount());
		if (clientModCount != server.getModCount())
		{
//			for (String s : server.getFileInfo())
//				out.println(s);
			objectOut.writeObject(server.getFileInfo());
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
}