package RAID;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

/**
 * The {@code Slave} class is a standalone class used for the slave side of the
 * RAID system. The class is completely static since it contains its own
 * {@code main} method and should not be instantiated in another class.
 * <p>
 * {@code Slave} works by connecting to the {@link RAID_Server} through the
 * {@link ConnectedSlave} class. See the {@link #connectToRS()} method to see
 * how the connection is established.
 * <p>
 * Once the connection is established, the slave calls the {@link #listen()}
 * method to listen for commands from the master.
 * 
 * @see RAID_Server
 * @see ConnectedSlave
 * 
 * @author Moshe Hirsch
 *
 */

public class Slave
{
	private static Socket socket;
	private static Scanner kb;
	private static ObjectInputStream in;
	private static ObjectOutputStream out;
	private static HashMap<String, MetaFile> metaFiles;

	public static void main(String[] args) throws IOException
	{
		metaFiles = new HashMap<>();
		kb = new Scanner(System.in);
		connectToRS();
		listen();
		kb.close();
	}

	/**
	 * {@code #listen()} starts an infinite loop to start listening for commands
	 * from the master. It reads a string from the input buffer (blocking until a
	 * line is available). A simple if else block decides what to do based on the
	 * command.
	 * <p>
	 * {@code heartbeat} calls the {@link #heartbeat()} method.
	 * <p>
	 * {@code putFile} calls the {@link #receiveFile()} method.
	 * <p>
	 * {@code getFile} calls the {@link #sendFile()} method.
	 * <p>
	 * {@code delFile} calls the {@link #delFile()} method.
	 * <p>
	 * Anything else prints an error message followed by the unknown command.
	 * 
	 * @throws IOException if there is an error reading (most likely caused by a
	 * unexpected server shutdown).
	 */
	private static void listen() throws IOException
	{
		while (true)
		{
			Object data = null;
			try {
				data = in.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if (data == SlaveCommand.Heartbeat)
				heartbeat();
			else if (data == SlaveCommand.PutFile)
				receiveFile();
			else if (data == SlaveCommand.GetFile)
				sendFile();
			else if (data == SlaveCommand.DelFile)
				delFile();
			else
				System.out.println("ERROR! Unknown command: " + data);
		}
	}

	private static void heartbeat()
	{
		System.out.println("Master requesting a heartbeat");
		try {
			out.writeUTF("alive");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void receiveFile() throws IOException
	{
//		String addedBy = in.readLine();
//		String dateAdded = in.readLine();
//		String fileName = in.readLine();
//		int partNumber = Integer.parseInt(in.readLine());
//		int partsAmount = Integer.parseInt(in.readLine());
		MetaFile file = null;
		byte[] data = null;
		try {
			file = (MetaFile) in.readObject();
			data = (byte[]) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}//new MetaFile(fileName, dateAdded, addedBy, partNumber, partsAmount);
//		byte[] data = new byte[Integer.parseInt(in.readLine())];
//		for (int i = 0; i < data.length; i++)
//			data[i] = Byte.parseByte(in.readLine());
		
		
		metaFiles.put(file.getFileName(), file);
		FileOutputStream fos = new FileOutputStream(file.getFileName());
		fos.write(data);
		fos.close();
		System.out.println("Succesfully received file: " + file.getFileName());
	}

	private static void sendFile() throws IOException
	{
		String fileName = in.readUTF();
		byte[] data = Files.readAllBytes(Paths.get(fileName));
		out.writeObject(data);
//		out.println(data.length);
//		for (byte b : data)
//			out.println(b);
		System.out.println("Master requesting file: " + fileName);
	}

	private static void delFile() throws IOException
	{
		String fileName = in.readUTF();
		Files.delete(Paths.get(fileName));
		metaFiles.remove(fileName);
		System.out.println("Master requesting deletion of: " + fileName);
	}

	/**
	 * {@code connectToRS} is in charge of establishing a connection between this
	 * slave and the master running on the RAID server.
	 * <p>
	 * It starts with a simple prompt asking for the server's address (with
	 * localhost and the webserver built in with a character shortcut).
	 * It then creates a socket connection with that ip address on port 536. Next,
	 * it stores a output stream inside a PrintWriter & a input stream inside of a
	 * BufferedReader for later use.
	 * 
	 * @throws IOException if an I/O error occurs when creating the
	 * output stream.
	 */
	private static void connectToRS() throws IOException
	{
		System.out.println("Welcome to The RAID Slave System");
		System.out.println("What is the IP address for the RAID Server? (l == localhost, m == moshehirsch.com)");
		String ip = kb.nextLine();
		if (ip.charAt(0) == 'l')
			ip = "127.0.0.1";
		else if (ip.charAt(0) == 'm')
			ip = "www.moshehirsch.com";
		socket = new Socket(ip, 536);
		out = new ObjectOutputStream(socket.getOutputStream());//PrintWriter(socket.getOutputStream(), true);
		in = new ObjectInputStream(socket.getInputStream());//BufferedReader(new InputStreamReader(socket.getInputStream()));
		System.out.println("Connected to the RAID Server: " + socket.getInetAddress() + ", " + socket.getLocalPort());
	}
}
