package jagracar.kinect.containers;

import jagracar.kinect.util.ScanBox;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Subclass of the KinectPoints class. Implements some additional functions to manipulate and save Kinect output data
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class Scan extends KinectPoints {

	/**
	 * Array containing the column position of the first valid point from a given row
	 */
	protected int[] ini;

	/**
	 * Array containing the column position of the last valid point from a given row
	 */
	protected int[] end;

	/**
	 * Array indicating if a given row is empty because there are no valid points
	 */
	protected boolean[] empty;

	/**
	 * The scan center
	 */
	protected PVector center;

	/**
	 * The column and row of the scan point that is closer to the scan center in the (x, y) plane
	 */
	protected int[] centralPointPixel;

	/**
	 * Coordinates of the scan central point
	 */
	protected PVector centralPoint;

	/**
	 * Array containing the point normals
	 */
	protected PVector[] normals;

	/**
	 * Array containing the scan back side points coordinates
	 */
	protected PVector[] backPoints;

	/**
	 * Constructs an empty Scan object with the specified dimensions
	 * 
	 * @param width the arrays horizontal dimension
	 * @param height the arrays vertical dimension
	 */
	public Scan(int width, int height) {
		super(width, height);
		this.ini = new int[this.height];
		this.end = new int[this.height];
		this.empty = new boolean[this.height];
		this.center = new PVector();
		this.centralPointPixel = new int[2];
		this.centralPoint = new PVector();
		this.normals = null;
		this.backPoints = null;
	}

	/**
	 * Constructs a Scan object using the Kinect points inside the scan box
	 * 
	 * @param kp the KinectPoints object
	 * @param box the scan box from which the scan points will be selected
	 */
	public Scan(KinectPoints kp, ScanBox box) {
		this(kp.width, kp.height);

		// Fill the main scan variables
		this.center.set(box.center);

		for (int i = 0; i < this.nPoints; i++) {
			PVector point = kp.points[i];
			this.points[i].set(point);
			this.colors[i] = kp.colors[i];
			this.visibilityMask[i] = kp.visibilityMask[i] && box.isInside(point);
		}

		// Calculate the scan specific arrays
		calculateScanSpecificArrays(false);
	}

	/**
	 * Calculates the scan specific arrays
	 * 
	 * @param startFromNull if true, the normals and backPoints array will be set to null before they are recalculated
	 */
	public void calculateScanSpecificArrays(boolean startFromNull) {
		// Obtain the scan extremes and the scan central point
		obtainExtremes();
		obtainCentralPoint();

		// Calculate the normals if they were calculated before
		if (normals != null) {
			if (startFromNull) {
				normals = null;
			}

			calculateNormals();
		}

		// Calculate the back side points if they were calculated before
		if (backPoints != null) {
			if (startFromNull) {
				backPoints = null;
			}

			calculateBackPoints();
		}
	}

	/**
	 * Obtains the scan extremes
	 */
	protected void obtainExtremes() {
		for (int row = 0; row < height; row++) {
			int iniCol = -1;
			int endCol = -1;

			for (int col = 0; col < width; col++) {
				if (visibilityMask[col + row * width]) {
					if (iniCol < 0) {
						iniCol = col;
					}

					endCol = col;
				}
			}

			ini[row] = iniCol;
			end[row] = endCol;
			empty[row] = iniCol < 0;
		}
	}

	/**
	 * Obtains the coordinates and the column and row values of the scan point closer to the scan center
	 */
	protected void obtainCentralPoint() {
		float minDistanceSq = Float.MAX_VALUE;
		centralPointPixel[0] = -1;
		centralPointPixel[1] = -1;

		for (int row = 0; row < height; row++) {
			if (!empty[row]) {
				for (int col = ini[row]; col <= end[row]; col++) {
					int index = col + row * width;

					if (visibilityMask[index]) {
						PVector point = points[index];
						float distanceSq = PApplet.sq(point.x - center.x) + PApplet.sq(point.y - center.y);

						if (distanceSq < minDistanceSq) {
							minDistanceSq = distanceSq;
							centralPointPixel[0] = col;
							centralPointPixel[1] = row;
							centralPoint.set(point);
						}
					}
				}
			}
		}
	}

	/**
	 * Calculates the points normals
	 */
	public void calculateNormals() {
		// Create the normals array if it was not created before
		if (normals == null) {
			normals = new PVector[nPoints];

			for (int i = 0; i < nPoints; i++) {
				normals[i] = new PVector();
			}
		}

		// Calculate the normals
		for (int row = 0; row < height; row++) {
			if (!empty[row]) {
				for (int col = ini[row]; col <= end[row]; col++) {
					int index = col + row * width;
					PVector normal = normals[index];
					normal.set(0, 0, 0);

					if (visibilityMask[index]) {
						// Calculate the average normal value at the given point
						PVector point = points[index];
						int n = 0;

						if (col + 1 < width && visibilityMask[index + 1]) {
							PVector v1 = PVector.sub(points[index + 1], point);

							if (row + 1 < height && visibilityMask[index + width]) {
								PVector v2 = PVector.sub(points[index + width], point);
								PVector perp = v1.cross(v2);
								perp.normalize();
								normal.add(perp);
								n++;
							}

							if (row - 1 >= 0 && visibilityMask[index - width]) {
								PVector v2 = PVector.sub(points[index - width], point);
								PVector perp = v2.cross(v1);
								perp.normalize();
								normal.add(perp);
								n++;
							}
						}

						if (col - 1 >= 0 && visibilityMask[index - 1]) {
							PVector v1 = PVector.sub(points[index - 1], point);

							if (row + 1 < height && visibilityMask[index + width]) {
								PVector v2 = PVector.sub(points[index + width], point);
								PVector perp = v2.cross(v1);
								perp.normalize();
								normal.add(perp);
								n++;
							}

							if (row - 1 >= 0 && visibilityMask[index - width]) {
								PVector v2 = PVector.sub(points[index - width], point);
								PVector perp = v1.cross(v2);
								perp.normalize();
								normal.add(perp);
								n++;
							}
						}

						if (n > 0) {
							normal.normalize();
						}
					}
				}
			}
		}
	}

	/**
	 * Calculates the scan back side points
	 */
	public void calculateBackPoints() {
		// Create the back side points array if it was not created before
		if (backPoints == null) {
			backPoints = new PVector[nPoints];

			for (int i = 0; i < nPoints; i++) {
				backPoints[i] = new PVector();
			}
		}

		// Calculate the normals if they were not calculated before
		if (normals == null) {
			calculateNormals();
		}

		// Calculate the back side points
		for (int row = 0; row < height; row++) {
			if (!empty[row]) {
				for (int col = ini[row]; col <= end[row]; col++) {
					int index = col + row * width;

					if (visibilityMask[index]) {
						float offset = 0.01f;

						if ((col - 1 >= 0) && (col + 1 < width) && (row - 1 >= 0) && (row + 1 < height)
								&& visibilityMask[index - 1] && visibilityMask[index + 1]
								&& visibilityMask[index - width] && visibilityMask[index + width]) {
							offset *= 10f;
						}

						backPoints[index].set(points[index]).sub(normals[index].copy().mult(offset));
					}
				}
			}
		}
	}

	/**
	 * Reduces the scan resolution by a given factor
	 * 
	 * @param reductionFactor the scale reduction factor
	 */
	public void reduceResolution(int reductionFactor) {
		if (reductionFactor > 1) {
			super.reduceResolution(reductionFactor);

			// Calculate the scan specific arrays
			calculateScanSpecificArrays(true);
		}
	}

	/**
	 * Reduces the scan resolution by a given factor, smoothing the points at the same time
	 * 
	 * @param p the parent Processing applet
	 * @param reductionFactor the scale reduction factor
	 */
	public void reduceResolution(PApplet p, int reductionFactor) {
		if (reductionFactor > 1) {
			// Obtain the dimensions of the new reduced arrays
			int widthNew = width / reductionFactor + 2;
			int heightNew = height / reductionFactor + 2;
			int nPointsNew = widthNew * heightNew;
			PVector[] pointsNew = new PVector[nPointsNew];
			int[] colorsNew = new int[nPointsNew];
			boolean[] visibilityMaskNew = new boolean[nPointsNew];

			// Populate the arrays
			for (int row = 0; row < heightNew; row++) {
				for (int col = 0; col < widthNew; col++) {
					int indexNew = col + row * widthNew;

					// Average between nearby pixels
					PVector pointAverage = new PVector();
					int redAverage = 0;
					int greenAverage = 0;
					int blueAverage = 0;
					int counter = 0;

					for (int i = -reductionFactor / 2; i <= reductionFactor / 2; i++) {
						for (int j = -reductionFactor / 2; j <= reductionFactor / 2; j++) {
							int rowNearby = row * reductionFactor + i;
							int colNearby = col * reductionFactor + j;

							if (colNearby >= 0 && colNearby < width && rowNearby >= 0 && rowNearby < height) {
								int indexNearby = colNearby + rowNearby * width;

								if (visibilityMask[indexNearby]) {
									pointAverage.add(points[indexNearby]);
									int color = colors[indexNearby];
									redAverage += (color >> 16) & 0xff;
									greenAverage += (color >> 8) & 0xff;
									blueAverage += color & 0xff;
									counter++;
								}
							}
						}
					}

					if (counter > 0) {
						pointsNew[indexNew] = pointAverage.div(counter);
						colorsNew[indexNew] = ((redAverage / counter) << 16) | ((greenAverage / counter) << 8)
								| (blueAverage / counter) | 0xff000000;
						visibilityMaskNew[indexNew] = true;
					} else {
						pointsNew[indexNew] = pointAverage;
						colorsNew[indexNew] = 0;
						visibilityMaskNew[indexNew] = false;
					}
				}
			}

			// Update the arrays to the new resolution
			width = widthNew;
			height = heightNew;
			nPoints = nPointsNew;
			points = pointsNew;
			colors = colorsNew;
			visibilityMask = visibilityMaskNew;

			// Calculate the scan specific arrays
			calculateScanSpecificArrays(true);
		}
	}

	/**
	 * Updates the scan points with new Kinect data
	 * 
	 * @param pointsNew the new Kinect 3D points
	 * @param rgbImgNew the new Kinect color image
	 * @param depthMapNew the new Kinect depth map
	 * @param reductionFactor the scale reduction factor
	 */
	public void update(PVector[] pointsNew, PImage rgbImgNew, int[] depthMapNew, int reductionFactor) {
		super.update(pointsNew, rgbImgNew, depthMapNew, reductionFactor);

		// Calculate the scan specific arrays
		calculateScanSpecificArrays(true);
	}

	/**
	 * Constrains the points visibilities to a cube delimited by some lower and upper corner coordinates
	 * 
	 * @param corners an array with the lower and upper corners
	 */
	public void constrainPoints(PVector[] corners) {
		super.constrainPoints(corners);

		// Calculate the scan specific arrays
		calculateScanSpecificArrays(false);
	}

	/**
	 * Rotates the scan around the vertical axis
	 * 
	 * @param rotationAngle the scan rotation angle in radians
	 */
	public void rotate(float rotationAngle) {
		float cos = PApplet.cos(rotationAngle);
		float sin = PApplet.sin(rotationAngle);

		for (PVector point : points) {
			point.sub(center);
			point.set(cos * point.x - sin * point.z, point.y, sin * point.x + cos * point.z);
			point.add(center);
		}

		// Calculate the scan specific arrays
		calculateScanSpecificArrays(false);
	}

	/**
	 * Increases or decreases the size of the scan by a given factor
	 * 
	 * @param scaleFactor the size scaling factor
	 */
	public void scale(float scaleFactor) {
		for (PVector point : points) {
			point.sub(center);
			point.mult(scaleFactor);
			point.add(center);
		}

		// Calculate the scan specific arrays
		calculateScanSpecificArrays(false);
	}

	/**
	 * Crops the scan to the region with visible points
	 */
	public void crop() {
		// Calculate the limits of the scan region with visible data
		int colIni = Integer.MAX_VALUE;
		int colEnd = Integer.MIN_VALUE;
		int rowIni = Integer.MAX_VALUE;
		int rowEnd = Integer.MIN_VALUE;

		for (int row = 0; row < height; row++) {
			if (!empty[row]) {
				if (ini[row] < colIni) {
					colIni = ini[row];
				}

				if (end[row] > colEnd) {
					colEnd = end[row];
				}

				if (row < rowIni) {
					rowIni = row;
				}

				if (row > rowEnd) {
					rowEnd = row;
				}
			}
		}

		// Check that there was at least one visible data point
		if (colIni <= colEnd && rowIni <= rowEnd) {
			// Obtain the dimensions of the new cropped arrays
			int widthNew = colEnd - colIni + 1;
			int heightNew = rowEnd - rowIni + 1;
			int nPointsNew = widthNew * heightNew;
			PVector[] pointsNew = new PVector[nPointsNew];
			int[] colorsNew = new int[nPointsNew];
			boolean[] visibilityMaskNew = new boolean[nPointsNew];

			// Populate the new arrays
			for (int row = 0; row < heightNew; row++) {
				for (int col = 0; col < widthNew; col++) {
					int indexNew = col + row * widthNew;
					int index = (colIni + col) + (rowIni + row) * width;
					pointsNew[indexNew] = points[index];
					colorsNew[indexNew] = colors[index];
					visibilityMaskNew[indexNew] = visibilityMask[index];
				}
			}

			// Update the arrays to the new dimensions
			width = widthNew;
			height = heightNew;
			nPoints = nPointsNew;
			points = pointsNew;
			colors = colorsNew;
			visibilityMask = visibilityMaskNew;

			// Calculate the scan specific arrays
			calculateScanSpecificArrays(true);
		}
	}

	/**
	 * Fills the scan holes interpolating between valid points in the same scan row
	 * 
	 * @param maxHoleGap the maximum number of points missing in order to fill the hole
	 */
	public void fillHoles(int maxHoleGap) {
		// Fill the holes row by row
		for (int row = 0; row < height; row++) {
			if (!empty[row]) {
				for (int col = ini[row] + 1; col < end[row]; col++) {
					int index = col + row * width;

					// Check if we are at the beginning of a hole
					if (!visibilityMask[index]) {
						// Calculate the limits of the hole
						int startIndex = index - 1;
						int finishIndex = startIndex;

						for (int i = col + 1; i <= end[row]; i++) {
							finishIndex = i + row * width;

							if (visibilityMask[finishIndex]) {
								// The column loop should continue from the end of the hole
								col = i;
								break;
							}
						}

						// Fill the hole if the gap is not too big
						if ((finishIndex - startIndex - 1) <= maxHoleGap) {
							int startColor = colors[startIndex];
							int finishColor = colors[finishIndex];
							int startRed = (startColor >> 16) & 0xff;
							int startGreen = (startColor >> 8) & 0xff;
							int startBlue = startColor & 0xff;
							float gapRed = ((finishColor >> 16) & 0xff) - startRed;
							float gapGreen = ((finishColor >> 8) & 0xff) - startGreen;
							float gapBlue = (finishColor & 0xff) - startBlue;
							PVector gapDirection = PVector.sub(points[finishIndex], points[startIndex]);
							float delta = 1.0f / (finishIndex - startIndex);
							float step = 0f;

							for (int i = startIndex + 1; i < finishIndex; i++) {
								step += delta;
								int pixelRed = Math.round(startRed + step * gapRed);
								int pixelGreen = Math.round(startGreen + step * gapGreen);
								int pixelBlue = Math.round(startBlue + step * gapBlue);
								points[i].set(points[startIndex]).add(PVector.mult(gapDirection, step));
								colors[i] = (pixelRed << 16) | (pixelGreen << 8) | pixelBlue | 0xff000000;
								visibilityMask[i] = true;
							}
						}
					}
				}
			}
		}

		// Calculate the scan specific arrays
		calculateScanSpecificArrays(false);
	}

	/**
	 * Draws the scan back side as triangles on the screen with a uniform color
	 * 
	 * @param p the parent Processing applet
	 * @param trianglesColor the triangles color
	 */
	public void drawBackSide(PApplet p, int trianglesColor) {
		p.pushStyle();
		p.noStroke();
		p.fill(trianglesColor);

		for (int row = 0; row < height - 1; row++) {
			if (!empty[row] && !empty[row + 1]) {
				int colStart = Math.min(ini[row], ini[row + 1]);
				int colEnd = Math.max(end[row], end[row + 1]);

				for (int col = colStart; col < colEnd; col++) {
					int index = col + row * width;

					// First triangle
					if (visibilityMask[index] && visibilityMask[index + width]) {
						if (visibilityMask[index + 1]) {
							drawTriangle(p, backPoints[index], backPoints[index + 1], backPoints[index + width]);
						} else if (visibilityMask[index + 1 + width]) {
							drawTriangle(p, backPoints[index], backPoints[index + 1 + width],
									backPoints[index + width]);
						}
					}

					// Second triangle
					if (visibilityMask[index + 1] && visibilityMask[index + 1 + width]) {
						if (visibilityMask[index + width]) {
							drawTriangle(p, backPoints[index + 1], backPoints[index + 1 + width],
									backPoints[index + width]);
						} else if (visibilityMask[index]) {
							drawTriangle(p, backPoints[index], backPoints[index + 1], backPoints[index + 1 + width]);
						}
					}
				}
			}
		}

		p.popStyle();
	}

	/**
	 * Initializes the scan points and colors from a file
	 * 
	 * @param p the parent Processing applet
	 * @param fileName the file name
	 */
	public void initFromFile(PApplet p, String fileName) {
		// Load the file lines containing the scan
		String[] fileLines = p.loadStrings(fileName);

		// The scan dimensions should be in the first line
		String[] dimensions = fileLines[0].split(" ");

		// Reset the scan variables
		width = Integer.valueOf(dimensions[0]);
		height = Integer.valueOf(dimensions[1]);
		nPoints = width * height;
		points = new PVector[nPoints];
		colors = new int[nPoints];
		visibilityMask = new boolean[nPoints];
		ini = new int[height];
		end = new int[height];
		empty = new boolean[height];

		// Fill the arrays
		center.set(0, 0, 0);
		int counter = 0;

		for (int i = 0; i < nPoints; i++) {
			String[] pointsAndColors = fileLines[i + 1].split(" ");

			if (Float.valueOf(pointsAndColors[3]) > 0) {
				float x = Float.valueOf(pointsAndColors[0]);
				float y = Float.valueOf(pointsAndColors[1]);
				float z = Float.valueOf(pointsAndColors[2]);
				int red = Math.round(Float.valueOf(pointsAndColors[3]));
				int green = Math.round(Float.valueOf(pointsAndColors[4]));
				int blue = Math.round(Float.valueOf(pointsAndColors[5]));

				points[i] = new PVector(x, y, z);
				colors[i] = (red << 16) | (green << 8) | blue | 0xff000000;
				visibilityMask[i] = true;
				center.add(points[i]);
				counter++;
			} else {
				points[i] = new PVector();
			}
		}

		if (counter > 0) {
			center.div(counter);
		}

		// Calculate the scan specific arrays
		calculateScanSpecificArrays(true);
	}

	/**
	 * Save the scan points and colors on a file
	 * 
	 * @param p the parent Processing applet
	 * @param fileName the file name
	 */
	public void savePoints(PApplet p, String fileName) {
		// Create the array that will contain the file lines
		String[] lines = new String[nPoints + 1];

		// The first line describes the scan dimensions
		lines[0] = width + " " + height;

		// Write each point coordinates and color on a separate line
		for (int i = 0; i < nPoints; i++) {
			if (visibilityMask[i]) {
				// Center the points coordinates
				PVector point = PVector.sub(points[i], center);
				int col = colors[i];
				int red = (col >> 16) & 0xff;
				int green = (col >> 8) & 0xff;
				int blue = col & 0xff;
				lines[i + 1] = point.x + " " + point.y + " " + point.z + " " + red + " " + green + " " + blue;
			} else {
				// Use a dummy line if the point should be masked
				lines[i + 1] = "-99" + " " + "-99" + " " + "-99" + " " + "-99" + " " + "-99" + " " + "-99";
			}
		}

		// Save the data on the file
		p.saveStrings(fileName, lines);
	}
}