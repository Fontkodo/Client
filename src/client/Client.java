package client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fontkodo.netstring.NetString;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

final class ClientTimer extends AnimationTimer {

	Client client;

	ClientTimer(Client client) {
		this.client = client;
	}

	@Override
	public void handle(long now) {
		try {
			this.client.executeFrame(now);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

public class Client extends Application {

	static final Dimension2D dimension = new Dimension2D(Screen.getPrimary().getBounds().getWidth(),
			Screen.getPrimary().getBounds().getHeight());

	ClientTimer timer;
	Canvas canvas;
	Image background;
	static List<SpaceObject> spaceObjects = new ArrayList<SpaceObject>();

	static class Conversation implements Runnable {

		@Override
		public void run() {
			JSONParser p = new JSONParser();
			while (true) {
				try {
					Socket s = new Socket("localhost", 8353);
					while (true) {
						String txt = NetString.readString(s.getInputStream());
						System.out.println(txt);
						JSONObject ob = (JSONObject) p.parse(txt);
						JSONArray a = (JSONArray) ob.get("SpaceObjects");
						List<SpaceObject> loso = new ArrayList<SpaceObject>();
						for (Object o : a) {
							JSONObject jo = (JSONObject) o;
							SpaceObject so = new SpaceObject();
							so.imgURL = (String) jo.get("imgURL");
							JSONObject tvo = (JSONObject) jo.get("vel");
							so.vel = new Velocity((double) tvo.get("x"), (double) tvo.get("y"));
							JSONObject tlo = (JSONObject) jo.get("loc");
							so.loc = new Point2D((double) tlo.get("x"), (double) tlo.get("y"));
							so.rotvel = (double) jo.get("rotvel");
							so.timestamp = (long) jo.get("timestamp");
							loso.add(so);
						}
						spaceObjects = loso;
					}
				} catch (IOException | ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public static void main(String[] args) {
		Thread t = new Thread(new Conversation());
		t.start();
		launch(args);
	}

	void executeFrame(long now) throws IOException {
		double width = canvas.getWidth();
		double height = canvas.getHeight();
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, width, height);
		gc.drawImage(background, 0, 0);
		for (SpaceObject so : spaceObjects) {
			gc.save();
			so.draw(gc);
			gc.restore();
		}
		gc.setFill(Color.WHITE);
	}

	boolean turningLeft = false;
	boolean turningRight = false;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Super GameFramework");
		primaryStage.setFullScreen(true);
		Group root = new Group();
		canvas = new Canvas(dimension.getWidth(), dimension.getWidth());
		canvas.setFocusTraversable(true);
		canvas.requestFocus();
		root.getChildren().add(canvas);
		primaryStage.setScene(new Scene(root));
		primaryStage.setResizable(false);
		primaryStage.show();
		if (timer == null) {
			background = ImageFactory.getImage("http://blasteroids.prototyping.site/assets/images/backgrounds/mars.jpg");
			timer = new ClientTimer(this);
			timer.start();
		}
	}

}
