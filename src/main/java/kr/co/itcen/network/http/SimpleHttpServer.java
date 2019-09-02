package kr.co.itcen.network.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHttpServer {
	private static final int PORT = 8888;

	public static void main(String[] args) {

		ServerSocket serverSocket = null;

		try {
			// 1. Create Server Socket
			serverSocket = new ServerSocket();

			// 2. Bind
			//String localhost = "0.0.0.0"; //으로 잡으면 127.~ 192.~ 모든 IP를 허용 웹서버는 이걸로
			String localhost = InetAddress.getLocalHost().getHostAddress();
			serverSocket.bind(new InetSocketAddress(localhost, PORT));
			consolLog("bind " + localhost + ":" + PORT);

			while (true) {
				// 3. Wait for connecting ( accept )
				Socket socket = serverSocket.accept();

				// 4. Delegate Processing Request
				new RequestHandler(socket).start();
			}

		} catch (IOException ex) {
			consolLog("error:" + ex);
		} finally {
			// 5. �옄�썝�젙由�
			try {
				if (serverSocket != null && serverSocket.isClosed() == false) {
					serverSocket.close();
				}
			} catch (IOException ex) {
				consolLog("error:" + ex);
			}
		}
	}

	public static void consolLog(String message) {
		System.out.println("[HttpServer#" + Thread.currentThread().getId() + "] " + message);
	}
}
