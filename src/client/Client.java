package client;

import java.io.*;
import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import fontkodo.netstring.NetString;

public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException, ParseException {
		Socket socket = new Socket("localhost", 9999);
		JSONObject ob = new JSONObject();
		ob.put("Dog", 123);
		ob.put("Cat", "Meow");
		ob.put("Spinning", "A Good Trick");
		OutputStreamWriter obw = new OutputStreamWriter(socket.getOutputStream());
		obw.write(ob.toString());
		obw.flush();
		InputStream ipr = new BufferedInputStream(socket.getInputStream());
		String content = NetString.readString(ipr);
		JSONParser p = new JSONParser();
		System.out.println(p.parse(content));
		obw.close();
		socket.close();

	}

}
