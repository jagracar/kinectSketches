package jagracar.kinect.containers;

import jagracar.kinect.util.ScanBox;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Subclass of the KinectPoints class. Implements some additional functions to manipulate and save Kinect output data
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class Scan extends KinectPoints {

	/**
	 * The scan central coordinates
	 */
	public PVector center;

	/**
	 * Constructs an empty Scan object with the specified dimensions
	 * 
	 * @param width the arrays horizontal dimension
	 * @param height the arrays vertical dimension
	 */
	public Scan(int width, int height) {
		super(width, height);
		this.center = new PVector();
	}

	/**
	 * Constructs a Scan object using the Kinect points inside the scan box
	 * 
	 * @param kp the KinectPoints object
	 * @param box the scan box from which the scan points will be selected
	 */
	public Scan(KinectPoints kp, ScanBox box) {
		super(kp.width, kp.height);
		this.center = box.center.copy();

		// Update the arrays
		for (int i = 0; i < this.nPoints; i++) {
			PVector point = kp.points[i];
			this.points[i].set(point);
			this.colors[i] = kp.colors[i];
			this.visibilityMask[i] = kp.visibilityMask[i] && box.isInside(point);
		}
	}

	/**
	 * Rotates the scan around the vertical axis
	 * 
	 * @param rotationAngle the scan rotation angle in radians
	 */
	public void rotate(float rotationAngle) {
		float cos = PApplet.cos(rotationAngle);
		float sin = PApplet.sin(rotationAngle);

		for (int i = 0; i < nPoints; i++) {
			PVector point = points[i];
			point.sub(center);
			point.set(cos * point.x - sin * point.z, point.y, sin * point.x + cos * point.z);
			point.add(center);
		}
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
		}
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
		center.set(0, 0, 0);

		// Fill the arrays
		int counter = 0;

		for (int i = 0; i < nPoints; i++) {
			String[] pointsAndColors = fileLines[i + 1].split(" ");

			if (Float.valueOf(pointsAndColors[3]) > 0) {
				points[i] = new PVector(Float.valueOf(pointsAndColors[0]), Float.valueOf(pointsAndColors[1]),
						Float.valueOf(pointsAndColors[2]));
				colors[i] = p.color(Float.valueOf(pointsAndColors[3]), Float.valueOf(pointsAndColors[4]),
						Float.valueOf(pointsAndColors[5]));
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
	}

	/**
	 * Save the scan points and colors on a file
	 * 
	 * @param p the parent Processing applet
	 * @param fileName the file name
	 */
	public void savePoints(PApplet p, String fileName) {
		// Crop the scan to avoid writing unnecessary empty data points
		crop();

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
				lines[i + 1] = point.x + " " + point.y + " " + point.z + " " + p.red(col) + " " + p.green(col) + " "
						+ p.blue(col);
			} else {
				// Use a dummy line if the point should be masked
				lines[i + 1] = "-99" + " " + "-99" + " " + "-99" + " " + "-99" + " " + "-99" + " " + "-99";
			}
		}

		// Save the data on the file
		p.saveStrings(fileName, lines);
	}
}