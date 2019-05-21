package RAID;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

	public static void main(String[] args) throws IOException, ClassNotFoundException
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
	 * @throws IOException if there is an error reading (most likely
	 * caused by a unexpected server shutdown).
	 * @throws ClassNotFoundException
	 */
	private static void listen() throws IOException, ClassNotFoundException
	{
		while (true)
		{
			SlaveCommand data = (SlaveCommand) in.readObject();
			switch (data)
			{

				case Heartbeat:
					heartbeat();
					break;
				case PutFile:
					receiveFile();
					break;
				case GetFile:
					sendFile();
					break;
				case DelFile:
					delFile();
					break;
				case GetSpecs:
					delFile();
					break;
				default:
					System.out.println("ERROR! Unknown command: " + data);
					break;
			}
		}
	}

	private static void heartbeat() throws IOException
	{
		System.out.println("Master requesting a heartbeat");
		out.writeBoolean(true);
		out.flush();
	}

	private static void receiveFile() throws IOException, ClassNotFoundException
	{
		System.out.println("Master sending file");
		MetaFile file = (MetaFile) in.readObject();
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		metaFiles.put(file.getFileName(), file);
		try (FileOutputStream fos = new FileOutputStream(file.getFileName()))
		{
			fos.write(data);
		}
		catch (Exception e)
		{
			// TODO try again? send error to master??
		}

		System.out.println("Succesfully received file: " + file.getFileName());
	}

	private static void sendFile() throws IOException
	{
		String fileName = in.readUTF();
		System.out.println("Master requesting file: " + fileName);
		byte[] data = Files.readAllBytes(Paths.get(fileName));
		out.writeInt(data.length);
		out.flush();
		out.write(data);
		out.flush();
		System.out.println("Finished sending");
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
	 * localhost and the webserver built in with a character shortcut). It then
	 * creates a socket connection with that ip address on port 536. Next, it stores
	 * a output stream inside a PrintWriter & a input stream inside of a
	 * BufferedReader for later use.
	 * 
	 * @throws IOException if an I/O error occurs when creating the output stream.
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
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		System.out.println("Connected to the RAID Server: " + socket.getInetAddress() + ", " + socket.getLocalPort());
	}

	public void getSpecs() throws IOException
	{
		out.writeInt(0);
		out.flush();
	}
}
