package jagracar.kinect.containers;

import java.awt.Rectangle;
import java.util.ArrayList;

import gab.opencv.OpenCV;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Helper class containing some useful methods to manipulate scans and slits
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class KinectHelper {

	/**
	 * This class has no public constructor, only static methods
	 */
	private KinectHelper() {

	}

	/**
	 * Creates an average scan from a list of scans, assuming that all the scans have the same dimensions
	 * 
	 * @param scanList the list of scans to average
	 * @return the scan average
	 */
	public static Scan averageScans(ArrayList<Scan> scanList) {
		// Create an empty average scan with the same dimensions as the scans in the list
		Scan firstScan = scanList.get(0);
		Scan averageScan = new Scan(firstScan.p, firstScan.width, firstScan.height);

		// Loop over the scans in the list and fill the average scan arrays
		int[] counter = new int[averageScan.nPoints];
		int[] red = new int[counter.length];
		int[] green = new int[counter.length];
		int[] blue = new int[counter.length];

		for (Scan scan : scanList) {
			averageScan.center.add(scan.center);

			for (int index = 0; index < averageScan.nPoints; index++) {
				if (scan.visibilityMask[index]) {
					averageScan.points[index].add(scan.points[index]);
					int color = scan.colors[index];
					red[index] += (color >> 16) & 0xff;
					green[index] += (color >> 8) & 0xff;
					blue[index] += color & 0xff;
					counter[index]++;
				}
			}
		}

		averageScan.center.div(scanList.size());

		for (int index = 0; index < averageScan.nPoints; index++) {
			if (counter[index] > 0) {
				averageScan.points[index].div(counter[index]);
				averageScan.colors[index] = ((red[index] / counter[index]) << 16)
						| ((green[index] / counter[index]) << 8) | (blue[index] / counter[index]) | 0xff000000;
				averageScan.visibilityMask[index] = true;
			}
		}

		return averageScan;
	}

	/**
	 * Creates a scan from the combination of several slits, assuming that all have the same orientation and dimensions
	 * 
	 * @param p the parent Processing applet
	 * @param slitList the list of slits to combine
	 * @param rotate if true the slits will rotated around their center
	 * @param commonCenter if true all the slits will be moved to have the same center
	 * @return the scan formed from the combination of the slits
	 */
	public static Scan combineSlits(PApplet p, ArrayList<Slit> slitList, boolean rotate, boolean commonCenter) {
		// Create an empty scan with the same center as the last slit added to the list
		Slit slit = slitList.get(slitList.size() - 1);
		boolean verticalSlits = slit.vertical;
		int width = verticalSlits ? slitList.size() : slit.points.length;
		int height = verticalSlits ? slit.points.length : slitList.size();
		Scan scan = new Scan(p, width, height);
		scan.center.set(slit.center);

		// Loop over the slits in the list and fill the scan arrays
		for (int i = 0; i < slitList.size(); i++) {
			slit = slitList.get(i);
			float offset = (slitList.size() - 1 - i) * 5;
			float rotationAngle = 4 * (slitList.size() - 1 - i) * PApplet.PI / 180;
			float cos = PApplet.cos(rotationAngle);
			float sin = PApplet.sin(rotationAngle);

			for (int j = 0; j < slit.points.length; j++) {
				if (slit.visibilityMask[j]) {
					int index = verticalSlits ? i + j * width : j + i * width;
					PVector point = scan.points[index];
					point.set(slit.points[j]);

					// Check if the slit points should be rotated or shifted
					if (rotate) {
						point.sub(slit.center);

						if (verticalSlits) {
							point.set(cos * point.x - sin * point.z, point.y, sin * point.x + cos * point.z);
						} else {
							point.set(point.x, cos * point.y - sin * point.z, sin * point.y + cos * point.z);
						}

						point.add(slit.center);
					} else {
						if (verticalSlits) {
							point.x += offset;
						} else {
							point.y += offset;
						}
					}

					// Check if the slit points should be moved to have the same center
					if (commonCenter) {
						point.sub(slit.center);
						point.add(scan.center);
					}

					scan.colors[index] = slit.colors[j];
					scan.visibilityMask[index] = true;
				}
			}
		}

		return scan;
	}

	/**
	 * Detects a face inside the provided Kinect points and returns the face position. This method uses OpenCV 2 to
	 * detect the faces. If you don't have OpenCV installed in your computer, you should comment the lines inside this
	 * function and return always null.
	 * 
	 * @param p the parent Processing applet
	 * @param kp the Kinect points that should be used to detect the face
	 * @return the face position if a face was detected, null otherwise
	 */
	public static PVector detectFace(PApplet p, KinectPoints kp) {
		// Create an image with only the visible points color information
		PImage img = p.createImage(kp.width, kp.height, PApplet.RGB);
		img.loadPixels();

		for (int index = 0; index < kp.nPoints; index++) {
			if (kp.visibilityMask[index]) {
				img.pixels[index] = kp.colors[index];
			}
		}

		img.updatePixels();

		// Initialize OpenCV
		OpenCV opencv = new OpenCV(p, img);
		opencv.loadCascade(OpenCV.CASCADE_FRONTALFACE);

		// Detect faces in the image
		Rectangle[] faces = opencv.detect();
		System.out.println("Detect face: " + faces.length + " faces detected");

		// Obtain the first detected face central position
		PVector faceCenter = null;

		if (faces.length > 0) {
			Rectangle face = faces[0];
			int x = face.x + (int) (face.width / 2);
			int y = face.y + (int) (face.height / 2);
			int index = x + y * kp.width;

			if (kp.visibilityMask[index]) {
				faceCenter = kp.points[index].copy();
			}
		}

		return faceCenter;
	}
}