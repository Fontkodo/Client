package client;

import java.io.IOException;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
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

public class Client extends Application{
	
	static final Dimension2D dimension = new Dimension2D(Screen.getPrimary().getBounds().getWidth(),
			Screen.getPrimary().getBounds().getHeight());
	
	ClientTimer timer;
	Canvas canvas;
	Image background;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	void executeFrame(long now) throws IOException{
		double width = canvas.getWidth();
		double height = canvas.getHeight();
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, width, height);
		gc.drawImage(background, 0, 0);
		gc.save();
		gc.restore();
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
			background = new Image("http://blasteroids.prototyping.site/assets/images/backgrounds/mars.jpg");
			timer = new ClientTimer(this);
			timer.start();
		}
	}
	
}
