package controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import webserver.RequestHandler;

public class Controller {

	private static final Logger log = LoggerFactory.getLogger(Controller.class);
	
	public static void createUser(Map<String, String> queryString) throws UnsupportedEncodingException {
		
		String userId = URLDecoder.decode(queryString.get("userId"),"utf-8");
		String password = URLDecoder.decode(queryString.get("password"),"utf-8");
		String name = URLDecoder.decode(queryString.get("name"),"utf-8");
		String email = URLDecoder.decode(queryString.get("email"),"utf-8");
		
		log.info("userId : {}",userId);
		log.info("password : {}",password);
		log.info("name : {}",name);
		log.info("email : {}",email);
		
		User user = new User(userId, password, name, email);
		log.info("createUser complete");
		
	}
}
