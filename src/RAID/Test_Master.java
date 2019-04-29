package RAID;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Test_Master {
	static volatile int port = 12345;
	static volatile Object portLock = new Object();
	static volatile AtomicBoolean needAnotherListener = new AtomicBoolean(false);
	static volatile Object listenerLock = new Object();
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

		while (true) {
			new Thread(() -> {
				try {
					acceptAndRun();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}).start();
			while(!needAnotherListener.get())
				Thread.sleep(5000);
			
		}

	}

	public static void acceptAndRun()  throws IOException, ClassNotFoundException {
		needAnotherListener.set(false);
		ServerSocket server;
		Socket socket;

		synchronized(portLock) {
			System.out.println("Waiting for the client request " + port);
			server = new ServerSocket(port);
			socket = server.accept();
			port++;
			System.out.println(port - 1 + " connection accepted");
		}
		needAnotherListener.set(true);
		
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		String temp = null;
		do {
			temp = (String) in.readObject();
			System.out.println(temp);
		} while (!temp.equals("1"));
	}

}
