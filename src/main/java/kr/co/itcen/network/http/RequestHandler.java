package kr.co.itcen.network.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;

public class RequestHandler extends Thread {
	
	private static String documentRoot = ""; //classpath에서 찾아야 ./webapp 말고 실행했을때 jar파일 로딩 될때 그 위치 로딩될때 세팅하는게 좋음
	static {
		//인스턴스에서 class접근 getClass 클래스 직접 접근class 컴파일 빌드할때 classpath위치로 간다
		//static 블록
		documentRoot = RequestHandler.class.getClass().getResource("/webapp").getPath();
	}
	private Socket socket;

	public RequestHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			// get IOStream
			OutputStream outputStream = socket.getOutputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// logging Remote Host IP Address & Port
			InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
			consoleLog("connected from " + inetSocketAddress.getAddress().getHostAddress() + ":"
					+ inetSocketAddress.getPort());

			String request = null;

			while (true) {
				String line = br.readLine();
				// 브라우저가연결끊으면
				if (line == null) {
					break;
				}

				// header만 읽음
				if ("".equals(line)) {
					break;
				}

				if (request == null) {
					request = line;
					break;
				}
			}

			// consoleLog(request);
			String[] tokens = request.split(" ");
			if ("GET".equals(tokens[0])) {
				consoleLog("request:" + request);
				responseStaticResource(outputStream, tokens[1], tokens[2]);
			} else {
				// POST, PUT, DELETE 명령은 무시
				consoleLog("bad request:" + request);
				response400Error(outputStream,tokens[2]);
				

			}

		} catch (Exception ex) {
			consoleLog("error:" + ex);
		} finally {
			// clean-up
			try {
				if (socket != null && socket.isClosed() == false) {
					socket.close();
				}

			} catch (IOException ex) {
				consoleLog("error:" + ex);
			}
		}
	}

	private void response400Error(OutputStream outputStream, String protocol) throws IOException {
		// TODO Auto-generated method stub
		
		File file = new File("documentRoot/error/400.html");
		
		byte[] body = Files.readAllBytes(file.toPath());
		String contentType = Files.probeContentType(file.toPath());

		// 응답
		outputStream.write((protocol + "400 BAD REQUEST\r\n").getBytes("UTF-8"));
		outputStream.write(("Content-Type : " + contentType + "; charset=utf-8\r\n").getBytes("UTF-8"));
		outputStream.write("\r\n".getBytes());
		outputStream.write(body);

	}

	private void responseStaticResource(OutputStream outputStream, String url, String protocol) throws IOException {
		// TODO Auto-generated method stub

		if ("/".equals(url)) {
			url = "/index.html";
		}

		File file = new File("documentRoot" + url); //./webapp이 안맞음

		if (file.exists() == false) {
			 response404Error(outputStream,protocol);
			consoleLog("File Not Found:" + url);
			
		
		}

		// new io
		byte[] body = Files.readAllBytes(file.toPath());
		String contentType = Files.probeContentType(file.toPath());

		// 응답
		outputStream.write((protocol + "200 OK\r\n").getBytes("UTF-8"));
		outputStream.write(("Content-Type : " + contentType + "; charset=utf-8\r\n").getBytes("UTF-8"));
		outputStream.write("\r\n".getBytes());
		outputStream.write(body);

	}

	private void response404Error(OutputStream outputStream, String protocol) throws IOException {
		// TODO Auto-generated method stub
		
		File file = new File("documentRoot/error/404.html");
		
		//404페이지 없을때
		//400은 내가 요청하지 않은 PUT/DELTE 호출할때
		// new io
				byte[] body = Files.readAllBytes(file.toPath());
				String contentType = Files.probeContentType(file.toPath());

				// 응답
				outputStream.write((protocol + "404 NOT FOUND\r\n").getBytes("UTF-8"));
				outputStream.write(("Content-Type : " + contentType + "; charset=utf-8\r\n").getBytes("UTF-8"));
				outputStream.write("\r\n".getBytes());
				outputStream.write(body);
		
	}

	public void consoleLog(String message) {
		System.out.println("[RequestHandler#" + getId() + "] " + message);
	}
}
