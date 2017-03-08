package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;

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
				log.info(message.getContents());
				switch (message.getCommand()) {		// where commands are handled 
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						//log.info("test"); logs info into the logger in java log
//						writer.write("ayyy this is a test"); 
//						writer.flush();
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message); // how to read things from server 
						log.info(response);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info(message.getUsername());
						break;
					case "whisper":
						log.info("Whisper To Other Users works");
						break;
					case "getall":
						log.info("Get All Users works");
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
