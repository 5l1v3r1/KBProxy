package com.aqnichol.kbproxy;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.aqnichol.kbproxy.clientpacket.ClientPacket;
import com.aqnichol.kbproxy.clientpacket.PacketReader;
import com.aqnichol.kbproxy.clientpacket.TagPacket;
import com.aqnichol.kbproxy.clientpacket.TransmitPacket;
import com.aqnichol.kbproxy.clientpacket.UnregisterPacket;
import com.aqnichol.kbproxy.validation.ValidateException;
import com.aqnichol.keyedbits.encode.*;
import com.aqnichol.keyedbits.value.UnmatchedTypeException;
import com.aqnichol.keyedbits.decode.*;

public class Client implements Runnable {

	private ValueDecoder decoder = null;
	private ValueEncoder encoder = null;
	private ByteBuffer tag = null;
	private Client peer = null;
	private ClientDirector director = null;
	private Socket socket = null;
	private Object propertyLock = new Object();
	
	public Client(Socket socket, ClientDirector director) throws IOException {
		this.socket = socket;
		this.director = director;
		InputStream input = socket.getInputStream();
		OutputStream output = socket.getOutputStream();
		
		FileDecodeStream decodeStream = new FileDecodeStream(input);
		FileEncodeStream encodeStream = new FileEncodeStream(output);
		DecodeStreamReader reader = new DecodeStreamReader(decodeStream);
		EncodeStreamWriter writer = new EncodeStreamWriter(encodeStream);
		decoder = new ValueDecoder(reader);
		encoder = new ValueEncoder(writer);
	}
	
	public void close() {
		try {
			socket.getInputStream().close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		try {
			socket.getOutputStream().close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		try {
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public ByteBuffer getTag() {
		synchronized (propertyLock) {
			return tag;
		}
	}
	
	public void setTag(ByteBuffer tag) {
		synchronized (propertyLock) {
			this.tag = tag;
		}
	}
	
	public Client getPeer() {
		synchronized (propertyLock) {
			return peer;
		}
	}
	
	public void setPeer(Client peer) {
		synchronized (propertyLock) {
			this.peer = peer;
		}
	}
	
	public synchronized void sendPacket(Map<String, Object> packet) {
		try {
			encoder.encodeObject(packet);
		} catch (EncodeStreamWriteError e1) {
			e1.printStackTrace();
		}
	}
	
	public void sendError(String message, int code) {
		HashMap<String, Object> packet = new HashMap<String, Object>();
		packet.put("type", "error");
		packet.put("message", message);
		packet.put("error", new Integer(code));
		sendPacket(packet);
	}
	
	public void sendTagOwned() {
		HashMap<String, Object> packet = new HashMap<String, Object>();
		packet.put("type", "owned");
		packet.put("tag", this.getTag());
		sendPacket(packet);
	}
	
	public void sendConnectionEstablished() {
		HashMap<String, Object> packet = new HashMap<String, Object>();
		packet.put("type", "connected");
		packet.put("tag", this.getTag());
		sendPacket(packet);
	}
	
	public void sendTagExists(ByteBuffer aTag) {
		HashMap<String, Object> packet = new HashMap<String, Object>();
		packet.put("type", "taken");
		packet.put("tag", aTag);
		sendPacket(packet);
	}
	
	public void run() {
		try {
			while (true) {
				Object object = decoder.decodeNextValue().getObject();
				if (object == null) break;
				ClientPacket packet = PacketReader.processClientPacket(object);
				handlePacket(packet);
			}
		} catch (UnmatchedTypeException e) {
			e.printStackTrace();
		} catch (DecodeStreamReadError e1) {
			e1.printStackTrace();
		} catch (ValidateException e2) {
			e2.printStackTrace();
		}
		director.destroyClient(this);
		System.out.println("Client connection terminated.");
	}
	
	private void handlePacket(ClientPacket packet) {
		if (packet instanceof TagPacket) {
			TagPacket tag = (TagPacket)packet;
			director.registerClient(this, tag.getTag());
		} else if (packet instanceof TransmitPacket) {
			TransmitPacket transmit = (TransmitPacket)packet;
			HashMap<String, Object> incoming = new HashMap<String, Object>();
			incoming.put("type", "incoming");
			incoming.put("object", transmit.getObject());
			
			Client other = this.getPeer();
			if (other != null) {
				other.sendPacket(incoming);
			} else {
				this.sendError("Remote client not connected.", 2);
			}
		} else if (packet instanceof UnregisterPacket) {
			if (this.getTag() != null) {
				director.unregisterClient(this);
			}
		}
	}
	
}
