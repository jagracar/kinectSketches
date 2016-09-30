package jagracar.kinect.util;

import jagracar.kinect.containers.KinectHelper;
import jagracar.kinect.containers.KinectPoints;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * This class defines a box inside of which the Kinect scans are taken
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
	 * @param strokeWeight the stroke weight to use
	 */
	public void draw(PApplet p, int color, float strokeWeight) {
		p.pushStyle();
		p.noFill();
		p.stroke(color);
		p.strokeWeight(strokeWeight);
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
		// Detect a face on the Kinect points
		PVector facePosition = KinectHelper.detectFace(p, kp);

		// Check if a face was detected
		boolean faceDetected = facePosition != null;

		if (faceDetected) {
			// Center the box on the face with a small offset in the z direction
			center.set(facePosition);
			center.add(0, 0, 0.2f * size);
			System.out.println("Center in face: Done (centered in the first face)");
		} else {
			System.out.println("Center in face: Nothing done. Try again");
		}

		return faceDetected;
	}
}