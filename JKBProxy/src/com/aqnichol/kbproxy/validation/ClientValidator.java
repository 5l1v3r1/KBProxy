package com.aqnichol.kbproxy.validation;
import java.util.Map;

public class ClientValidator {

	protected Object object = null;
	protected Map<String, Object> map = null;
	
	public ClientValidator(Object object) {
		this.object = object;
	}
	
	@SuppressWarnings("unchecked")
	public void validate() throws ValidateException {
		if (!(object instanceof Map)) {
			throw new ValidateException("Expected a map input");
		}
		map = (Map<String, Object>)object;
		if (!map.containsKey("type")) {
			throw new ValidateException("Expected a `type` key");
		}
		Object typeObj = map.get("type");
		if (!(typeObj instanceof String)) {
			throw new ValidateException("Expected `type` to be a string.");
		}
	}
	
	public String getType() {
		if (map == null) return null;
		return (String)map.get("type");
	}
	
	public Map<String, Object> getMap() {
		return map;
	}
	
}
