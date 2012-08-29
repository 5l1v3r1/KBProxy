package com.aqnichol.kbproxy.clientpacket;

import com.aqnichol.kbproxy.validation.ValidateException;

import java.nio.ByteBuffer;
import java.util.Map;

public class TagPacket extends ClientPacket {

	private ByteBuffer tag = null;
	
	public TagPacket(Map<String, Object> packet) {
		super(packet);
	}
	
	@Override
	public void validate() throws ValidateException {
		if (!packet.containsKey("tag")) {
			throw new ValidateException("Missing `tag` argument");
		}
		Object tagObj = packet.get("tag");
		if (!(tagObj instanceof ByteBuffer)) {
			throw new ValidateException("Expecteg `tag` as a ByteBuffer");
		}
		tag = (ByteBuffer)tagObj;
	}
	
	public ByteBuffer getTag() {
		return tag;
	}

}
