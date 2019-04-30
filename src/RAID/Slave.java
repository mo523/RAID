package RAID;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Slave {
	static Scanner kyb = new Scanner(System.in);

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		int port = 12345;
		boolean connected = false;
		InetAddress host = InetAddress.getLocalHost();
		//System.out.println(host.getHostAddress());192.168.1.4
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		do {
			Socket socket = new Socket(host.getHostAddress(), port);
			socket.setSoTimeout(5 * 1000);
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
				connected = true;
			} catch (SocketTimeoutException e) {
				System.out.println("Port " + port + " already has a connection. Trying again with " + ++port);
			}
		} while (!connected);
		
		String temp = null;
		do {
			System.out.println("Write Something");
			temp = kyb.next();
			out.writeObject(temp);
		} while (!temp.equals("1"));
	}
}
