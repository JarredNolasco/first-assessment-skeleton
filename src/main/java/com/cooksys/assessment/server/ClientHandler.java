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
				// Most of the changes will happen here. 
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
						//log.info(message.getUsername());
						log.info("Size of Map " + test.size());
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						test.remove(message.getUsername());
						log.info("Size of Map " + test.size());
						this.socket.close();
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
							String response2 = mapper.writeValueAsString(message);
							writer2.write(response2);
							writer2.flush();
						}
						break;
					case "@":
						String whoToWhisperTo = message.getContents();
						log.info(whoToWhisperTo);
						for (String key : test.keySet()) {
							//log.info(key + ""+ test.get(key));
							log.info(arg0);
							if (whoToWhisperTo == key)
							{
								log.info("You have whispered to the right person");
							}
						}
						
						
						break;
					case "getall":
						for (String key : test.keySet()) {
						    log.info(key+" " + test.get(key));
						    
						    // figure out how to output this to the javascript
						}
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	

}
