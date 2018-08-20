package client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

final class ClientTimer extends AnimationTimer {

	Client client;
	long lastFrameNanos;

	ClientTimer(Client client) {
		this.client = client;
		this.lastFrameNanos = 0;
	}

	@Override
	public void handle(long now) {
		if ((now - lastFrameNanos) < 25_000_000) {
			return;
		}
		lastFrameNanos = now;
		try {
			this.client.executeFrame(now);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

public class Client extends Application {

	static Dimension2D serverDimensions = new Dimension2D(Screen.getPrimary().getBounds().getWidth(),
			Screen.getPrimary().getBounds().getHeight());

	static Stage theStage;
	ClientTimer timer;
	static Canvas canvas;
	Image background;
	static List<SpaceObject> spaceObjects = new ArrayList<SpaceObject>();
	static List<String> sounds = new ArrayList<String>();
	static Map<String, Long> highScores = new HashMap<String, Long>();

	static SpaceObject getMyPlayer() {
		for (SpaceObject so : spaceObjects) {
			if (so.userid.equals(ControlEvent.getClientID())) {
				return so;
			}
		}
		return null;
	}
	
	static long millisecondAdjustment;

	static class GameStateReceiver implements Runnable {

		private Socket s;

		GameStateReceiver(Socket s) {
			this.s = s;
		}

		@Override
		public void run() {
			JSONParser p = new JSONParser();
			while (true) {
				try {
					while (true) {
						String txt = NetString.readString(s.getInputStream());
						JSONObject ob = (JSONObject) p.parse(txt);
						JSONObject tempDim = (JSONObject) ob.get("Dimensions");
						{
							long delay = System.currentTimeMillis() - (long) ob.get("currentMillis");
							if(millisecondAdjustment == 0) {
								millisecondAdjustment = delay;
							} else {
								millisecondAdjustment = ((19 * millisecondAdjustment) + delay) / 20;
							}
						}
						serverDimensions = new Dimension2D((long) tempDim.get("Width"), (long) tempDim.get("Height"));
						if (theStage != null) {
							if (Math.abs(serverDimensions.getWidth() - canvas.getWidth()) > 1) {
								canvas.setWidth(serverDimensions.getWidth());
								canvas.setHeight(serverDimensions.getHeight());

								theStage.setWidth(serverDimensions.getWidth());
								theStage.setHeight(serverDimensions.getHeight());
							}
						}
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
							so.currentRotation = (double) jo.get("currentRotation");
							so.rotvel = (double) jo.get("rotvel");
							so.timestamp = (long) jo.get("timestamp") + millisecondAdjustment;
							so.userid = jo.getOrDefault("userid", "0").toString();
							so.scale = (double) jo.get("scale");
							so.score = (long) jo.getOrDefault("score", 0L);
							so.photonCount = (long) jo.getOrDefault("photonCount", 0L);
							so.fuel = (double) jo.getOrDefault("fuel", 0.0);
							so.shieldLevel = (long) jo.getOrDefault("shieldLevel", 0L);
							so.highScore = (long) jo.getOrDefault("highScore", 0L);
							loso.add(so);
						}
						spaceObjects = loso;
						JSONArray audio = (JSONArray) ob.get("Sounds");
						for (Object o : audio) {
							AudioClipFactory.getAudioClip((String) o).play();
						}
					}
				} catch (IOException | ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Socket s = new Socket("localhost", 6081);
		new Thread(new GameStateReceiver(s)).start();
		new Thread(new OutgoingCommands(s)).start();
		outgoingEvents.put(new ConnectionEvent());
		launch(args);
	}

	void executeFrame(long now) throws IOException {
		double width = canvas.getWidth();
		double height = canvas.getHeight();
		GraphicsContext gc = canvas.getGraphicsContext2D();

		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, width, height);
		// gc.drawImage(background, 0, 0);
		for (SpaceObject so : spaceObjects) {
			gc.save();
			so.draw(gc);
			gc.restore();
		}
		gc.setFill(Color.WHITE);
		SpaceObject p = getMyPlayer();
		if (p != null) {
			gc.save();
			gc.scale(1.2, 1.2);
			gc.fillText(String.format(
					"Score: %d\nPhotons: %d\nFuel: %.2f\nShield Level: %d\nHigh Score: %d\nAdjustment: %d\n", p.score,
					p.photonCount, p.fuel, p.shieldLevel, p.highScore, millisecondAdjustment), 10, 20);
			gc.restore();
		}
	}

	boolean turningLeft = false;
	boolean turningRight = false;

	static BlockingQueue<ControlEvent> outgoingEvents = new ArrayBlockingQueue<ControlEvent>(10);

	static class OutgoingCommands implements Runnable {

		private Socket s;

		OutgoingCommands(Socket s) {
			this.s = s;
		}

		@Override
		public void run() {
			while (true) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					do {
						ControlEvent event = outgoingEvents.take();
						JSONObject ob = event.toJSONObject();
						String txt = ob.toJSONString();
						byte[] b = NetString.toNetStringBytes(txt);
						baos.write(b);
						if (event instanceof DisconnectionEvent) {
							Thread.sleep(500);
							System.exit(0);
						}
					} while (outgoingEvents.peek() != null);
					// Don't bother flushing until all pending events are written
					s.getOutputStream().write(baos.toByteArray());
					s.getOutputStream().flush();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Super GameFramework");
		Group root = new Group();
		canvas = new Canvas(serverDimensions.getWidth(), serverDimensions.getWidth());
		canvas.setFocusTraversable(true);
		canvas.requestFocus();
		root.getChildren().add(canvas);
		canvas.setOnKeyPressed(e -> {
			try {
				if (e.getCode().equals(KeyCode.SPACE)) {
					outgoingEvents.put(new FireEvent());
				}
				if (e.getCode().equals(KeyCode.UP)) {
					outgoingEvents.put(new ForwardEvent());
				}
				if (e.getCode().equals(KeyCode.DOWN)) {
					outgoingEvents.put(new BackwardEvent());
				}
				if (e.getCode().equals(KeyCode.LEFT)) {
					outgoingEvents.put(new LeftEvent());
					SpaceObject p = getMyPlayer();
					if (p != null) {
						if(p.lastPredictiveTurn < System.currentTimeMillis() - millisecondAdjustment) {
							p.currentRotation += 5 * Math.PI / 180;
							p.lastPredictiveTurn = System.currentTimeMillis();
						}
					}
				}
				if (e.getCode().equals(KeyCode.RIGHT)) {
					outgoingEvents.put(new RightEvent());
					SpaceObject p = getMyPlayer();
					if (p != null) {
						if(p.lastPredictiveTurn < System.currentTimeMillis() - millisecondAdjustment) {
							p.currentRotation -= 5 * Math.PI / 180;
							p.lastPredictiveTurn = System.currentTimeMillis();
						}
					}
				}
			} catch (Exception thing) {
			}
		});
		primaryStage.setScene(new Scene(root));
		primaryStage.setResizable(true);
		primaryStage.show();
		theStage = primaryStage;
		if (timer == null) {
			background = ImageFactory
					.getImage("http://blasteroids.prototyping.site/assets/images/backgrounds/mars.jpg");
			timer = new ClientTimer(this);
			timer.start();
		}
		primaryStage.setOnCloseRequest(e -> {
			try {
				outgoingEvents.put(new DisconnectionEvent());
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
	}

}
