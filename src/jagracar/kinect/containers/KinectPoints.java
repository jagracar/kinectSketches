package jagracar.kinect.containers;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Class used to manipulate and paint Kinect output data
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class KinectPoints {

	/**
	 * The parent Processing applet
	 */
	protected PApplet p;

	/**
	 * The arrays horizontal dimension
	 */
	protected int width;

	/**
	 * The arrays vertical dimension
	 */
	protected int height;

	/**
	 * Total number of points in the arrays
	 */
	protected int nPoints;

	/**
	 * Array containing the points coordinates
	 */
	protected PVector[] points;

	/**
	 * Array containing the points colors
	 */
	protected int[] colors;

	/**
	 * Array containing the points visibility mask
	 */
	protected boolean[] visibilityMask;

	/**
	 * Maximum separation between two consecutive points to consider them connected
	 */
	protected float maxPointSeparationSq = 120 * 120;

	/**
	 * Constructs an empty KinectPoints object with the specified dimensions
	 * 
	 * @param p the parent Processing applet
	 * @param width the arrays horizontal dimension
	 * @param height the arrays vertical dimension
	 */
	public KinectPoints(PApplet p, int width, int height) {
		this.p = p;
		this.width = width;
		this.height = height;
		this.nPoints = this.width * this.height;
		this.points = new PVector[this.nPoints];
		this.colors = new int[this.nPoints];
		this.visibilityMask = new boolean[this.nPoints];

		// Initialize the points array
		for (int index = 0; index < this.nPoints; index++) {
			this.points[index] = new PVector();
		}
	}

	/**
	 * Constructs a KinectPoints object from the provided Kinect output data
	 * 
	 * @param p the parent Processing applet
	 * @param points the Kinect 3D points
	 * @param rgbImg the Kinect color image
	 * @param depthMap the Kinect depth map
	 * @param reductionFactor the scale reduction factor
	 */
	public KinectPoints(PApplet p, PVector[] points, PImage rgbImg, int[] depthMap, int reductionFactor) {
		reductionFactor = Math.max(1, reductionFactor);
		this.p = p;
		this.width = rgbImg.width / reductionFactor;
		this.height = rgbImg.height / reductionFactor;
		this.nPoints = this.width * this.height;
		this.points = new PVector[this.nPoints];
		this.colors = new int[this.nPoints];
		this.visibilityMask = new boolean[this.nPoints];

		// Populate the arrays
		rgbImg.loadPixels();

		for (int row = 0; row < this.height; row++) {
			for (int col = 0; col < this.width; col++) {
				int index = col + row * this.width;
				int indexOriginal = col * reductionFactor + row * reductionFactor * rgbImg.width;
				this.points[index] = points[indexOriginal].copy();
				this.colors[index] = rgbImg.pixels[indexOriginal];
				this.visibilityMask[index] = depthMap[indexOriginal] > 0;
			}
		}

		rgbImg.updatePixels();
	}

	/**
	 * Updates the Kinect points with new Kinect data
	 * 
	 * @param pointsNew the new Kinect 3D points
	 * @param rgbImgNew the new Kinect color image
	 * @param depthMapNew the new Kinect depth map
	 * @param reductionFactor the scale reduction factor
	 */
	public void update(PVector[] pointsNew, PImage rgbImgNew, int[] depthMapNew, int reductionFactor) {
		reductionFactor = Math.max(1, reductionFactor);
		int widthNew = rgbImgNew.width / reductionFactor;
		int heightNew = rgbImgNew.height / reductionFactor;

		// Check if the arrays resolution has changed
		if (widthNew != width || heightNew != height) {
			width = widthNew;
			height = heightNew;
			nPoints = width * height;
			points = new PVector[nPoints];
			colors = new int[nPoints];
			visibilityMask = new boolean[nPoints];

			for (int index = 0; index < nPoints; index++) {
				points[index] = new PVector();
			}
		}

		// Update the arrays
		rgbImgNew.loadPixels();

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = col + row * width;
				int indexOriginal = col * reductionFactor + row * reductionFactor * rgbImgNew.width;
				points[index].set(pointsNew[indexOriginal]);
				colors[index] = rgbImgNew.pixels[indexOriginal];
				visibilityMask[index] = depthMapNew[indexOriginal] > 0;
			}
		}

		rgbImgNew.updatePixels();
	}

	/**
	 * Creates a copy of the Kinect points object
	 * 
	 * @return the Kinect points copy
	 */
	public KinectPoints copy() {
		// Create an empty KinectPoints object
		KinectPoints kp = new KinectPoints(p, width, height);

		// Fill the arrays
		for (int index = 0; index < nPoints; index++) {
			kp.points[index].set(points[index]);
			kp.colors[index] = colors[index];
			kp.visibilityMask[index] = visibilityMask[index];
		}

		// Set the rest of the variables
		kp.maxPointSeparationSq = maxPointSeparationSq;

		return kp;
	}

	/**
	 * Constrains the points visibilities to a cube delimited by some lower and upper corner coordinates
	 * 
	 * @param corners an array with the lower and upper corners
	 */
	public void constrainPoints(PVector[] corners) {
		float xMin = corners[0].x;
		float yMin = corners[0].y;
		float zMin = corners[0].z;
		float xMax = corners[1].x;
		float yMax = corners[1].y;
		float zMax = corners[1].z;

		for (int index = 0; index < nPoints; index++) {
			PVector point = points[index];
			visibilityMask[index] &= (point.x > xMin) && (point.x < xMax) && (point.y > yMin) && (point.y < yMax)
					&& (point.z > zMin) && (point.z < zMax);
		}
	}

	/**
	 * Calculates the corner limits that contain all the visible points
	 * 
	 * @return a points array with the lower and upper corner limits
	 */
	public PVector[] calculateLimits() {
		float xMin = Float.MAX_VALUE;
		float yMin = Float.MAX_VALUE;
		float zMin = Float.MAX_VALUE;
		float xMax = -Float.MAX_VALUE;
		float yMax = -Float.MAX_VALUE;
		float zMax = -Float.MAX_VALUE;

		for (int index = 0; index < nPoints; index++) {
			if (visibilityMask[index]) {
				PVector point = points[index];

				if (point.x < xMin) {
					xMin = point.x;
				}

				if (point.x > xMax) {
					xMax = point.x;
				}

				if (point.y < yMin) {
					yMin = point.y;
				}

				if (point.y > yMax) {
					yMax = point.y;
				}

				if (point.z < zMin) {
					zMin = point.z;
				}

				if (point.z > zMax) {
					zMax = point.z;
				}
			}
		}

		// Check that there was at least a visible point
		if ((xMax - xMin) >= 0) {
			return new PVector[] { new PVector(xMin, yMin, zMin), new PVector(xMax, yMax, zMax) };
		} else {
			return null;
		}
	}

	/**
	 * Reduces the Kinect points resolution by a given factor
	 * 
	 * @param reductionFactor the scale reduction factor
	 */
	public void reduceResolution(int reductionFactor) {
		if (reductionFactor > 1) {
			// Obtain the dimensions of the new reduced arrays
			int widthNew = width / reductionFactor;
			int heightNew = height / reductionFactor;
			int nPointsNew = widthNew * heightNew;
			PVector[] pointsNew = new PVector[nPointsNew];
			int[] colorsNew = new int[nPointsNew];
			boolean[] visibilityMaskNew = new boolean[nPointsNew];

			// Populate the arrays
			for (int row = 0; row < heightNew; row++) {
				for (int col = 0; col < widthNew; col++) {
					int indexNew = col + row * widthNew;
					int index = col * reductionFactor + row * reductionFactor * width;
					pointsNew[indexNew] = points[index];
					colorsNew[indexNew] = colors[index];
					visibilityMaskNew[indexNew] = visibilityMask[index];
				}
			}

			// Update the arrays to the new resolution
			width = widthNew;
			height = heightNew;
			nPoints = nPointsNew;
			points = pointsNew;
			colors = colorsNew;
			visibilityMask = visibilityMaskNew;
		}
	}

	/**
	 * Returns true if the two points are close enough to be considered connected
	 * 
	 * @param point1 the first point
	 * @param point2 the second point
	 * @return true if the points can be considered connected
	 */
	protected boolean connected(PVector point1, PVector point2) {
		float dx = point1.x - point2.x;
		float dy = point1.y - point2.y;
		float dz = point1.z - point2.z;

		return (dx * dx + dy * dy + dz * dz) < maxPointSeparationSq;
	}

	/**
	 * Draws the Kinect points as pixels on the screen
	 * 
	 * @param pixelSize the pixel size
	 */
	public void drawAsPixels(int pixelSize) {
		p.pushStyle();
		p.strokeCap(PApplet.SQUARE);
		p.strokeWeight(pixelSize);

		for (int index = 0; index < nPoints; index++) {
			if (visibilityMask[index]) {
				PVector point = points[index];
				p.stroke(colors[index]);
				p.point(point.x, point.y, point.z);
			}
		}

		p.popStyle();
	}

	/**
	 * Draws the Kinect points as pixels on the screen with a uniform color
	 * 
	 * @param pixelSize the pixel size
	 * @param pixelColor the pixel color
	 */
	public void drawAsPixels(int pixelSize, int pixelColor) {
		p.pushStyle();
		p.strokeCap(PApplet.SQUARE);
		p.strokeWeight(pixelSize);
		p.stroke(pixelColor);

		for (int index = 0; index < nPoints; index++) {
			if (visibilityMask[index]) {
				PVector point = points[index];
				p.point(point.x, point.y, point.z);
			}
		}

		p.popStyle();
	}

	/**
	 * Draws the Kinect points as horizontal bands on the screen
	 * 
	 * @param verticalGap the positive vertical gap between bands
	 */
	public void drawAsBands(int verticalGap) {
		p.pushStyle();
		p.noStroke();
		boolean bandStarted = false;

		for (int row = 0; row < height - 1; row += verticalGap) {
			// Finish the band if we are starting a new row and the last shape was not closed
			if (bandStarted) {
				p.endShape();
				bandStarted = false;
			}

			for (int col = 0; col < width; col++) {
				int index = col + row * width;

				// Check if the point is valid
				if (visibilityMask[index]) {
					PVector point = points[index];

					if (!bandStarted) {
						// Start a new band
						p.beginShape(PApplet.TRIANGLE_STRIP);
						p.fill(colors[index]);
						p.vertex(point.x, point.y, point.z);
						bandStarted = true;
					} else if (connected(point, points[index - 1])) {
						p.fill(colors[index]);
						p.vertex(point.x, point.y, point.z);
					} else {
						p.endShape();
						bandStarted = false;

						// It's a good point, use it in the next loop as starting point for a new band
						col--;
						continue;
					}

					// Check if the lower point is valid
					int lowerIndex = index + width;

					if (visibilityMask[lowerIndex]) {
						PVector lowerPoint = points[lowerIndex];

						if (connected(point, lowerPoint)) {
							p.fill(colors[lowerIndex]);
							p.vertex(lowerPoint.x, lowerPoint.y, lowerPoint.z);
						} else {
							p.fill(colors[index]);
							p.vertex(point.x, point.y, point.z);
						}
					} else {
						p.fill(colors[index]);
						p.vertex(point.x, point.y, point.z);
					}
				} else if (bandStarted) {
					// The point is not valid, let's see if we can use the lower point for the last point in the band
					int lowerIndex = index + width;

					if (visibilityMask[lowerIndex]) {
						PVector lowerPoint = points[lowerIndex];

						if (connected(lowerPoint, points[index - 1])) {
							p.fill(colors[lowerIndex]);
							p.vertex(lowerPoint.x, lowerPoint.y, lowerPoint.z);
						}
					}

					// Finish the band
					p.endShape();
					bandStarted = false;
				}
			}
		}

		// Finish the band if the last shape was not closed
		if (bandStarted) {
			p.endShape();
		}

		p.popStyle();
	}

	/**
	 * Draws the Kinect points as horizontal bands on the screen with a uniform color
	 * 
	 * @param verticalGap the positive vertical gap between bands
	 * @param bandsColor the bands color
	 */
	public void drawAsBands(int verticalGap, int bandsColor) {
		p.pushStyle();
		p.noStroke();
		p.fill(bandsColor);
		boolean bandStarted = false;

		for (int row = 0; row < height - 1; row += verticalGap) {
			// Finish the band if we are starting a new row and the last shape was not closed
			if (bandStarted) {
				p.endShape();
				bandStarted = false;
			}

			for (int col = 0; col < width; col++) {
				int index = col + row * width;

				// Check if the point is valid
				if (visibilityMask[index]) {
					PVector point = points[index];

					if (!bandStarted) {
						// Start a new band
						p.beginShape(PApplet.TRIANGLE_STRIP);
						p.vertex(point.x, point.y, point.z);
						bandStarted = true;
					} else if (connected(point, points[index - 1])) {
						p.vertex(point.x, point.y, point.z);
					} else {
						p.endShape();
						bandStarted = false;

						// It's a good point, use it in the next loop as starting point for a new band
						col--;
						continue;
					}

					// Check if the lower point is valid
					int lowerIndex = index + width;

					if (visibilityMask[lowerIndex]) {
						PVector lowerPoint = points[lowerIndex];

						if (connected(point, lowerPoint)) {
							p.vertex(lowerPoint.x, lowerPoint.y, lowerPoint.z);
						} else {
							p.vertex(point.x, point.y, point.z);
						}
					} else {
						p.vertex(point.x, point.y, point.z);
					}
				} else if (bandStarted) {
					// The point is not valid, let's see if we can use the lower point for the last point in the band
					int lowerIndex = index + width;

					if (visibilityMask[lowerIndex]) {
						PVector lowerPoint = points[lowerIndex];

						if (connected(lowerPoint, points[index - 1])) {
							p.vertex(lowerPoint.x, lowerPoint.y, lowerPoint.z);
						}
					}

					// Finish the band
					p.endShape();
					bandStarted = false;
				}
			}
		}

		// Finish the band if the last shape was not closed
		if (bandStarted) {
			p.endShape();
		}

		p.popStyle();
	}

	/**
	 * Draws a line between two Kinect points if they are connected
	 * 
	 * @param index1 the first point index
	 * @param index2 the second point index
	 * @param useColors use the points colors if true
	 */
	protected void drawLine(int index1, int index2, boolean useColors) {
		PVector point1 = points[index1];
		PVector point2 = points[index2];

		if (connected(point1, point2)) {
			if (useColors) {
				p.stroke(colors[index1]);
				p.vertex(point1.x, point1.y, point1.z);
				p.stroke(colors[index2]);
				p.vertex(point2.x, point2.y, point2.z);
			} else {
				p.vertex(point1.x, point1.y, point1.z);
				p.vertex(point2.x, point2.y, point2.z);
			}
		}
	}

	/**
	 * Draws the Kinect points as lines on the screen
	 * 
	 * @param lineWeight the line weight
	 */
	public void drawAsLines(float lineWeight) {
		p.pushStyle();
		p.strokeCap(PApplet.SQUARE);
		p.strokeWeight(lineWeight);
		p.beginShape(PApplet.LINES);

		for (int row = 0; row < height - 1; row++) {
			for (int col = 0; col < width - 1; col++) {
				int index = col + row * width;

				if (visibilityMask[index]) {
					if (visibilityMask[index + 1]) {
						drawLine(index, index + 1, true);
					}

					if (visibilityMask[index + width]) {
						drawLine(index, index + width, true);
					}

					if (visibilityMask[index + 1 + width]) {
						drawLine(index, index + 1 + width, true);
					}
				}
			}
		}

		p.endShape();
		p.popStyle();
	}

	/**
	 * Draws the Kinect points as lines on the screen with a uniform color
	 * 
	 * @param lineWeight the line weight
	 * @param lineColor the line color
	 */
	public void drawAsLines(float lineWeight, int lineColor) {
		p.pushStyle();
		p.strokeCap(PApplet.SQUARE);
		p.strokeWeight(lineWeight);
		p.stroke(lineColor);
		p.beginShape(PApplet.LINES);

		for (int row = 0; row < height - 1; row++) {
			for (int col = 0; col < width - 1; col++) {
				int index = col + row * width;

				if (visibilityMask[index]) {
					if (visibilityMask[index + 1]) {
						drawLine(index, index + 1, false);
					}

					if (visibilityMask[index + width]) {
						drawLine(index, index + width, false);
					}

					if (visibilityMask[index + 1 + width]) {
						drawLine(index, index + 1 + width, false);
					}
				}
			}
		}

		p.endShape();
		p.popStyle();
	}

	/**
	 * Draws a triangle between three Kinect points if they are connected
	 * 
	 * @param index1 the first point index
	 * @param index2 the second point index
	 * @param index3 the third point index
	 * @param useColors use the points colors if true
	 */
	protected void drawTriangle(int index1, int index2, int index3, boolean useColors) {
		PVector point1 = points[index1];
		PVector point2 = points[index2];
		PVector point3 = points[index3];

		if (connected(point1, point2) && connected(point1, point3) && connected(point2, point3)) {
			if (useColors) {
				p.fill(colors[index1]);
				p.vertex(point1.x, point1.y, point1.z);
				p.fill(colors[index2]);
				p.vertex(point2.x, point2.y, point2.z);
				p.fill(colors[index3]);
				p.vertex(point3.x, point3.y, point3.z);
			} else {
				p.vertex(point1.x, point1.y, point1.z);
				p.vertex(point2.x, point2.y, point2.z);
				p.vertex(point3.x, point3.y, point3.z);
			}
		}
	}

	/**
	 * Draws the Kinect points as triangles on the screen
	 */
	public void drawAsTriangles() {
		p.pushStyle();
		p.noStroke();
		p.beginShape(PApplet.TRIANGLES);

		for (int row = 0; row < height - 1; row++) {
			for (int col = 0; col < width - 1; col++) {
				int index = col + row * width;

				// First triangle
				if (visibilityMask[index] && visibilityMask[index + width]) {
					if (visibilityMask[index + 1]) {
						drawTriangle(index, index + 1, index + width, true);
					} else if (visibilityMask[index + 1 + width]) {
						drawTriangle(index, index + 1 + width, index + width, true);
					}
				}

				// Second triangle
				if (visibilityMask[index + 1] && visibilityMask[index + 1 + width]) {
					if (visibilityMask[index + width]) {
						drawTriangle(index + 1, index + 1 + width, index + width, true);
					} else if (visibilityMask[index]) {
						drawTriangle(index, index + 1, index + 1 + width, true);
					}
				}
			}
		}

		p.endShape();
		p.popStyle();
	}

	/**
	 * Draws the Kinect points as triangles on the screen with a uniform color
	 * 
	 * @param trianglesColor the triangles color
	 */
	public void drawAsTriangles(int trianglesColor) {
		p.pushStyle();
		p.noStroke();
		p.fill(trianglesColor);
		p.beginShape(PApplet.TRIANGLES);

		for (int row = 0; row < height - 1; row++) {
			for (int col = 0; col < width - 1; col++) {
				int index = col + row * width;

				// First triangle
				if (visibilityMask[index] && visibilityMask[index + width]) {
					if (visibilityMask[index + 1]) {
						drawTriangle(index, index + 1, index + width, false);
					} else if (visibilityMask[index + 1 + width]) {
						drawTriangle(index, index + 1 + width, index + width, false);
					}
				}

				// Second triangle
				if (visibilityMask[index + 1] && visibilityMask[index + 1 + width]) {
					if (visibilityMask[index + width]) {
						drawTriangle(index + 1, index + 1 + width, index + width, false);
					} else if (visibilityMask[index]) {
						drawTriangle(index, index + 1, index + 1 + width, false);
					}
				}
			}
		}

		p.endShape();
		p.popStyle();
	}

	/**
	 * Sets the value of the maximum separation between two consecutive points to consider them connected
	 * 
	 * @param newMaxPointSeparation the new maximum point separation value
	 */
	public void setMaxPointSeparation(float newMaxPointSeparation) {
		maxPointSeparationSq = newMaxPointSeparation * newMaxPointSeparation;
	}

	/**
	 * Returns the value of the maximum separation between two consecutive points to consider them connected
	 */
	public float getMaxPointSeparation() {
		return PApplet.sqrt(maxPointSeparationSq);
	}
}
