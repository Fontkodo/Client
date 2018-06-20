package client;

import java.io.*;
import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException, ParseException {
		Socket socket = new Socket("localhost", 8353);
		JSONObject ob = new JSONObject();
		ob.put("Dog", 123);
		ob.put("Cat", "Meow");
		ob.put("Spinning", "A Good Trick");
		OutputStreamWriter obw = new OutputStreamWriter(socket.getOutputStream());
		obw.write(ob.toString());
		obw.flush();
		InputStreamReader ipr = new InputStreamReader(socket.getInputStream());
		char [] stuff = new char[100_000];
		int n = ipr.read(stuff);
		CharArrayReader car = new CharArrayReader(stuff, 0, n);
		JSONParser p = new JSONParser();
		System.out.println(p.parse(car));
		obw.close();
		socket.close();

	}

}
