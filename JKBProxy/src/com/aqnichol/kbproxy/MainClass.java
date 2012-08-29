package com.aqnichol.kbproxy;

public class MainClass {

	public static void main (String[] args) {
		int port = 0;
		if (args.length != 1) {
			/*System.err.println("Usage: command <port>");
			return;*/
			port = 1337;
		} else {
			port = Integer.parseInt(args[0]);			
		}
		
		System.out.println("Attempting to listen on port " + port);
		ClientDirector director = new ClientDirector(port);
		director.serverMain();
	}
	
}
