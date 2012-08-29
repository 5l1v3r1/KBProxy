package com.aqnichol.kbproxy.clientpacket;

import java.util.Map;

import com.aqnichol.kbproxy.validation.ValidateException;

public abstract class ClientPacket {

	protected Map<String, Object> packet;
	
	public ClientPacket(Map<String, Object> packet) {
		this.packet = packet;
	}
	
	public abstract void validate() throws ValidateException;
	
	public Map<String, Object> getPacket() {
		return packet;
	}
	
}
