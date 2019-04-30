package RAID;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Test_Master {
	static volatile int port = 12345;
	static volatile Object portLock = new Object();
	static volatile AtomicBoolean needAnotherListener = new AtomicBoolean(false);
//	static volatile Object listenerLock = new Object();
	static volatile Set<Socket> connections = new HashSet<Socket>();
	static Scanner kyb = new Scanner(System.in);
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		new Thread(Test_Master::listenForConnections).start();;
		for (int i = 0; i < 5; i++) {
			if(kyb.next().equals("q"))
				connections.forEach(System.out::println);
		}
	}

	public static void listenForConnections(){
		while (true) {
			new Thread(() -> {
				try {
					acceptConnectionAndAddToSet();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}).start();
			while (!needAnotherListener.get())
				try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

		}
	}

	public static void acceptConnectionAndAddToSet() throws IOException, ClassNotFoundException {
		needAnotherListener.set(false);
		ServerSocket server;
		Socket socket;

		synchronized (portLock) {
			System.out.println("Waiting for the client request " + port);
			server = new ServerSocket(port);
			socket = server.accept();
			System.out.println(port++ + " connection accepted");
		}
		needAnotherListener.set(true);

		connections.add(socket);
		//for some reason you have to make the streams for the client/slave to not timeout
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		String temp = null;
		do {
			temp = (String) in.readObject();
			System.out.println(temp);
		} while (!temp.equals("1"));
	}

}
