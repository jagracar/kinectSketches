package kinectScanner;

import java.awt.Rectangle;

import gab.opencv.OpenCV;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * This class defines a box inside of which the Kinect scans are taken
 *
 * Uses OpenCV 2 to center the box in the first detected face. If you don't have OpenCV installed in your computer, you
 * should comment the lines inside the centerInFace() function, but centerInFace() still needs to be defined.
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class ScanBox {

	/**
	 * The box center
	 */
	public PVector center;

	/**
	 * The box size
	 */
	public float size;

	/**
	 * Constructs a scan box object
	 * 
	 * @param center the box center
	 * @param size the box size
	 */
	public ScanBox(PVector center, float size) {
		this.center = center.copy();
		this.size = size;
	}

	/**
	 * Checks if a given point is inside the box
	 * 
	 * @param point the point to check
	 * @return true if the point is inside the box
	 */
	public boolean isInside(PVector point) {
		float halfSize = size / 2;

		return Math.abs(point.x - center.x) < halfSize && Math.abs(point.y - center.y) < halfSize
				&& Math.abs(point.z - center.z) < halfSize;
	}

	/**
	 * Draws the box on the screen
	 * 
	 * @param p the parent Processing applet
	 * @param color the color to use
	 */
	public void draw(PApplet p, int color) {
		p.pushStyle();
		p.noFill();
		p.stroke(color);
		p.strokeWeight(1);
		p.pushMatrix();
		p.translate(center.x, center.y, center.z);
		p.line(-size, 0, 0, size, 0, 0);
		p.line(0, -size, 0, 0, size, 0);
		p.line(0, 0, -size, 0, 0, size);
		p.box(size, size, size);
		p.popMatrix();
		p.popStyle();
	}

	/**
	 * Detects a face inside the provided Kinect points and centers the box on the face position
	 * 
	 * @param p the parent Processing applet
	 * @param kp the Kinect points that should be used to detect the face
	 * @return true if a face was detected and the box position was centered on the face
	 */
	public boolean centerInFace(PApplet p, KinectPoints kp) {
		// Create an image with only the visible points color information
		PImage img = p.createImage(kp.width, kp.height, PApplet.RGB);
		img.loadPixels();

		for (int i = 0; i < kp.nPoints; i++) {
			if (kp.visibilityMask[i]) {
				img.pixels[i] = kp.colors[i];
			}
		}

		img.updatePixels();

		// Initialize OpenCV
		OpenCV opencv = new OpenCV(p, img);
		opencv.loadCascade(OpenCV.CASCADE_FRONTALFACE);

		// Detect faces in the image
		Rectangle[] faces = opencv.detect();
		System.out.println("Center in face: " + faces.length + " face detected");

		// Check if a face was detected
		boolean boxCentered = false;

		if (faces.length > 0) {
			// Get the first face 3D position
			Rectangle face = faces[0];
			int x = face.x + (int) (face.width / 2);
			int y = face.y + (int) (face.height / 2);
			int index = x + y * kp.width;

			if (kp.visibilityMask[index]) {
				// Center the box on the face with a small offset in the z direction
				center.set(kp.points[index]);
				center.add(0, 0, 100);
				System.out.println("Center in face: Done (centered in the first face)");
				boxCentered = true;
			} else {
				System.out.println("Center in face: Invalid point. Try again");
			}
		} else {
			System.out.println("Center in face: Nothing done. Try again");
		}

		return boxCentered;
	}
}