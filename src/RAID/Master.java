package RAID;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.net.util.Base64;

public class Master extends Thread
{
	private volatile HashSet<ConnectedSlave> slaves;
	private volatile HashMap<String, MetaFile> files;

	public Master(HashMap<String, MetaFile> files)
	{
		slaves = new HashSet<>();
		this.files = files;
	}

	public void run()
	{
		listenForConnections();
	}

	public void listenForConnections()
	{
		while (true)
		{
			try
			{
				ServerSocket ss = new ServerSocket(536);
				ConnectedSlave ps = new ConnectedSlave(ss.accept());
				System.out.println("\n\tSlave connected " + ps);
				synchronized (slaves)
				{
					slaves.add(ps);
				}
				ss.close();
				files.putAll(ps.getPrevSessionFiles());
				System.out.println("\t\tReloading complete");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean addFile(String fileName, String name, byte[] data) throws IOException
	{
		checkForDisconnect();
		MetaFile file;
		synchronized (slaves)
		{
			int padding = 0;
			if (slaves.size() == 0)
			{
				System.out.println("No slaves are connected yet");
				return false;
			}
			else
			{
				if (slaves.size() < 3)
				{
					file = new MetaFile(fileName, new Date().toString().substring(0, 16), name, -1, slaves.size(),
							padding, data.length + padding);
					System.out.println("Not enough slaves for parity backup, sending complete file to all slaves");
					MetaFile nFile = file.getNextMetaFile();
					for (ConnectedSlave cs : slaves)
					{
						cs.sendFile(nFile, data);
						nFile = nFile.getNextMetaFile();
					}
				}
				else
				{
					if (data.length % (slaves.size() - 1) != 0)
						padding = (slaves.size() - 1) - (data.length) % (slaves.size() - 1);
					file = new MetaFile(fileName, new Date().toString().substring(0, 16), name, -1, slaves.size(),
							padding, data.length + padding);
					PriorityQueue<ConnectedSlave> pq = getSlavePQ();
					ConnectedSlave currSlave = pq.poll();
					System.out.println(currSlave + " chosen to build file");
					MetaFile nfile = file.getNextMetaFile();
					byte[][] splitData = currSlave.splitFile(nfile, data);
					System.out.println(currSlave + " finished splitting file, sending to remaining slaves");
					for (int i = 0; i < splitData.length; i++)
					{
						currSlave = pq.poll();
						nfile = nfile.getNextMetaFile();
						currSlave.sendFile(nfile, splitData[i]);
					}
				}
				files.put(file.getFileName(), file);
				return true;
			}
		}
	}
	
	public boolean addEncryptedFile(String fileName, String name, byte[] data,String password) throws IOException
	{
		checkForDisconnect();
		MetaFile file;
		synchronized (slaves)
		{
			int padding = 0;
			if (slaves.size() == 0)
			{
				System.out.println("No slaves are connected yet");
				return false;
			}
			else
			{
				KeyGenerator keyGen;
				keyGen = KeyGenerator.getInstance("AES");
				SecureRandom secRandom = new SecureRandom();
				
				
				
			      
			      //Initializing the KeyGenerator
			      keyGen.init(secRandom);
				
				
		     Key key = keyGen.generateKey();
		     byte[] encryptedKey = encryptKey(key, password);
					if (data.length % (slaves.size() - 1) != 0)
						padding = (slaves.size() - 1) - (data.length) % (slaves.size() - 1);
					file = new EncryptedMetaFile(fileName, new Date().toString().substring(0, 16), name, -1, slaves.size(),
							padding, data.length + padding, encryptedKey);
					PriorityQueue<ConnectedSlave> pq = getSlavePQ();
					ConnectedSlave currSlave = pq.poll();
					System.out.println(currSlave + " chosen to encrypt file");
					
					byte[] encryptedData = currSlave.encryptFile(key, data);
					System.out.println(currSlave + " finished splitting file, sending to remaining slaves");
					
					if (slaves.size() < 3)
					{
						file = new MetaFile(fileName, new Date().toString().substring(0, 16), name, -1, slaves.size(),
								padding, data.length + padding);
						System.out.println("Not enough slaves for parity backup, sending complete file to all slaves");
						MetaFile nFile = file.getNextMetaFile();
						for (ConnectedSlave cs : slaves)
						{
							cs.sendFile(nFile, data);
							nFile = nFile.getNextMetaFile();
						}
					}
					else
					{
						if (data.length % (slaves.size() - 1) != 0)
							padding = (slaves.size() - 1) - (data.length) % (slaves.size() - 1);
						file = new MetaFile(fileName, new Date().toString().substring(0, 16), name, -1, slaves.size(),
								padding, data.length + padding);
						PriorityQueue<ConnectedSlave> pq = getSlavePQ();
						ConnectedSlave currSlave = pq.poll();
						System.out.println(currSlave + " chosen to build file");
						MetaFile nfile = file.getNextMetaFile();
						byte[][] splitData = currSlave.splitFile(nfile, data);
						System.out.println(currSlave + " finished splitting file, sending to remaining slaves");
						for (int i = 0; i < splitData.length; i++)
						{
							currSlave = pq.poll();
							nfile = nfile.getNextMetaFile();
							currSlave.sendFile(nfile, splitData[i]);
						}
					}
				files.put(file.getFileName(), file);
				return true;
			}
		}
	}

	public void broadcastMessage(String msg)
	{
		for (ConnectedSlave s : slaves)
			s.sendMessage(msg);
	}

	public void checkForDisconnect() throws IOException
	{
		HashSet<ConnectedSlave> disconnected = new HashSet<>();
		synchronized (slaves)
		{
			for (ConnectedSlave s : slaves)
			{
				if (s.disconnected())
				{
					System.out.println("\t\t" + s.toString() + " is disconnected");
					disconnected.add(s);
				}
			}
			slaves.removeAll(disconnected);

			// Recover lost files
			if (!disconnected.isEmpty())
			{
				PriorityQueue<ConnectedSlave> pq = getSlavePQ();
				ConnectedSlave currSlave = pq.poll();
				for (MetaFile mf : files.values())
				{
					HashMap<Integer, byte[]> parts = new HashMap<>();
					for (ConnectedSlave cs : pq)
						cs.getFile(parts, mf.getFileName());
					byte[] data = currSlave.recoverFile(mf, parts);
					addFile(mf.getFileName(), mf.getAddedBy(), data);
				}
			}
		}
	}

	public int getSlaveCount()
	{
		return slaves.size();
	}

	public HashSet<ConnectedSlave> getSockets()
	{
		return slaves;
	}

	public byte[] getFile(String fileName) throws IOException
	{
		synchronized (slaves)
		{
			checkForDisconnect();
			HashMap<Integer, byte[]> parts = new HashMap<>();
			PriorityQueue<ConnectedSlave> pq = getSlavePQ();
			ConnectedSlave currSlave = pq.remove();
			System.out.println(currSlave + " chosen to build file");
			MetaFile file = files.get(fileName);
			System.out.println(file);
			System.out.println("Getting parts from slaves");
			for (ConnectedSlave cs : pq)
				cs.getFile(parts, file.getFileName());
			return currSlave.buildFile(file, parts);
		}
	}

	public void delAllFiles() throws IOException
	{
		for (String fName : files.keySet())
			delFile(fName);
	}

	public void delFile(String fileName) throws IOException
	{
		synchronized (slaves)
		{
			for (ConnectedSlave cs : slaves)
				cs.delFile(fileName);
		}
		files.remove(fileName);
		System.out.println(fileName + " successfully removed");
	}

	private PriorityQueue<ConnectedSlave> getSlavePQ() throws IOException
	{
		PriorityQueue<ConnectedSlave> pq = new PriorityQueue<ConnectedSlave>();
		for (ConnectedSlave cs : slaves)
			cs.updateSpecs();
		for (ConnectedSlave cs : slaves)
			pq.add(cs);
		return pq;
	}

	public void shutdown()
	{
		for (ConnectedSlave cs : slaves)
			try
			{
				cs.shutdown();
			}
			catch (IOException e)
			{

			}
			finally
			{

			}
	}
	
	public byte[] encryptKey(Key key, String password) {
	    try {
	        IvParameterSpec iv = new IvParameterSpec("encryptionIntVec".getBytes("UTF-8"));
	        SecretKeySpec skeySpec = new SecretKeySpec(password.getBytes(), "AES");
	 
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
	 
	        byte[] encrypted = cipher.doFinal(key.getEncoded());
	        System.out.println(Base64.encodeBase64(encrypted).length+"newen");
	        return Base64.encodeBase64(encrypted);
	        
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return null;
	}
}
