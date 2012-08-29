package com.aqnichol.kbproxy.clientpacket;

import com.aqnichol.kbproxy.validation.*;

public class PacketReader {

	public static ClientPacket processClientPacket(Object object) throws ValidateException {
		ClientValidator validator = new ClientValidator(object);
		validator.validate();
		
		String type = validator.getType();
		ClientPacket packet = null;
		
		if (type.equals("tag")) {
			packet = new TagPacket(validator.getMap());
		} else if (type.equals("transmit")) {
			packet = new TransmitPacket(validator.getMap());
		} else if (type.equals("unregister")) {
			packet = new UnregisterPacket(validator.getMap());
		} else {
			throw new ValidateException("Invalid `type` argument: " + type);
		}
		
		packet.validate();
		return packet;
	}
	
}
