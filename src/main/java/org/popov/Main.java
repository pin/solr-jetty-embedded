package org.popov;

import org.popov.WebServer;

public class Main {
	public static void main(String... args) throws Exception {
		new Main().start();
	}
	
	private WebServer server;
	
	public Main() {
		server = new WebServer(8983);        
	}
	
	public void start() throws Exception {
		server.start();        
		server.join();
	}
}
