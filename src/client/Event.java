package client;

import org.json.simple.JSONObject;

public abstract class Event {
	abstract JSONObject toJSONObject();
}

class ConnectionEvent extends Event {

	@Override
	JSONObject toJSONObject() {
		JSONObject ob = new JSONObject();
		ob.put("dog", "fido");
		ob.put("cow", "elsie");
		return ob;
	}
	
}