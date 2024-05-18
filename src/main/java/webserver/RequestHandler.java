package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);
            
            // 입력값을 한 줄 단위로 읽기
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            String line = br.readLine();
            log.info("inputStream content : {}", line);
            
            
            // 아무것도 입력하지 않은 경우
        	String sep = File.separator;
        	String Uri = String.join(sep, line.split(" ")[1].split("/")); // URI값
        	log.info("URI : {}", Uri);
        	
        	
        	if(Uri.equals("")) {
        		byte[] body = "Hello World".getBytes();
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
        	}
        	
        	
        	// 뭐라도 입력한 경우
        	String filePath = "." + sep + "webapp" + Uri;
        	log.info("filePath : {}", filePath);
            	
        	
        	File file = new File(filePath);
        	if(file.exists()) {
        		byte[] body = Files.readAllBytes(file.toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
        	}else {
        		// 입력한 url의 리소스가 존재하지 않는 경우
              byte[] body = "404 error".getBytes();
              response200Header(dos, body.length);
              responseBody(dos, body);
        	}

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
