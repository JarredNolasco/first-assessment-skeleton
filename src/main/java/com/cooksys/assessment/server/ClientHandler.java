package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	// client 
	private Socket socket;
	private ServerSocket ServerSocket;
	
	private static final ConcurrentMap<String, Object> test = new ConcurrentHashMap<String, Object>();

	
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
				String raw = reader.readLine(); //reads line with javascript
				Message message = mapper.readValue(raw, Message.class);
				
				//log.info(message.getContents());
				switch (message.getCommand()) {		// where commands are handled 
					case "connect":
						
						log.info("user <{}> connected", message.getUsername());
						test.put(message.getUsername(), socket);
						
							
							for (Object value : test.values()) {
								PrintWriter writer6 = new PrintWriter(new OutputStreamWriter(((Socket) value).getOutputStream())); // Need new writers for all the different connects 
								//message.setContents("Testing Broadcast");
								//log.info(message.getUsername());
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								message.setTimestamp(timestamp.toString());
								message.setContents("connect");
								log.info(timestamp.toString());
								String response = mapper.writeValueAsString(message);
								writer6.write(response);
								writer6.flush();
							}
							
						
						
						
						
						break;
					case "disconnect":
						
						log.info("user <{}> disconnected", message.getUsername());
						test.remove(message.getUsername());					
						//log.info("Size of Map " + test.size());
						this.socket.close();
						
						for (Object value : test.values()) {
							PrintWriter writer7 = new PrintWriter(new OutputStreamWriter(((Socket) value).getOutputStream()));
							//message.setContents("Testing Broadcast");
							//log.info(message.getUsername());
							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							message.setTimestamp(timestamp.toString());
							message.setContents("disconnect");
							//message.setUsername(username);
							String response = mapper.writeValueAsString(message);
							writer7.write(response);
							writer7.flush();
						}
						
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message); // how to read things from server 
						//log.info(response);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						for (Object value : test.values()) {
							PrintWriter writer2 = new PrintWriter(new OutputStreamWriter(((Socket) value).getOutputStream()));
							//message.setContents("Testing Broadcast");
							log.info(message.getContents());
							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							message.setTimestamp(timestamp.toString());
							
							String response2 = mapper.writeValueAsString(message);
							writer2.write(response2);
							writer2.flush();
						}
						break;
					case "@":
						String whoToWhisperTo = message.getContents();
						//log.info(whoToWhisperTo.length()+""+whoToWhisperTo);
						
						
						for (String key : test.keySet()) {
							//log.info(key + ""+ test.get(key));
						//	log.info(key.length()+""+ key);
							if (key.equals(whoToWhisperTo))
							{
								PrintWriter writer2 = new PrintWriter(new OutputStreamWriter(((Socket) test.get(key)).getOutputStream()));
								//message.setContents("Testing Broadcast");
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								message.setTimestamp(timestamp.toString());
								log.info(message.getContents());
								String response2 = mapper.writeValueAsString(message);
								writer2.write(response2);
								writer2.flush();
								
								
							}
						}
						
						
						break;
					case "users":
						String listOfUsers ="";
						for (String key : test.keySet()) {
						    //log.info(key+" " + test.get(key));
							listOfUsers = listOfUsers + key + "\n";
							log.info(listOfUsers);
						    
						    // figure out how to output this to the javascript
						}
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						message.setTimestamp(timestamp.toString());
						message.setContents(listOfUsers);
						log.info(listOfUsers);
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
