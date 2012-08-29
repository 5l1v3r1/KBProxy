package com.aqnichol.kbproxy.clientpacket;

import com.aqnichol.kbproxy.validation.ValidateException;
import java.util.Map;

public class TransmitPacket extends ClientPacket {

	public TransmitPacket(Map<String, Object> map) {
		super(map);
	}
	
	@Override
	public void validate() throws ValidateException {
		if (!packet.containsKey("object")) {
			throw new ValidateException("Missing `object` argument");
		}
	}
	
	public Object getObject() {
		return packet.get("object");
	}

}
