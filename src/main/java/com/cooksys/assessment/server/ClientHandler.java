package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;


public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	// client 
	private Socket socket;
	
	
	private static final ConcurrentMap<String, Object> userMap = new ConcurrentHashMap<String, Object>();

	
	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}
				
	public void run() {
		try {
			

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				switch (message.getCommand()) {		
					case "connect":
						
						log.info("user <{}> connected", message.getUsername());
						userMap.put(message.getUsername(), socket);
					
							for (Object value : userMap.values()) {
								PrintWriter connectWriter = new PrintWriter(new OutputStreamWriter(((Socket) value).getOutputStream())); 
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								message.setTimestamp(timestamp.toString());
								message.setContents("connect");
								log.info(timestamp.toString());
								String response = mapper.writeValueAsString(message);
								connectWriter.write(response);
								connectWriter.flush();
							}
						
						break;
					case "disconnect":
						
						log.info("user <{}> disconnected", message.getUsername());
						userMap.remove(message.getUsername());					
						this.socket.close();
						
						for (Object value : userMap.values()) {
							PrintWriter disconnectWriter = new PrintWriter(new OutputStreamWriter(((Socket) value).getOutputStream()));
							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							message.setTimestamp(timestamp.toString());
							message.setContents("disconnect");
							String response = mapper.writeValueAsString(message);
							disconnectWriter.write(response);
							disconnectWriter.flush();
						}
						
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						message.setTimestamp(timestamp.toString());
						String response = mapper.writeValueAsString(message); 
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						for (Object value : userMap.values()) {
							PrintWriter broadcastWriter = new PrintWriter(new OutputStreamWriter(((Socket) value).getOutputStream()));
							log.info(message.getContents());
							Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
							message.setTimestamp(timestamp2.toString());
							
							String response2 = mapper.writeValueAsString(message);
							broadcastWriter.write(response2);
							broadcastWriter.flush();
						}
						break;
					case "@":
						String mystring = message.getContents();
						String arr[] = mystring.split(" ", 2);
						String firstWord = arr[0];   
						String theRest = arr[1];
						String whoToWhisperTo = firstWord;	
						for (String key : userMap.keySet()) {
							if (key.equals(whoToWhisperTo))
							{
								PrintWriter atWriter = new PrintWriter(new OutputStreamWriter(((Socket) userMap.get(key)).getOutputStream()));
								
								Timestamp timestamp3 = new Timestamp(System.currentTimeMillis());
								message.setTimestamp(timestamp3.toString());
								log.info(message.getContents());
								message.setContents(theRest);
								String response2 = mapper.writeValueAsString(message);
								atWriter.write(response2);
								atWriter.flush();
							}
						}
						break;
					case "users":
						String listOfUsers ="";
						for (String key : userMap.keySet()) {
							listOfUsers = listOfUsers + "\n" + key  ;
							log.info(listOfUsers);
						}
						Timestamp timestamp4 = new Timestamp(System.currentTimeMillis());
						message.setTimestamp(timestamp4.toString());
						message.setContents(listOfUsers);
						String response2 = mapper.writeValueAsString(message);
						writer.write(response2);
						writer.flush();
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	

}
