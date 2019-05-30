package RAID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

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
		readPrevSessionFiles();
		sendPrevSessionFiles();
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
					saveFile();
					break;
				case GetFile:
					sendFile();
					break;
				case DelFile:
					delFile();
					break;
				case GetSpecs:
					getSpecs();
					break;
				case SplitFile:
					splitFile();
					break;
				case RecoverFile:
					recoverFile();
					break;
				case BuildFile:
					buildFile();
					break;
				default:
					System.out.println("ERROR! Unknown command: " + data);
					break;
			}
		}
	}

	private static void saveFile() throws ClassNotFoundException, IOException
	{
		System.out.println("Master sending file");
		MetaFile file = (MetaFile) in.readObject();
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		System.out.println("Succesfully received file: " + file.getFileName());
		metaFiles.put(file.getFileName(), file);
		try (FileOutputStream fos = new FileOutputStream(file.getFileName());
				ObjectOutputStream metaWriter = new ObjectOutputStream(
						new FileOutputStream(file.getFileName() + ".MetaFile"))) {
			fos.write(data);
			metaWriter.writeObject(file);
		}
		catch (Exception e)
		{
			// TODO try again? send error to master??
		}
		System.out.println("Finished writing file to disk");
	}

	private static void heartbeat() throws IOException
	{
		System.out.println("Master requesting a heartbeat");
		out.writeBoolean(false);
		out.flush();
	}

	private static void sendFile() throws IOException
	{
		String fileName = in.readUTF();
		System.out.println("Master requesting file: " + fileName);
		if (metaFiles.containsKey(fileName))
		{
			out.writeBoolean(true);
			byte[] data = Files.readAllBytes(Paths.get(fileName));
			out.writeInt(data.length);
			out.flush();
			out.write(data);
			out.flush();
			out.writeInt(metaFiles.get(fileName).getPartNumber());
			out.flush();
			System.out.println("Finished sending");
		}
		else
			out.writeBoolean(false);
	}

	private static void delFile() throws IOException
	{
		String fileName = in.readUTF();
		Files.delete(Paths.get(fileName));
		metaFiles.remove(fileName);
		System.out.println("Master requesting deletion of: " + fileName);
	}

	private static void splitFile() throws ClassNotFoundException, IOException
	{
		MetaFile file = (MetaFile) in.readObject();
		int slaves = file.getPartsAmount();

		byte[] data = new byte[in.readInt()];
		in.readFully(data);

		int parts = slaves - 1;
		System.out.println("Splitting file into " + parts + " parts & one parity part");
		int fileSize = data.length;
		int splitSize = fileSize / parts;
		int padding = 0;
		if (fileSize % parts != 0)
		{
			splitSize++;
			padding = parts - fileSize % parts;
			System.out.println("Padding required " + padding);
		}
		byte[][] split = new byte[slaves][splitSize];
		for (int i = 0; i < parts; i++)
			for (int j = 0; j < split[i].length; j++)
				if (i * splitSize + j >= fileSize)
					break;
				else
					split[i][j] = data[i * splitSize + j];
		System.out.println("Finished splitting file, creating parity bits");
		for (int i = 0; i < splitSize; i++)
		{
			byte b = split[0][i];
			for (int j = 1; j < parts; j++)
				b ^= split[j][i];
			split[split.length - 1][i] = b;
		}
		out.writeInt(split[0].length);
		out.flush();
		for (int i = 0; i < split.length - 1; i++)
		{
			out.write(split[i]);
			out.flush();
		}
		System.out.println("Finished sending file parts to master");
		metaFiles.put(file.getFileName(), file);
		try (FileOutputStream fos = new FileOutputStream(file.getFileName());
				ObjectOutputStream metaWriter = new ObjectOutputStream(//mayer
				new FileOutputStream(file.getFileName() + ".MetaFile")))//mayer
		{
			fos.write(split[split.length - 1]);
			metaWriter.writeObject(file);//mayer
		}
		catch (Exception e)
		{
		}
		System.out.println("Finished writing file to disk");
	}

	private static void buildFile() throws IOException, ClassNotFoundException
	{
		MetaFile file = (MetaFile) in.readObject();
		String fileName = file.getFileName();
		int parts = file.getPartsAmount();
		int partSize = file.getSize() / (parts == 1 ? 1 : parts - 1);
		byte[][] fileParts = new byte[parts][partSize];
		for (int i = 0; i < parts - 1; i++)
		{
			int partNum = in.readInt();
			in.readFully(fileParts[partNum]);
		}
		if (metaFiles.containsKey(fileName))
			fileParts[metaFiles.get(fileName).getPartNumber()] = Files.readAllBytes(Paths.get(fileName));
		byte[] fullFile = new byte[file.getSize() - file.getPadding()];
		for (int i = 0; i < fullFile.length; i++)
			fullFile[i] = fileParts[i / partSize + (parts == 1 ? 0 : 1)][i % partSize];
		out.writeInt(fullFile.length);
		out.flush();
		out.write(fullFile);
		out.flush();
	}

	private static void recoverFile() throws IOException, ClassNotFoundException
	{
		MetaFile file = (MetaFile) in.readObject();
		String fileName = file.getFileName();
		int parts = file.getPartsAmount();
		int partSize = file.getSize() / (parts - 1);
		byte[][] fileParts = new byte[parts][];
		for (int i = 0; i < parts - 2; i++)
		{
			int partNum = in.readInt();
			fileParts[partNum] = new byte[partSize];
			in.readFully(fileParts[partNum]);
		}
		if (metaFiles.containsKey(fileName))
			fileParts[metaFiles.get(fileName).getPartNumber()] = Files.readAllBytes(Paths.get(fileName));
		int missing = -1;
		for (int i = 0; i < parts; i++)
			if (fileParts[i] == null)
				missing = i;
		if (missing != 0)
		{
			fileParts[missing] = new byte[partSize];
			for (int i = 0; i < partSize; i++)
			{
				byte b = fileParts[0][i];
				for (int j = 1; j < fileParts.length; j++)
					if (j == missing)
						continue;
					else
						b ^= fileParts[j][i];
				fileParts[missing][i] = b;
			}
		}
		byte[] fullFile = new byte[file.getSize() - file.getPadding()];
		for (int i = 0; i < fullFile.length; i++)
			fullFile[i] = fileParts[i / partSize + 1][i % partSize];
		out.writeInt(fullFile.length);
		out.flush();
		out.write(fullFile);
		out.flush();
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

	public static void getSpecs() throws IOException
	{
		int specs = 0;
		if (System.getProperty("user.dir").contains("eclipse"))
			specs = 10000000;
		System.out.println("Master requesting specs: " + specs);
		out.writeInt(specs);
		out.flush();
	}
	private static void sendPrevSessionFiles() throws IOException {
		System.out.println("Sending Previous Session Files");
		out.writeObject(metaFiles);
		out.flush();
		System.out.println("Done Sending Previous Session Files");
	}

	private static void readPrevSessionFiles() throws IOException, ClassNotFoundException {
		System.out.println("Reading Previous Session Files");
		Set<String> prevFiles = new HashSet<>();
		Set<String> tempMetaFiles = new HashSet<>();
	
		String path = new File("").getCanonicalPath();
		Path dir = Paths.get(path);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
		    for (Path file: stream) {
		    	File tempFile = file.toFile();
		    	if(tempFile.isFile() && !tempFile.getName().matches("(.classpath|.project|desktop.ini)")) {
		    		if(tempFile.getName().endsWith(".MetaFile"))
		    			tempMetaFiles.add(tempFile.getName());
		    		else
		    			prevFiles.add(tempFile.getName());
		    	}
		    		
		    }
		} catch (IOException | DirectoryIteratorException x) {
		    System.err.println(x);
		}
		//this part only adds the MetaFile if its corresponding actual file is extant
		for(String meta: tempMetaFiles) {
			if(prevFiles.contains(removeExtension(meta))) {
				ObjectInputStream metaReader = new ObjectInputStream(new FileInputStream(meta));
				metaFiles.put(removeExtension(meta), (MetaFile) metaReader.readObject());
				metaReader.close();
			}
		}
		System.out.println("Done Reading Previous Session Files");
	}
	private static String removeExtension(String fileName) {
		if(fileName.contains("."))
			return fileName.substring(0, fileName.lastIndexOf("."));
		return fileName;
	}
}
