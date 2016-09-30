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
	 * Maximum separation between two consecutive points to consider them connected
	 */
	protected static final float MAX_SEPARATION_SQ = 120 * 120;

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
	 * Constructs an empty KinectPoints object with the specified dimensions
	 * 
	 * @param width the arrays horizontal dimension
	 * @param height the arrays vertical dimension
	 */
	public KinectPoints(int width, int height) {
		this.width = width;
		this.height = height;
		this.nPoints = this.width * this.height;
		this.points = new PVector[this.nPoints];
		this.colors = new int[this.nPoints];
		this.visibilityMask = new boolean[this.nPoints];

		// Initialize the points array
		for (int i = 0; i < this.nPoints; i++) {
			this.points[i] = new PVector();
		}
	}

	/**
	 * Constructs a KinectPoints object from the provided Kinect output data
	 * 
	 * @param points the Kinect 3D points
	 * @param rgbImg the Kinect color image
	 * @param depthMap the Kinect depth map
	 * @param reductionFactor the scale reduction factor
	 */
	public KinectPoints(PVector[] points, PImage rgbImg, int[] depthMap, int reductionFactor) {
		reductionFactor = Math.max(1, reductionFactor);
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

			// Initialize the points array
			for (int i = 0; i < nPoints; i++) {
				points[i] = new PVector();
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

		for (int i = 0; i < nPoints; i++) {
			PVector point = points[i];
			visibilityMask[i] &= (point.x > xMin) && (point.x < xMax) && (point.y > yMin) && (point.y < yMax)
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

		for (int i = 0; i < nPoints; i++) {
			if (visibilityMask[i]) {
				PVector point = points[i];

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
	 * Returns true if the two points are close enough to be considered connected
	 * 
	 * @param point1 the first point
	 * @param point2 the second point
	 * @return true if the points can be considered connected
	 */
	protected static boolean connected(PVector point1, PVector point2) {
		float dx = point1.x - point2.x;
		float dy = point1.y - point2.y;
		float dz = point1.z - point2.z;

		return (dx * dx + dy * dy + dz * dz) < MAX_SEPARATION_SQ;
	}

	/**
	 * Draws the Kinect points as pixels on the screen
	 * 
	 * @param p the parent Processing applet
	 * @param pixelSize the pixel size
	 */
	public void drawAsPixels(PApplet p, int pixelSize) {
		p.pushStyle();
		p.strokeWeight(pixelSize);

		for (int i = 0; i < nPoints; i++) {
			if (visibilityMask[i]) {
				PVector point = points[i];
				p.stroke(colors[i]);
				p.point(point.x, point.y, point.z);
			}
		}

		p.popStyle();
	}

	/**
	 * Draws the Kinect points as pixels on the screen with a uniform color
	 * 
	 * @param p the parent Processing applet
	 * @param pixelSize the pixel size
	 * @param pixelColor the pixel color
	 */
	public void drawAsPixels(PApplet p, int pixelSize, int pixelColor) {
		p.pushStyle();
		p.strokeWeight(pixelSize);
		p.stroke(pixelColor);

		for (int i = 0; i < nPoints; i++) {
			if (visibilityMask[i]) {
				PVector point = points[i];
				p.point(point.x, point.y, point.z);
			}
		}

		p.popStyle();
	}

	/**
	 * Draws the Kinect points as horizontal bands on the screen
	 * 
	 * @param p the parent Processing applet
	 * @param verticalGap the positive vertical gap between bands
	 */
	public void drawAsBands(PApplet p, int verticalGap) {
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
					} else if (KinectPoints.connected(point, points[index - 1])) {
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

						if (KinectPoints.connected(point, lowerPoint)) {
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

						if (KinectPoints.connected(lowerPoint, points[index - 1])) {
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
	 * @param p the parent Processing applet
	 * @param verticalGap the positive vertical gap between bands
	 * @param bandsColor the bands color
	 */
	public void drawAsBands(PApplet p, int verticalGap, int bandsColor) {
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
					} else if (KinectPoints.connected(point, points[index - 1])) {
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

						if (KinectPoints.connected(point, lowerPoint)) {
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

						if (KinectPoints.connected(lowerPoint, points[index - 1])) {
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
	 * @param p the parent Processing applet
	 * @param point1 the first point
	 * @param point2 the second point
	 */
	protected void drawLine(PApplet p, PVector point1, PVector point2) {
		if (KinectPoints.connected(point1, point2)) {
			p.line(point1.x, point1.y, point1.z, point2.x, point2.y, point2.z);
		}
	}

	/**
	 * Draws the Kinect points as lines on the screen
	 * 
	 * @param p the parent Processing applet
	 * @param lineWeight the line weight
	 */
	public void drawAsLines(PApplet p, float lineWeight) {
		p.pushStyle();
		p.strokeWeight(lineWeight);

		for (int row = 0; row < height - 1; row++) {
			for (int col = 0; col < width - 1; col++) {
				int index = col + row * width;

				if (visibilityMask[index]) {
					PVector point = points[index];
					p.stroke(colors[index]);

					if (visibilityMask[index + 1]) {
						drawLine(p, point, points[index + 1]);
					}

					if (visibilityMask[index + width]) {
						drawLine(p, point, points[index + width]);
					}

					if (visibilityMask[index + 1 + width]) {
						drawLine(p, point, points[index + 1 + width]);
					}
				}
			}
		}

		p.popStyle();
	}

	/**
	 * Draws the Kinect points as lines on the screen with a uniform color
	 * 
	 * @param p the parent Processing applet
	 * @param lineWeight the line weight
	 * @param lineColor the line color
	 */
	public void drawAsLines(PApplet p, float lineWeight, int lineColor) {
		p.pushStyle();
		p.strokeWeight(lineWeight);
		p.stroke(lineColor);

		for (int row = 0; row < height - 1; row++) {
			for (int col = 0; col < width - 1; col++) {
				int index = col + row * width;

				if (visibilityMask[index]) {
					PVector point = points[index];

					if (visibilityMask[index + 1]) {
						drawLine(p, point, points[index + 1]);
					}

					if (visibilityMask[index + width]) {
						drawLine(p, point, points[index + width]);
					}

					if (visibilityMask[index + 1 + width]) {
						drawLine(p, point, points[index + 1 + width]);
					}
				}
			}
		}

		p.popStyle();
	}

	/**
	 * Draws a triangle between three Kinect points if they are connected
	 * 
	 * @param p the parent Processing applet
	 * @param point1 the first point
	 * @param point2 the second point
	 * @param point3 the third point
	 */
	protected void drawTriangle(PApplet p, PVector point1, PVector point2, PVector point3) {
		if (KinectPoints.connected(point1, point2) && KinectPoints.connected(point1, point3)
				&& KinectPoints.connected(point2, point3)) {
			p.beginShape(PApplet.TRIANGLES);
			p.vertex(point1.x, point1.y, point1.z);
			p.vertex(point2.x, point2.y, point2.z);
			p.vertex(point3.x, point3.y, point3.z);
			p.endShape();
		}
	}

	/**
	 * Draws a triangle between three Kinect points if they are connected
	 * 
	 * @param p the parent Processing applet
	 * @param point1 the first point
	 * @param point2 the second point
	 * @param point3 the third point
	 * @param color1 the first point color
	 * @param color2 the second point color
	 * @param color3 the third point color
	 */
	protected void drawTriangle(PApplet p, PVector point1, PVector point2, PVector point3, int color1, int color2,
			int color3) {
		if (KinectPoints.connected(point1, point2) && KinectPoints.connected(point1, point3)
				&& KinectPoints.connected(point2, point3)) {
			p.beginShape(PApplet.TRIANGLES);
			p.fill(color1);
			p.vertex(point1.x, point1.y, point1.z);
			p.fill(color2);
			p.vertex(point2.x, point2.y, point2.z);
			p.fill(color3);
			p.vertex(point3.x, point3.y, point3.z);
			p.endShape();
		}
	}

	/**
	 * Draws the Kinect points as triangles on the screen
	 * 
	 * @param p the parent Processing applet
	 */
	public void drawAsTriangles(PApplet p) {
		p.pushStyle();
		p.noStroke();

		for (int row = 0; row < height - 1; row++) {
			for (int col = 0; col < width - 1; col++) {
				int index = col + row * width;

				// First triangle
				if (visibilityMask[index] && visibilityMask[index + width]) {
					if (visibilityMask[index + 1]) {
						drawTriangle(p, points[index], points[index + 1], points[index + width], colors[index],
								colors[index + 1], colors[index + width]);
					} else if (visibilityMask[index + 1 + width]) {
						drawTriangle(p, points[index], points[index + 1 + width], points[index + width], colors[index],
								colors[index + 1 + width], colors[index + width]);
					}
				}

				// Second triangle
				if (visibilityMask[index + 1] && visibilityMask[index + 1 + width]) {
					if (visibilityMask[index + width]) {
						drawTriangle(p, points[index + 1], points[index + 1 + width], points[index + width],
								colors[index + 1], colors[index + 1 + width], colors[index + width]);
					} else if (visibilityMask[index]) {
						drawTriangle(p, points[index], points[index + 1], points[index + 1 + width], colors[index],
								colors[index + 1], colors[index + 1 + width]);
					}
				}
			}
		}

		p.popStyle();
	}

	/**
	 * Draws the Kinect points as triangles on the screen with a uniform color
	 * 
	 * @param p the parent Processing applet
	 * @param trianglesColor the triangles color
	 */
	public void drawAsTriangles(PApplet p, int trianglesColor) {
		p.pushStyle();
		p.noStroke();
		p.fill(trianglesColor);

		for (int y = 0; y < height - 1; y++) {
			for (int x = 0; x < width - 1; x++) {
				int index = x + y * width;

				// First triangle
				if (visibilityMask[index] && visibilityMask[index + width]) {
					if (visibilityMask[index + 1]) {
						drawTriangle(p, points[index], points[index + 1], points[index + width]);
					} else if (visibilityMask[index + 1 + width]) {
						drawTriangle(p, points[index], points[index + 1 + width], points[index + width]);
					}
				}

				// Second triangle
				if (visibilityMask[index + 1] && visibilityMask[index + 1 + width]) {
					if (visibilityMask[index + width]) {
						drawTriangle(p, points[index + 1], points[index + 1 + width], points[index + width]);
					} else if (visibilityMask[index]) {
						drawTriangle(p, points[index], points[index + 1], points[index + 1 + width]);
					}
				}
			}
		}

		p.popStyle();
	}
}