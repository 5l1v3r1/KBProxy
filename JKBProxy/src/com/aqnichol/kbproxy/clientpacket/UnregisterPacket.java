package com.aqnichol.kbproxy.clientpacket;

import com.aqnichol.kbproxy.validation.ValidateException;

import java.util.Map;

public class UnregisterPacket extends ClientPacket {

	public UnregisterPacket(Map<String, Object> map) {
		super(map);
	}

	@Override
	public void validate() throws ValidateException {
	}

}
