package kinectScanner;

import java.util.ArrayList;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;

/**
 * A Processing 3D scanner sketch using the Kinect sensor
 *
 * Select the scan area with the controls (or use the "center in face" option) and press the "take scan" button to
 * capture the 3D points inside the box. Press "save scan" to save them in the sketch directory. Press "take scan" again
 * to take more scans.
 *
 * Do the same for the slit scans.
 *
 * Use http://www.openprocessing.org/sketch/62533 to read and represent the scans.
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class KinectScannerSketch extends PApplet {

	// Sketch control variables
	public boolean drawKinectPoints = true;
	public boolean drawAsBands = true;
	public boolean drawAsPixels = false;
	public boolean drawAsLines = false;
	public boolean monochrome = false;
	public int monochromeColor = 255;
	public int resolution = 2;
	public PVector[] limits = null;
	// public PVector[] limits = new PVector[] { new PVector(-1100, -1500, 0), new PVector(1100, 1000, 3300) };
	public String fileName = "test";
	public String fileDir = "src/kinectScanner/out/";
	public boolean drawBox = false;
	public int framesPerScan = 10;
	public boolean takeScan = false;
	public boolean drawScan = false;
	public boolean saveScan = false;
	public boolean verticalSlitScan = true;
	public boolean rotateSlitScan = false;
	public boolean centerSlitScan = false;
	public boolean takeSlitScan = false;
	public boolean drawSlitScan = true;
	public boolean saveSlitScan = false;
	public boolean takeSculpture = false;
	public boolean drawSculpture = true;
	public boolean saveSculpture = false;
	public boolean oktoberfest = false;
	public boolean handControl = false;

	// Main sketch objects
	public SimpleOpenNI context;
	public KinectPoints kPoints;
	public ScanBox box;
	public Sculpture sculpture;
	public MovingImg bier;
	public MovingImg[] brezeln;
	public PImage backgroundImg;
	public Floor floor;
	public KinectControlPanel controlPanel;
	public Scan scan;
	public Scan slitScan;
	public ArrayList<Scan> scansToAverage = new ArrayList<Scan>();
	public ArrayList<Slit> slits = new ArrayList<Slit>();

	// Scene perspective variables
	public float initZoom = 0.35f;
	public float initRotX = PI;
	public float initRotY = 0;
	public float zoom = initZoom;
	public float rotX = initRotX;
	public float rotY = initRotY;

	// Other internal variables
	private int frameIterator = 0;
	private int scanCounter = 0;
	private int slitScanCounter = 0;
	private int sculptureCounter = 0;
	private boolean handIsEnabled = false;
	private int handGesture = 0;
	private PVector handPosition = null;
	private PVector previousHandPosition = null;

	/**
	 * Sets the default window size
	 */
	public void settings() {
		size(1024, 768, P3D);
	}

	/**
	 * Initializes the main sketch objects
	 */
	public void setup() {
		// Set the sketch perspective
		perspective(radians(45), ((float) width) / ((float) height), 10.0f, 150000.0f);

		// The 3D perspective should also affect the points and lines
		hint(ENABLE_STROKE_PERSPECTIVE);

		// Initialize SimpleOpenNI context
		context = new SimpleOpenNI(this);
		context.setMirror(true);
		context.enableDepth();
		context.enableRGB();
		context.alternativeViewPointDepthToImage();

		// Set the hand gesture that will be used to detect hands
		handGesture = SimpleOpenNI.GESTURE_HAND_RAISE;

		// Initialize the KinectPoints object
		context.update();
		kPoints = new KinectPoints(context.depthMapRealWorld(), context.rgbImage(), context.depthMap(), resolution);

		// Calculate the scene limits if necessary
		if (limits == null) {
			limits = kPoints.calculateLimits();
		}

		// Initialize the scan box
		box = new ScanBox(PVector.add(limits[0], limits[1]).mult(0.5f), 400);

		// Initialize the sculpture
		sculpture = new Sculpture(60f, 30, 10);

		// Initialize the bier object for the Oktoberfest game
		PImage bierImg = loadImage("src/kinectScanner/mass.png");
		bier = new MovingImg(bierImg);
		bier.angle = PI;

		// Initialize the brezel objects for the Oktoberfest game
		PImage brezelImg = loadImage("src/kinectScanner/brezel.png");
		int nBrezeln = 3;
		brezeln = new MovingImg[nBrezeln];

		for (int i = 0; i < nBrezeln; i++) {
			MovingImg brezel = new MovingImg(brezelImg);
			brezel.position = getRandomPosition();
			brezel.velocity.set(0, -5, 0);
			brezeln[i] = brezel;
		}

		// Load the image that will be used as sketch background
		// backgroundImg = loadImage("src/kinectScanner/background.jpg");
		backgroundImg = null;

		// Initialize the sketch floor
		floor = new Floor(this, color(0));

		// Initialize the Kinect control panel object
		controlPanel = new KinectControlPanel(this, 0, 0);

		// Start the control panel. This should come after all the other definitions
		PApplet.runSketch(new String[] { KinectControlPanel.class.getName() }, controlPanel);
	}

	/**
	 * Draw method
	 */
	public void draw() {
		// Write the frame rate on the screen title
		surface.setTitle("Kinect Munich Creative Coding Workshop 2016 // " + (int) frameRate + " fps");

		// Draw the background
		if (backgroundImg != null) {
			background(backgroundImg);
		} else {
			background(220);
		}

		// Update the kinect points
		context.update();
		kPoints.update(context.depthMapRealWorld(), context.rgbImage(), context.depthMap(), resolution);

		// Constrain the Kinect points visibility to the limits defined by the user
		kPoints.constrainPoints(limits);

		// Check if the hand detection and tracking should be enabled or disabled
		if (takeSculpture || oktoberfest || handControl) {
			if (!handIsEnabled) {
				context.enableHand();
				context.startGesture(handGesture);
				handIsEnabled = true;
			}
		} else if (handIsEnabled) {
			context.enableHand(false);
			context.endGesture(handGesture);
			handIsEnabled = false;
			handPosition = null;
			previousHandPosition = null;
		}

		// Control the scene view with the hand
		if (handControl && handPosition != null) {
			rotY += map(handPosition.x - previousHandPosition.x, -2000, 2000, -PI, PI);
			rotX -= map(handPosition.y - previousHandPosition.y, -2000, 2000, -PI, PI);

			if (handPosition.z - previousHandPosition.z > 0) {
				zoom /= map(handPosition.z - previousHandPosition.z, 0, 2000, 1, 2);
			} else {
				zoom *= map(-handPosition.z + previousHandPosition.z, 0, 2000, 1, 2);
			}
		}

		// Position the scene
		translate(width / 2, height / 2, 0);
		rotateX(rotX);
		rotateY(rotY);
		scale(zoom);
		translate(0, 0, -1500);

		// Draw the floor
		floor.draw(this, limits);

		// Draw the scan box
		if (drawBox) {
			box.draw(this, 255);
		}

		// Define the scene lights if we are not using real colors
		if (monochrome) {
			setSceneLights();
		}

		// Check if the Kinect points should be drawn
		if (drawKinectPoints) {
			// Draw the kinect points as bands
			if (drawAsBands) {
				if (monochrome) {
					kPoints.drawAsBands(this, 1, monochromeColor);
				} else {
					kPoints.drawAsBands(this, 1);
				}
			}

			// Draw the kinect points as pixels
			if (drawAsPixels) {
				if (monochrome) {
					kPoints.drawAsPixels(this, 3, monochromeColor);
				} else {
					kPoints.drawAsPixels(this, 3);
				}
			}

			// Draw the kinect points as lines
			if (drawAsLines) {
				if (monochrome) {
					kPoints.drawAsLines(this, 3, monochromeColor);
				} else {
					kPoints.drawAsLines(this, 3);
				}
			}
		}

		// Check if a scan should be taken
		if (takeScan) {
			if (framesPerScan == 1) {
				scan = new Scan(kPoints, box);
				frameIterator = framesPerScan;
			} else {
				scansToAverage.add(new Scan(kPoints, box));
				frameIterator++;
				println("Take scan: Running (frame " + frameIterator + ")");

				if (frameIterator >= framesPerScan) {
					scan = KinectHelper.averageScans(scansToAverage);
				}
			}

			if (frameIterator >= framesPerScan) {
				scansToAverage.clear();
				frameIterator = 0;
				scanCounter++;
				takeScan = false;
				println("Take scan: Done (scan " + scanCounter + ")");
			}
		}

		// Draw the last scan taken
		if (drawScan && !takeScan && scan != null) {
			if (monochrome) {
				scan.drawAsTriangles(this, monochromeColor);
			} else {
				scan.drawAsTriangles(this);
			}
		}

		// Save the last scan taken
		if (saveScan) {
			if (scan != null) {
				String scanFileName = fileDir + fileName + "-" + scanCounter + ".points";
				scan.savePoints(this, scanFileName);
				println("Save scan: 3D points saved in " + scanFileName);
			}

			saveScan = false;
		}

		// Check if a slit scan should be taken
		if (takeSlitScan) {
			slits.add(new Slit(kPoints, box, verticalSlitScan));
			slitScan = KinectHelper.combineSlits(slits, rotateSlitScan, centerSlitScan);
			println("Take slit scan: Running (" + slits.size() + " slits)");
		}

		// Draw the last slit scan taken
		if (drawSlitScan && slitScan != null) {
			if (monochrome) {
				slitScan.drawAsTriangles(this, monochromeColor);
			} else {
				slitScan.drawAsTriangles(this);
			}
		}

		// Save the last slit scan taken
		if (saveSlitScan) {
			if (slitScan != null) {
				slitScanCounter++;
				String slitScanFileName = fileDir + fileName + "-slit" + slitScanCounter + ".points";
				slitScan.savePoints(this, slitScanFileName);
				println("Save slit scan: 3D points saved in " + slitScanFileName);
			}

			saveSlitScan = false;
		}

		// Draw the sculpture
		if (drawSculpture) {
			// Set the scene light if it was not done before
			if (!monochrome) {
				setSceneLights();
			}

			// Draw the sculpture
			sculpture.draw(this, color(230, 100, 100));

			// Draw a small sphere to signal the hand position
			if (handPosition != null) {
				pushStyle();
				pushMatrix();
				noStroke();
				fill(color(255, 20, 20));
				translate(handPosition.x, handPosition.y, handPosition.z);
				sphere(10);
				popMatrix();
				popStyle();
			}
		}

		// Save the last sculpture
		if (saveSculpture) {
			sculptureCounter++;
			String sculptureFileName = fileDir + fileName + "-" + sculptureCounter + ".sculpt";
			sculpture.savePoints(this, sculptureFileName);
			saveSculpture = false;
			println("Save sculpture: control points saved in " + sculptureFileName);
		}

		// Play the Oktoberfest game
		if (oktoberfest) {
			// Update the bier image position to the tacked hand position, plus a small offset in the z direction
			if (handPosition != null) {
				bier.position.set(handPosition.x, handPosition.y, handPosition.z - 60);
				bier.visible = true;
			} else {
				bier.visible = false;
			}

			// Update the brezeln positions
			for (MovingImg brezel : brezeln) {
				brezel.update();

				// Move the brezel to a random position if the hand is close enough or it fell too much
				if ((handPosition != null && brezel.closeToPosition(handPosition))
						|| (brezel.position.y < limits[0].y + 0.1f * (limits[1].y - limits[0].y))) {
					brezel.position = getRandomPosition();
				}
			}

			// Order the brezeln according to their z coordinate (higher values first)
			sortBrezelImages(brezeln);

			// Draw the the bier and the brezeln on the screen
			boolean bierIsDraw = false;

			for (MovingImg brezel : brezeln) {
				if (!bierIsDraw && bier.position.z > brezel.position.z) {
					bier.draw(this);
					bierIsDraw = true;
				}

				brezel.draw(this);
			}

			if (!bierIsDraw) {
				bier.draw(this);
			}
		}
	}

	/**
	 * Sets the sketch scene lights
	 */
	public void setSceneLights() {
		ambientLight(100, 100, 100);
		directionalLight(255 - 100, 255 - 100, 255 - 100, 0, -0.1f, 0.9f);
		lightSpecular(200, 200, 200);
	}

	/**
	 * Returns a random position within some fixed limits
	 * 
	 * @return the random position
	 */
	public PVector getRandomPosition() {
		return new PVector(random(limits[0].x, limits[1].x), 0.8f * limits[1].y, random(600f, 2000f));
	}

	/**
	 * Sorts the given moving images array according to the their z coordinate, from higher to lower z values
	 * 
	 * @param images the moving images array to sort
	 */
	public void sortBrezelImages(MovingImg[] images) {
		int nImages = images.length;

		for (int i = 0; i < nImages - 1; i++) {
			int maxValueIndex = i;
			float maxValue = images[i].position.z;

			for (int j = i + 1; j < nImages; j++) {
				float value = images[j].position.z;

				if (value > maxValue) {
					maxValueIndex = j;
					maxValue = value;
				}
			}

			if (maxValueIndex != i) {
				MovingImg tmp = images[i];
				images[i] = images[maxValueIndex];
				images[maxValueIndex] = tmp;
			}
		}
	}

	/**
	 * Controls the scene angle view when the mouse is dragged
	 */
	public void mouseDragged() {
		noCursor();
		rotX -= map(mouseY - pmouseY, -height, height, -TWO_PI, TWO_PI);
		rotY -= map(mouseX - pmouseX, -width, width, -TWO_PI, TWO_PI);
	}

	/**
	 * Makes the cursor visible again when the mouse is released
	 */
	public void mouseReleased() {
		cursor();
	}

	/**
	 * Controls the scene zoom when the mouse wheel is moved
	 * 
	 * @param event the mouse event
	 */
	public void mouseWheel(MouseEvent event) {
		float wheelCount = event.getCount();

		if (wheelCount > 0) {
			zoom *= 1.0 + 0.05 * wheelCount;
		} else {
			zoom /= 1.0 - 0.05 * wheelCount;
		}
	}

	/**
	 * Starts tracking a hand when a hand gesture is recognized within the sketch limits
	 * 
	 * @param context the SimpleOpenNI context object
	 * @param gestureType the identified gesture type
	 * @param position the position where the gesture was detected
	 */
	public void onCompletedGesture(SimpleOpenNI context, int gestureType, PVector position) {
		println("SimpleOpenNI hand information: Recognized gesture (" + gestureType + ")");

		// Start tracking the hand if its position is inside the sketch limits
		if (position.x > limits[0].x && position.x < limits[1].x && position.y > limits[0].y && position.y < limits[1].y
				&& position.z > limits[0].z && position.z < limits[1].z) {
			context.endGesture(gestureType);
			context.startTrackingHand(position);
			handPosition = position.copy();
			previousHandPosition = position.copy();
		}
	}

	/**
	 * Indicates when a hand is detected
	 * 
	 * @param context the SimpleOpenNI context object
	 * @param handId the hand id
	 * @param position the position where the hand was detected
	 */
	public void onNewHand(SimpleOpenNI context, int handId, PVector position) {
		println("SimpleOpenNI hand information: Hand detected (id: " + handId + ")");
	}

	/**
	 * Tracks the hand position and adds new control points to an active sculpture
	 * 
	 * @param context the SimpleOpenNI context object
	 * @param handId the hand id
	 * @param position the hand position
	 */
	public void onTrackedHand(SimpleOpenNI context, int handId, PVector position) {
		previousHandPosition = handPosition;
		handPosition = position.copy();

		if (takeSculpture) {
			sculpture.addControlPoint(position);
		}
	}

	/**
	 * Resets some of the sketch variables when the hand is lost
	 * 
	 * @param context the SimpleOpenNI context object
	 * @param handId the hand id
	 */
	public void onLostHand(SimpleOpenNI context, int handId) {
		println("SimpleOpenNI hand information: Hand lost (id: " + handId + ")");
		context.startGesture(handGesture);
		handPosition = null;
		previousHandPosition = null;

		// Set the original zoom and orientation view if we were using the hand to control them
		if (handControl) {
			zoom = initZoom;
			rotX = initRotX;
			rotY = initRotY;
		}
	}

	/**
	 * Executes the Processing sketch
	 * 
	 * @param args arguments to be passed to the sketch
	 */
	static public void main(String[] args) {
		String[] sketchName = new String[] { KinectScannerSketch.class.getName() };

		if (args != null) {
			PApplet.main(concat(sketchName, args));
		} else {
			PApplet.main(sketchName);
		}
	}
}
