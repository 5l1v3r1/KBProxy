package com.aqnichol.kbproxy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class ClientDirector {

	private int port = 0;
	private Thread thread = null;
	private ArrayList<Client> clients = null;

	public ClientDirector(int port) {
		this.port = port;
		clients = new ArrayList<Client>();
	}

	public ArrayList<Client> getClients() {
		return clients;
	}

	// use to run multiple servers or something
	public void dispatchServer() {
		thread = new Thread(new Runnable() {
			public void run() {
				serverMain();
			}
		});
		thread.start();
	}

	public void serverMain() {
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		// continuously accept new client connections
		try {
			while (true) {
				Socket clientSocket = null;
				clientSocket = server.accept();
				handleClient(clientSocket);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleClient(Socket s) {
		System.out.println("Got connection: " + s.getRemoteSocketAddress().toString());
		Client c;
		try {
			c = new Client(s, this);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		synchronized (clients) {
			clients.add(c);
		}
		new Thread(c).start();
	}
	
	// registration
	
	public void registerClient(Client client, ByteBuffer tag) {
		synchronized (clients) {
			if (client.getTag() != null) {
				unregisterClient(client);
			}
			
			// find a matching client and/or deny the tag
			Client otherClient = null;
			for (int i = 0; i < clients.size(); i++) {
				Client aClient = clients.get(i);
				if (aClient == client || aClient.getTag() == null) continue;
				if (aClient.getTag().equals(tag)) {
					if (otherClient == null) {
						otherClient = aClient;
					} else {
						client.sendTagExists(tag);
						return;
					}
				}
			}
			
			client.setTag(tag);
			if (otherClient != null) {
				// connection established
				client.setPeer(otherClient);
				otherClient.setPeer(client);
				otherClient.sendConnectionEstablished();
				client.sendConnectionEstablished();
			} else {
				// tag owned
				client.sendTagOwned();
			}
		}
	}
	
	public void unregisterClient(Client client) {
		synchronized (clients) {
			client.setTag(null);
			if (client.getPeer() != null) {
				Client peer = client.getPeer();
				peer.setPeer(null);
				client.setPeer(null);
				peer.sendTagOwned();
			}
		}
	}
	
	public void destroyClient(Client client) {
		synchronized (clients) {
			if (client.getPeer() != null) {
				unregisterClient(client);
			}
			clients.remove(client);
			client.close();
		}
	}

}
