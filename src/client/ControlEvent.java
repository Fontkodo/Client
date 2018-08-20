package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

import org.json.simple.JSONObject;

public class ControlEvent {
	static private String clientID;

	static String getClientID() {
		final String fileName = "/tmp/blasteroids.id.txt";
		if (clientID != null) {
			return clientID;
		}
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String ln = br.readLine();
			br.close();
			clientID = "" + Long.parseLong(ln);
			return clientID;
		} catch (Exception e) {
		}
		clientID = "" + (new Random().nextLong());
		try {
			FileWriter fw = new FileWriter(fileName);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(clientID);
			pw.close();
		} catch (Exception anything) {
			System.err.println("MAJOR problem " + anything.getMessage());
			System.exit(1);
			;
		}
		return clientID;
	}

	JSONObject toJSONObject() {
		JSONObject ob = new JSONObject();
		ob.put("userid", getClientID());
		ob.put("action", "connect");
		return ob;
	}
}

class ConnectionEvent extends ControlEvent {
	JSONObject toJSONObject() {
		JSONObject ob = super.toJSONObject();
		ob.put("action", "connect");
		return ob;
	}
}

class DisconnectionEvent extends ControlEvent {
	JSONObject toJSONObject() {
		JSONObject ob = super.toJSONObject();
		ob.put("action", "disconnect");
		return ob;
	}
}

class LeftEvent extends ControlEvent {
	JSONObject toJSONObject() {
		JSONObject ob = super.toJSONObject();
		ob.put("action", "left");
		return ob;
	}
}

class RightEvent extends ControlEvent {
	JSONObject toJSONObject() {
		JSONObject ob = super.toJSONObject();
		ob.put("action", "right");
		return ob;
	}
}

class ForwardEvent extends ControlEvent {
	JSONObject toJSONObject() {
		JSONObject ob = super.toJSONObject();
		ob.put("action", "forward");
		return ob;
	}
}

class BackwardEvent extends ControlEvent {
	JSONObject toJSONObject() {
		JSONObject ob = super.toJSONObject();
		ob.put("action", "backward");
		return ob;
	}
}

class FireEvent extends ControlEvent {
	JSONObject toJSONObject() {
		JSONObject ob = super.toJSONObject();
		ob.put("action", "fire");
		return ob;
	}
}