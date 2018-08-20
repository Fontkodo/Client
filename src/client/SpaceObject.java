package client;

import java.io.IOException;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

class SpaceObject {
	String imgURL;
	Velocity vel;
	Point2D loc;
	double currentRotation;
	double rotvel;
	long timestamp;
	String userid;
	double scale;
	Image img = null;
	long score;
	long photonCount;
	double fuel;
	long shieldLevel;
	long highScore;
	long lastPredictiveTurn;
	
	Point2D getLoc() {
		long elapsed = System.currentTimeMillis() - timestamp;
		double newX = loc.getX() + vel.x*elapsed;
		double newY = loc.getY() + vel.y*elapsed;
		return new Point2D(newX, newY);
	}
	
	double getRot() {
		long elapsed = System.currentTimeMillis() - timestamp;
		return -(currentRotation + rotvel*elapsed);
	}
	
	void draw(GraphicsContext gc) throws IOException {
		if (img == null) {
			img = ImageFactory.getImage(imgURL);
		}
		Point2D newLoc = getLoc();
		gc.translate(newLoc.getX(), newLoc.getY());
		gc.rotate(getRot()*180/Math.PI);
		gc.scale(this.scale, this.scale);
		boolean flash = ((System.currentTimeMillis() / 300) % 3) > 1;
		if(flash && userid.equals(""+ControlEvent.getClientID())) {
			double radius = Math.max(img.getWidth(), img.getHeight());
			//gc.strokeArc(x, y, w, h, startAngle, arcExtent, closure);
			gc.setStroke(Color.YELLOW);
			gc.strokeArc(-img.getWidth()/2, -img.getHeight()/2, radius, radius, 0.0, 360.0, ArcType.ROUND);
		}
		gc.drawImage(img, -img.getWidth()/2, -img.getHeight()/2);
	}
}
