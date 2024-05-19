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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controller.Controller;
import util.HttpRequestUtils;
import util.IOUtils;

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
            String requestHttpHeader = getRequestHttpHeader(br);
            
//            // 여기서 get방식과 post방식 분리하는게 좋을 것 같음
            String[] content = requestHttpHeader.split(" ");
            String method = content[0];
            
            String uri = content[1]; // URI값
            log.info("URI : {}", uri);
            
            if(method.equalsIgnoreCase("post")) {
            	// post방식일 경우
            	
            	String body = getRequestHttpBody(br, requestHttpHeader.length());
            	Map<String, String> queryString = HttpRequestUtils.parseQueryString(body);
            	log.info("queryString content : {}",queryString);
            	Controller.createUser(queryString);
            	
            	return;
            }
            else if(method.equalsIgnoreCase("get")) {
            	// 아무것도 입력하지 않은 경우
            	String sep = File.separator;
            	
            	// 아무것도 입력하지 않은 경우
            	if(uri.equals("")) {
	        		byte[] body = "Hello World".getBytes();
	        		response200Header(dos, body.length);
	        		responseBody(dos, body);
	        		return;
            	}
            	
            	// 뭐라도 입력한 경우
            	String requestPath = "./" + "webapp" + uri;
            	log.info("requestPath : {}", requestPath);
            	
            	// path안에 ?가 존재하는 경우 => 쿼리스트링이 있는 경우
            	if(uri.contains("?")) {
        		// 1. url과 쿼리스트링을 분리한다.
        		int index = uri.indexOf("?");
        		String params = uri.substring(index+1);
        		String path = uri.substring(0,index);
        		Map<String, String> queryString = HttpRequestUtils.parseQueryString(params);
        		log.info("path : {}",path);
        		log.info("queryString content : {}",queryString);
        		
	        		if(path.equals("/user/create")) {
	        			
	        			// 계정생성
	        			Controller.createUser(queryString);
	        		}
        		}
            	
            	
            	File file = new File(requestPath);
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
    
    private String getRequestHttpHeader(BufferedReader br) throws IOException {
    	
        
        StringBuffer httpHeader = new StringBuffer("");
        String str;
        while(!(str = br.readLine()).equals("")) {
        	httpHeader.append(str);
    	}
        log.info("httpHeader content : {}", httpHeader.toString());
        
        return httpHeader.toString();
    }
    
    private String getRequestHttpBody(BufferedReader br, int contentLength) throws IOException {
    	String body = IOUtils.readData(br, contentLength);
    	log.info("httpBody content : {}",body);
    	return body;
    }
}
