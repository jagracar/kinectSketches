package jagracar.kinect.containers;

import java.util.ArrayList;

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
	 * The scan center
	 */
	protected PVector center;

	/**
	 * Array containing the scan point normals
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
		this.center = new PVector();
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

		// Fill the main scan arrays
		for (int i = 0; i < this.nPoints; i++) {
			PVector point = kp.points[i];
			this.points[i].set(point);
			this.colors[i] = kp.colors[i];
			this.visibilityMask[i] = kp.visibilityMask[i] && box.isInside(point);
		}

		// Set the scan center to the scan box center
		this.center.set(box.center);
	}

	/**
	 * Calculates the scan points normals
	 */
	public void calculateNormals() {
		// Create the normals array if necessary
		if (normals == null || normals.length != nPoints) {
			normals = new PVector[nPoints];

			for (int i = 0; i < nPoints; i++) {
				normals[i] = new PVector();
			}
		}

		// Calculate the normals
		PVector v1 = new PVector();
		PVector v2 = new PVector();
		PVector perp = new PVector();

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = col + row * width;
				PVector normal = normals[index];
				normal.set(0, 0, 0);

				if (visibilityMask[index]) {
					// Calculate the average normal value at the given point
					PVector point = points[index];
					int counter = 0;

					if (col + 1 < width && visibilityMask[index + 1]) {
						PVector.sub(points[index + 1], point, v1);

						if (row + 1 < height && visibilityMask[index + width]) {
							PVector.sub(points[index + width], point, v2);
							PVector.cross(v1, v2, perp);
							perp.normalize();
							normal.add(perp);
							counter++;
						}

						if (row - 1 >= 0 && visibilityMask[index - width]) {
							PVector.sub(points[index - width], point, v2);
							PVector.cross(v2, v1, perp);
							perp.normalize();
							normal.add(perp);
							counter++;
						}
					}

					if (col - 1 >= 0 && visibilityMask[index - 1]) {
						PVector.sub(points[index - 1], point, v1);

						if (row + 1 < height && visibilityMask[index + width]) {
							PVector.sub(points[index + width], point, v2);
							PVector.cross(v2, v1, perp);
							perp.normalize();
							normal.add(perp);
							counter++;
						}

						if (row - 1 >= 0 && visibilityMask[index - width]) {
							PVector.sub(points[index - width], point, v2);
							PVector.cross(v1, v2, perp);
							perp.normalize();
							normal.add(perp);
							counter++;
						}
					}

					if (counter > 0) {
						normal.normalize();
					}
				}
			}
		}
	}

	/**
	 * Calculates the scan back side points
	 */
	public void calculateBackPoints() {
		// Create the back side points array if necessary
		if (backPoints == null || backPoints.length != nPoints) {
			backPoints = new PVector[nPoints];

			for (int i = 0; i < nPoints; i++) {
				backPoints[i] = new PVector();
			}
		}

		// Calculate the normals if they were not calculated before or the array doesn't have the correct dimensions
		if (normals == null || normals.length != nPoints) {
			calculateNormals();
		}

		// Calculate the back side points
		PVector perp = new PVector();

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = col + row * width;
				PVector backPoint = backPoints[index];
				backPoint.set(points[index]);

				if (visibilityMask[index]) {
					float offset = 0.01f;

					if (col - 1 >= 0 && col + 1 < width && row - 1 >= 0 && row + 1 < height && visibilityMask[index - 1]
							&& visibilityMask[index + 1] && visibilityMask[index - width]
							&& visibilityMask[index + width]) {
						offset *= 10f;
					}

					perp.set(normals[index]).mult(offset);
					backPoint.sub(perp);
				}
			}
		}
	}

	/**
	 * Updates the scan normals
	 */
	public void updateNormals() {
		if (normals != null) {
			calculateNormals();
		}
	}

	/**
	 * Update the scan back side points
	 */
	public void updateBackPoints() {
		if (backPoints != null) {
			calculateBackPoints();
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

		// Update the normals and backPoints arrays
		updateNormals();
		updateBackPoints();
	}

	/**
	 * Updates the scan with the data from another scan
	 * 
	 * @param scan the scan to use to update this scan
	 */
	public void update(Scan scan) {
		// Check if the arrays dimensions are the same
		if (scan.nPoints != nPoints) {
			width = scan.width;
			height = scan.height;
			nPoints = width * height;
			points = new PVector[nPoints];
			colors = new int[nPoints];
			visibilityMask = new boolean[nPoints];

			for (int i = 0; i < nPoints; i++) {
				points[i] = new PVector();
			}

			// Initialize the normals array if necessary
			if (normals != null) {
				normals = new PVector[nPoints];

				for (int i = 0; i < nPoints; i++) {
					normals[i] = new PVector();
				}
			}

			// Initialize the backPoints array if necessary
			if (backPoints != null) {
				backPoints = new PVector[nPoints];

				for (int i = 0; i < nPoints; i++) {
					backPoints[i] = new PVector();
				}
			}
		}

		// Update the main scan arrays
		for (int i = 0; i < nPoints; i++) {
			points[i].set(scan.points[i]);
			colors[i] = scan.colors[i];
			visibilityMask[i] = scan.visibilityMask[i];
		}

		// Update the normals array if necessary
		if (normals != null && scan.normals != null) {
			for (int i = 0; i < nPoints; i++) {
				normals[i].set(scan.normals[i]);
			}
		} else {
			updateNormals();
		}

		// Update the back points array if necessary
		if (backPoints != null && scan.backPoints != null) {
			for (int i = 0; i < nPoints; i++) {
				backPoints[i].set(scan.backPoints[i]);
			}
		} else {
			updateBackPoints();
		}

		// Set the rest of the scan variables
		center.set(scan.center);
		maxPointSeparationSq = scan.maxPointSeparationSq;
	}

	/**
	 * Updates the scan points with those contained in a file
	 * 
	 * @param p the parent Processing applet
	 * @param fileName the file name
	 */
	public void updateFromFile(PApplet p, String fileName) {
		// Load the file lines containing the scan data
		String[] fileLines = p.loadStrings(fileName);

		// The scan dimensions are in the first line
		String[] dimensions = fileLines[0].split(" ");
		int newWidth = Integer.valueOf(dimensions[0]);
		int newHeight = Integer.valueOf(dimensions[1]);

		// Reset the main scan arrays if necessary
		if (newWidth != width || newHeight != height) {
			width = newWidth;
			height = newHeight;
			nPoints = width * height;
			points = new PVector[nPoints];
			colors = new int[nPoints];
			visibilityMask = new boolean[nPoints];

			for (int i = 0; i < nPoints; i++) {
				points[i] = new PVector();
			}
		}

		// Fill the main scan variables
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

				points[i].set(x, y, z);
				colors[i] = (red << 16) | (green << 8) | blue | 0xff000000;
				visibilityMask[i] = true;
				center.add(points[i]);
				counter++;
			} else {
				points[i].set(0, 0, 0);
				colors[i] = 0;
				visibilityMask[i] = false;
			}
		}

		if (counter > 0) {
			center.div(counter);
		}

		// Update the normals and backPoints arrays
		updateNormals();
		updateBackPoints();
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

		// The first line contains the scan dimensions
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

	/**
	 * Creates a copy of the scan object
	 * 
	 * @return the scan copy
	 */
	public Scan copy() {
		// Create an empty scan
		Scan scan = new Scan(width, height);

		// Fill the main scan arrays
		for (int i = 0; i < nPoints; i++) {
			scan.points[i].set(points[i]);
			scan.colors[i] = colors[i];
			scan.visibilityMask[i] = visibilityMask[i];
		}

		// Fill the normals array if necessary
		if (normals != null) {
			scan.normals = new PVector[nPoints];

			for (int i = 0; i < nPoints; i++) {
				scan.normals[i] = normals[i].copy();
			}
		}

		// Fill the backPoints array if necessary
		if (backPoints != null) {
			scan.backPoints = new PVector[nPoints];

			for (int i = 0; i < nPoints; i++) {
				scan.backPoints[i] = backPoints[i].copy();
			}
		}

		// Set the rest of the scan variables
		scan.center.set(center);
		scan.maxPointSeparationSq = maxPointSeparationSq;

		return scan;
	}

	/**
	 * Constrains the points visibilities to a cube delimited by some lower and upper corner coordinates
	 * 
	 * @param corners an array with the lower and upper corners
	 */
	public void constrainPoints(PVector[] corners) {
		super.constrainPoints(corners);

		// Update the normals and backPoints arrays
		updateNormals();
		updateBackPoints();
	}

	/**
	 * Reduces the scan resolution by a given factor, smoothing the points at the same time
	 * 
	 * @param reductionFactor the scale reduction factor
	 */
	public void reduceResolution(int reductionFactor) {
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

			// Update the normals and backPoints arrays
			updateNormals();
			updateBackPoints();
		}
	}

	/**
	 * Translates the scan position and the scan center
	 * 
	 * @param translationVector the translation vector
	 */
	public void translate(PVector translationVector) {
		// Translate the scan points
		for (PVector point : points) {
			point.add(translationVector);
		}

		// Translate the scan center
		center.add(translationVector);

		// Update the backPoints arrays
		updateBackPoints();
	}

	/**
	 * Rotates the scan around the scan center
	 * 
	 * @param rotationAngle the scan rotation angle in radians
	 */
	public void rotate(float rotationAngle) {
		// Rotate the scan points around the scan center
		float cos = (float) Math.cos(rotationAngle);
		float sin = (float) Math.sin(rotationAngle);

		for (PVector point : points) {
			point.sub(center);
			point.set(cos * point.x - sin * point.z, point.y, sin * point.x + cos * point.z);
			point.add(center);
		}

		// Update the normals and backPoints arrays
		updateNormals();
		updateBackPoints();
	}

	/**
	 * Increases or decreases the size of the scan by a given factor
	 * 
	 * @param scaleFactor the size scaling factor
	 */
	public void scale(float scaleFactor) {
		// Scale the scan points
		for (PVector point : points) {
			point.sub(center);
			point.mult(scaleFactor);
			point.add(center);
		}

		// Update the maximum scan separation between points
		setMaxPointSeparation(scaleFactor * getMaxPointSeparation());

		// Update the backPoints arrays
		updateBackPoints();
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
			for (int col = 0; col < width; col++) {
				if (visibilityMask[col + row * width]) {
					if (col < colIni) {
						colIni = col;
					}

					if (col > colEnd) {
						colEnd = col;
					}

					if (row < rowIni) {
						rowIni = row;
					}

					if (row > rowEnd) {
						rowEnd = row;
					}
				}
			}
		}

		// Make sure that the limits make sense
		if (colIni > colEnd) {
			colIni = 0;
			colEnd = 0;
			rowIni = 0;
			rowEnd = 0;
		}

		// Obtain the dimensions of the new cropped arrays
		int widthNew = colEnd - colIni + 1;
		int heightNew = rowEnd - rowIni + 1;

		// Check that the new array dimensions are different to the current ones
		if (widthNew != width || heightNew != height) {
			// Create the new arrays
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

			// Update the normals and backPoints arrays
			updateNormals();
			updateBackPoints();
		}
	}

	/**
	 * Extends the scan arrays dimensions
	 * 
	 * @param widthNew the new arrays horizontal dimension
	 * @param heightNew the new arrays vertical dimension
	 */
	public void extend(int widthNew, int heightNew) {
		// Check that the new array dimensions are larger than the original ones
		if (widthNew >= width && heightNew >= height && (widthNew != width || heightNew != height)) {
			// Create the new extended arrays
			int nPointsNew = widthNew * heightNew;
			PVector[] pointsNew = new PVector[nPointsNew];
			int[] colorsNew = new int[nPointsNew];
			boolean[] visibilityMaskNew = new boolean[nPointsNew];

			for (int i = 0; i < nPointsNew; i++) {
				pointsNew[i] = new PVector();
			}

			// Populate the new arrays
			int startCol = (widthNew - width) / 2;
			int startRow = (heightNew - height) / 2;

			for (int row = 0; row < height; row++) {
				for (int col = 0; col < width; col++) {
					int index = col + row * width;
					int indexNew = (startCol + col) + (startRow + row) * widthNew;
					pointsNew[indexNew].set(points[index]);
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

			// Update the normals and backPoints arrays
			updateNormals();
			updateBackPoints();
		}
	}

	/**
	 * Extends the scan arrays dimensions to have the scan central point in the middle of the arrays
	 */
	public void extendFromCenter() {
		// Obtain the pixel position of the point closest to the scan center in the x,y plane
		int[] centralPointPixel = obtainCentralPoint();

		// Obtain the dimensions of the new extended arrays
		int widthNew = 2 * Math.max(centralPointPixel[0], width - 1 - centralPointPixel[0]) + 1;
		int heightNew = 2 * Math.max(centralPointPixel[1], height - 1 - centralPointPixel[1]) + 1;

		// Check that the new array dimensions are larger than the original ones
		if (widthNew >= width && heightNew >= height && (widthNew != width || heightNew != height)) {
			// Create the new extended arrays
			int nPointsNew = widthNew * heightNew;
			PVector[] pointsNew = new PVector[nPointsNew];
			int[] colorsNew = new int[nPointsNew];
			boolean[] visibilityMaskNew = new boolean[nPointsNew];

			for (int i = 0; i < nPointsNew; i++) {
				pointsNew[i] = new PVector();
			}

			// Populate the new arrays
			int startCol = centralPointPixel[0] > (width - 1 - centralPointPixel[0]) ? 0 : widthNew - width;
			int startRow = centralPointPixel[1] > (height - 1 - centralPointPixel[1]) ? 0 : heightNew - height;

			for (int row = 0; row < height; row++) {
				for (int col = 0; col < width; col++) {
					int index = col + row * width;
					int indexNew = (startCol + col) + (startRow + row) * widthNew;
					pointsNew[indexNew].set(points[index]);
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

			// Update the normals and backPoints arrays
			updateNormals();
			updateBackPoints();
		}
	}

	/**
	 * Obtains the column and row values of the scan point closer to the scan center in the x,y plane
	 * 
	 * @return the column and row values of the scan point closer to the scan center in the x,y plane
	 */
	protected int[] obtainCentralPoint() {
		float minDistanceSq = Float.MAX_VALUE;
		int[] centralPointPixel = new int[] { width / 2, height / 2 };

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = col + row * width;

				if (visibilityMask[index]) {
					PVector point = points[index];
					float distanceSq = PApplet.sq(point.x - center.x) + PApplet.sq(point.y - center.y);

					if (distanceSq < minDistanceSq) {
						minDistanceSq = distanceSq;
						centralPointPixel[0] = col;
						centralPointPixel[1] = row;
					}
				}
			}
		}

		return centralPointPixel;
	}

	/**
	 * Fills the scan holes interpolating between valid points in the same scan row
	 * 
	 * @param maxHoleGap the maximum number of points missing in order to fill the hole
	 */
	public void fillHoles(int maxHoleGap) {
		// Fill the holes row by row
		boolean holesHaveBeenFilled = false;

		for (int row = 0; row < height; row++) {
			for (int col = 1; col < width; col++) {
				int index = col + row * width;

				// Check if we are at the beginning of a hole
				if (!visibilityMask[index] && visibilityMask[index - 1]) {
					// Calculate the limits of the hole
					boolean holeCloses = false;
					int startIndex = index - 1;
					int finishIndex = startIndex;

					for (int i = col + 1; i < width; i++) {
						finishIndex = i + row * width;

						// Check if we found the hole end
						if (visibilityMask[finishIndex]) {
							holeCloses = true;

							// The column loop should continue from the end of the hole
							col = i;
							break;
						}
					}

					// Fill the hole if the gap is not too big
					if (holeCloses && (finishIndex - startIndex - 1) <= maxHoleGap) {
						float delta = 1.0f / (finishIndex - startIndex);
						int startColor = colors[startIndex];
						int finishColor = colors[finishIndex];
						int startRed = (startColor >> 16) & 0xff;
						int startGreen = (startColor >> 8) & 0xff;
						int startBlue = startColor & 0xff;
						float deltaRed = (((finishColor >> 16) & 0xff) - startRed) * delta;
						float deltaGreen = (((finishColor >> 8) & 0xff) - startGreen) * delta;
						float deltaBlue = ((finishColor & 0xff) - startBlue) * delta;
						PVector deltaDirection = PVector.sub(points[finishIndex], points[startIndex]).mult(delta);

						for (int i = startIndex + 1; i < finishIndex; i++) {
							int pixelRed = Math.round(startRed + (i - startIndex) * deltaRed);
							int pixelGreen = Math.round(startGreen + (i - startIndex) * deltaGreen);
							int pixelBlue = Math.round(startBlue + (i - startIndex) * deltaBlue);
							points[i].set(points[i - 1]).add(deltaDirection);
							colors[i] = (pixelRed << 16) | (pixelGreen << 8) | pixelBlue | 0xff000000;
							visibilityMask[i] = true;
						}

						// At least one hole has been filled
						holesHaveBeenFilled = true;
					}
				}
			}
		}

		// Update the normals and backPoints arrays if necessary
		if (holesHaveBeenFilled) {
			updateNormals();
			updateBackPoints();
		}
	}

	/**
	 * Smoothes the scan points using a Gaussian kernel
	 * 
	 * @param kernelSize the kernel size. Should be an odd number larger than 1
	 */
	public void gaussianSmooth(int kernelSize) {
		if (kernelSize > 1) {
			// Make sure that the kernel size is an even number
			if (kernelSize % 2 == 0) {
				kernelSize++;
			}

			// Create the Gaussian kernel
			float[][] kernel = new float[kernelSize][kernelSize];
			int kernelMiddlePoint = (kernelSize - 1) / 2;
			float maxDistanceSq = PApplet.sq(kernelMiddlePoint);
			float sigmaSq = PApplet.sq(kernelMiddlePoint / 2f);

			for (int i = 0; i < kernelSize; i++) {
				for (int j = 0; j < kernelSize; j++) {
					float distanceSq = PApplet.sq(i - kernelMiddlePoint) + PApplet.sq(j - kernelMiddlePoint);

					if (distanceSq <= maxDistanceSq) {
						kernel[i][j] = PApplet.pow(2.718f, -distanceSq / (2 * sigmaSq));
					}
				}
			}

			// Calculate the smoothed points
			PVector[] smoothedPoints = new PVector[nPoints];

			for (int row = 0; row < height; row++) {
				for (int col = 0; col < width; col++) {
					int index = col + row * width;

					if (visibilityMask[index]) {
						PVector pointsSum = new PVector();
						float kernelValueCounter = 0;

						for (int i = 0; i < kernelSize; i++) {
							int rowStep = row - kernelMiddlePoint + i;

							for (int j = 0; j < kernelSize; j++) {
								int colStep = col - kernelMiddlePoint + j;

								if (colStep >= 0 && colStep < width && rowStep >= 0 && rowStep < height) {
									int indexStep = colStep + rowStep * width;

									if (visibilityMask[indexStep]) {
										PVector pointStep = points[indexStep];
										float kernelValue = kernel[i][j];

										if (kernelValue != 0 && connected(points[index], pointStep)) {
											pointsSum.add(kernelValue * pointStep.x, kernelValue * pointStep.y,
													kernelValue * pointStep.z);
											kernelValueCounter += kernelValue;
										}
									}
								}
							}
						}

						smoothedPoints[index] = pointsSum.div(kernelValueCounter);
					} else {
						smoothedPoints[index] = points[index];
					}
				}
			}

			// Update the points array
			points = smoothedPoints;

			// Update the normals and backPoints arrays
			updateNormals();
			updateBackPoints();
		}
	}

	/**
	 * Returns the array index of the scan point that is closest to a given screen position
	 * 
	 * @param p the parent Processing applet
	 * @param xScreen the screen x position
	 * @param yScreen the screen y position
	 * @param searchRadius the radius to search for close points
	 * @return the array index of the point that is closest to the given screen position. Returns -1 if no point is
	 *         found
	 */
	protected int getPointIndexUnderScreenPosition(PApplet p, float xScreen, float yScreen, float searchRadius) {
		// Get all the scan points that are close to the given screen position
		ArrayList<Integer> closePointsIndex = new ArrayList<Integer>();
		ArrayList<Float> closePointsZValue = new ArrayList<Float>();
		ArrayList<Float> closePointsDistanceSq = new ArrayList<Float>();
		float maxDistanceSq = PApplet.sq(searchRadius);
		float maxZValue = -Float.MAX_VALUE;

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = col + row * width;

				if (visibilityMask[index]) {
					// Get the point distance to the given screen position
					PVector point = points[index];
					float distanceSq = PApplet.sq(xScreen - p.screenX(point.x, point.y, point.z))
							+ PApplet.sq(yScreen - p.screenY(point.x, point.y, point.z));

					// Select only those points that are close enough
					if (distanceSq < maxDistanceSq) {
						// Get the point z value on the current 3D view
						float zValue = p.modelZ(point.x, point.y, point.z);

						// Save the highest z value
						if (zValue > maxZValue) {
							maxZValue = zValue;
						}

						// Check if the scan has the back points array
						if (backPoints != null) {
							// Only select those points that are in front of their back points
							PVector backPoint = backPoints[index];

							if (zValue > p.modelZ(backPoint.x, backPoint.y, backPoint.z)) {
								// Save the point information
								closePointsIndex.add(index);
								closePointsZValue.add(zValue);
								closePointsDistanceSq.add(distanceSq);
							}
						} else {
							// Save the point information
							closePointsIndex.add(index);
							closePointsZValue.add(zValue);
							closePointsDistanceSq.add(distanceSq);
						}
					}
				}
			}
		}

		// Get the point that is closest to the screen position and the highest z value
		int closestPointIndex = -1;
		float minDistanceSq = maxDistanceSq;

		for (int i = 0; i < closePointsIndex.size(); i++) {
			float distanceSq = closePointsDistanceSq.get(i) + PApplet.sq(maxZValue - closePointsZValue.get(i));

			if (distanceSq < minDistanceSq) {
				closestPointIndex = closePointsIndex.get(i);
				minDistanceSq = distanceSq;
			}
		}

		return closestPointIndex;
	}

	/**
	 * Sets the scan point that is closest to the given screen position as non visible
	 * 
	 * This method should be called between the Processing pushMatrix and popMatrix methods that affect how the scan is
	 * drawn on the screen
	 * 
	 * @param p the parent Processing applet
	 * @param xScreen the screen x position
	 * @param yScreen the screen y position
	 * @param searchRadius the radius to search for close points
	 */
	public void disolveUnderScreenPosition(PApplet p, float xScreen, float yScreen, float searchRadius) {
		// Get the index of the point that is closest to the screen position
		int index = getPointIndexUnderScreenPosition(p, xScreen, yScreen, searchRadius);

		// Check that there is a close point
		if (index >= 0) {
			// Set the point as non visible
			visibilityMask[index] = false;

			// Update the backPoints array
			updateBackPoints();
		}
	}

	/**
	 * Centers the scan at the point that falls at the given screen position
	 * 
	 * This method should be called between the Processing pushMatrix and popMatrix methods that affect how the scan is
	 * drawn on the screen
	 * 
	 * @param p the parent Processing applet
	 * @param xScreen the screen x position
	 * @param yScreen the screen y position
	 * @param searchRadius the radius to search for close points
	 */
	public void centerAtScreenPosition(PApplet p, float xScreen, float yScreen, float searchRadius) {
		// Get the index of the scan point that is closest to the screen position
		int index = getPointIndexUnderScreenPosition(p, xScreen, yScreen, searchRadius);

		// Check that there is a close point
		if (index >= 0) {
			// Subtract the point coordinates to all the scan points
			translate(PVector.mult(points[index], -1));
		}
	}

	/**
	 * Sets the scan center
	 * 
	 * @param newCenter the new scan center
	 */
	public void setCenter(PVector newCenter) {
		center.set(newCenter);
	}

	/**
	 * Draws a triangle between three Kinect points if they are connected
	 * 
	 * @param p the parent Processing applet
	 * @param index1 the first point index
	 * @param index2 the second point index
	 * @param index3 the third point index
	 * @param useColors use the points colors if true
	 */
	protected void drawTriangle(PApplet p, int index1, int index2, int index3, boolean useColors) {
		if (normals == null) {
			super.drawTriangle(p, index1, index2, index3, useColors);
		} else {
			PVector point1 = points[index1];
			PVector point2 = points[index2];
			PVector point3 = points[index3];

			if (connected(point1, point2) && connected(point1, point3) && connected(point2, point3)) {
				PVector normal1 = normals[index1];
				PVector normal2 = normals[index2];
				PVector normal3 = normals[index3];

				if (useColors) {
					p.fill(colors[index1]);
					p.normal(normal1.x, normal1.y, normal1.z);
					p.vertex(point1.x, point1.y, point1.z);
					p.fill(colors[index2]);
					p.normal(normal2.x, normal2.y, normal2.z);
					p.vertex(point2.x, point2.y, point2.z);
					p.fill(colors[index3]);
					p.normal(normal3.x, normal3.y, normal3.z);
					p.vertex(point3.x, point3.y, point3.z);
				} else {
					p.normal(normal1.x, normal1.y, normal1.z);
					p.vertex(point1.x, point1.y, point1.z);
					p.normal(normal2.x, normal2.y, normal2.z);
					p.vertex(point2.x, point2.y, point2.z);
					p.normal(normal3.x, normal3.y, normal3.z);
					p.vertex(point3.x, point3.y, point3.z);
				}
			}
		}
	}

	/**
	 * Draws a triangle between three Kinect points if they are connected
	 * 
	 * @param p the parent Processing applet
	 * @param index1 the first point index
	 * @param index2 the second point index
	 * @param index3 the third point index
	 * @param useColors use the points colors if true
	 */
	protected void drawBackTriangle(PApplet p, int index1, int index2, int index3) {
		PVector backPoint1 = backPoints[index1];
		PVector backPoint2 = backPoints[index2];
		PVector backPoint3 = backPoints[index3];

		if (connected(backPoint1, backPoint2) && connected(backPoint1, backPoint3)
				&& connected(backPoint2, backPoint3)) {
			PVector normal1 = normals[index1];
			PVector normal2 = normals[index2];
			PVector normal3 = normals[index3];

			p.normal(normal1.x, normal1.y, normal1.z);
			p.vertex(backPoint1.x, backPoint1.y, backPoint1.z);
			p.normal(normal2.x, normal2.y, normal2.z);
			p.vertex(backPoint2.x, backPoint2.y, backPoint2.z);
			p.normal(normal3.x, normal3.y, normal3.z);
			p.vertex(backPoint3.x, backPoint3.y, backPoint3.z);
		}
	}

	/**
	 * Draws the scan back side as triangles on the screen with a uniform color
	 * 
	 * @param p the parent Processing applet
	 * @param trianglesColor the triangles color
	 */
	public void drawBackSide(PApplet p, int trianglesColor) {
		if (backPoints != null) {
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
							drawBackTriangle(p, index, index + 1, index + width);
						} else if (visibilityMask[index + 1 + width]) {
							drawBackTriangle(p, index, index + 1 + width, index + width);
						}
					}

					// Second triangle
					if (visibilityMask[index + 1] && visibilityMask[index + 1 + width]) {
						if (visibilityMask[index + width]) {
							drawBackTriangle(p, index + 1, index + 1 + width, index + width);
						} else if (visibilityMask[index]) {
							drawBackTriangle(p, index, index + 1, index + 1 + width);
						}
					}
				}
			}

			p.endShape();
			p.popStyle();
		}
	}
}
