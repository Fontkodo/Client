package client;

import java.io.IOException;

import javafx.geometry.*;
import javafx.scene.canvas.*;
import javafx.scene.image.*;

class SpaceObject {
	String imgURL;
	Velocity vel;
	Point2D loc;
	double currentRotation;
	double rotvel;
	long timestamp;
	Image img = null;
	
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
		gc.scale(1, 1);
		gc.drawImage(img, -img.getWidth()/2, -img.getHeight()/2);
	}
}
