package sketches.scanViewer;

import controlP5.ControlEvent;
import jagracar.kinect.containers.Scan;
import jagracar.kinect.util.ImageHelper;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;
import processing.opengl.PShader;

/**
 * A Processing sketch that can be used to visualize and manipulate scans obtained with the Kinect sensor
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class ScanViewerSketch extends PApplet {

	// Sketch control variables
	public String scanDir = "data/scans/";
	public String[] scanFiles = new String[] { "scan1.points", "scan2.points", "scan3.points", "scan4.points",
			"chloe.points", "diego.points" };
	public String shadersDir = "data/shaders/";
	public String[] meshShaderFiles = new String[] { "scanFrag.glsl", "scanVert.glsl" };
	public String[] pointShaderFiles = new String[] { "pointFrag.glsl", "pointVert.glsl" };
	public String[] lineShaderFiles = new String[] { "lineFrag.glsl", "lineVert.glsl" };
	public int currentScanIndex = 0;
	public int currentEffect = 0;
	public int startResolution = 2;
	public int startFillHoleSize = 15;
	public int startSmothness = 2;
	public boolean drawMesh = true;
	public boolean drawLines = false;
	public boolean drawPoints = false;

	// Main sketch objects
	public Scan[] scans;
	public Scan scan;
	public PImage backgroundImg;
	public PShader scanShader;
	public PShader pointShader;
	public PShader lineShader;
	public BallsEffect ballsEffect;
	public ControlPanel controlPanel;
	public float startTime;

	// Scene perspective variables
	public float zoom = 1.2f;
	public float rotX = PI;
	public float rotY = 0;

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
		// Load all the scans
		scans = new Scan[scanFiles.length];

		for (int i = 0; i < scanFiles.length; i++) {
			Scan s = new Scan(this);
			s.updateFromFile(scanDir + scanFiles[i]);
			s.crop();
			scans[i] = s;
		}

		// Use the correct scan
		scan = scans[currentScanIndex].copy();

		// Adapt the scan for the sketch
		scan.reduceResolution(startResolution);
		scan.fillHoles(startFillHoleSize);
		scan.gaussianSmooth(startSmothness);

		// Calculate the scan point normals
		scan.calculateNormals();

		// Calculate the scan meshes
		scan.calculateMesh(true);
		scan.calculatePointsMesh(true, 2);
		scan.calculateLinesMesh(true, 1);

		// Load the scan mesh shader and set its uniform values
		scanShader = loadShader(shadersDir + meshShaderFiles[0], shadersDir + meshShaderFiles[1]);
		scanShader.set("illuminateFrontFace", false);
		scanShader.set("backColor", 1.0f, 1.0f, 1.0f, 1.0f);
		scanShader.set("time", 0.0f);
		scanShader.set("effect", 0);
		scanShader.set("invertEffect", false);
		scanShader.set("effectColor", 0.3f, 0.3f, 0.3f, 1.0f);
		scanShader.set("fillWithColor", false);

		// Load the scan point shader and set its uniform values
		pointShader = loadShader(shadersDir + pointShaderFiles[0], shadersDir + pointShaderFiles[1]);
		pointShader.set("time", 0.0f);
		pointShader.set("effect", 0);
		pointShader.set("invertEffect", false);
		pointShader.set("effectColor", 0.3f, 0.3f, 0.3f, 1.0f);
		pointShader.set("fillWithColor", false);

		// Load the scan line shader and set its uniform values
		lineShader = loadShader(shadersDir + lineShaderFiles[0], shadersDir + lineShaderFiles[1]);
		lineShader.set("time", 0.0f);
		lineShader.set("effect", 0);
		lineShader.set("invertEffect", false);
		lineShader.set("effectColor", 0.3f, 0.3f, 0.3f, 1.0f);
		lineShader.set("fillWithColor", false);

		// Create the image that will be used as sketch background
		backgroundImg = ImageHelper.createGradientImg(this, color(240), color(100));

		// Initialize the balls effect
		ballsEffect = new BallsEffect(this, 500, 500, 80, 0.5f, 5);

		// Initialize the control panel object
		controlPanel = new ControlPanel(this);
		controlPanel.setup();

		// Save the starting time
		startTime = millis();

		// The 3D perspective should affect the points and lines
		hint(ENABLE_STROKE_PERSPECTIVE);
	}

	/**
	 * Draw method
	 */
	public void draw() {
		// Write the frame rate on the screen title
		surface.setTitle("Kinect scan viewer // " + (int) frameRate + " fps");

		// Use the z-buffer to paint all the 3D objects according to their z position
		hint(ENABLE_DEPTH_TEST);

		// Draw the background
		background(backgroundImg);

		// Define the scene lights
		setSceneLights();

		// Position the scene
		translate(width / 2, height / 2, 0);
		rotateX(rotX);
		rotateY(rotY);
		scale(zoom);

		// Set the scan mesh shader and the point and line shader time uniform
		scanShader.set("time", millis() - startTime);
		pointShader.set("time", millis() - startTime);
		lineShader.set("time", millis() - startTime);

		// Update the balls effect if necessary
		if (currentEffect == 8) {
			ballsEffect.update(false);
			scanShader.set("mask", ballsEffect.getImage());
			pointShader.set("mask", ballsEffect.getImage());
			lineShader.set("mask", ballsEffect.getImage());
		} else if (currentEffect == 9) {
			ballsEffect.update(true);
			scanShader.set("mask", ballsEffect.getImage());
			pointShader.set("mask", ballsEffect.getImage());
			lineShader.set("mask", ballsEffect.getImage());
		}

		// Draw the scan
		if (drawMesh) {
			scan.drawMesh(scanShader);
		} else if (drawPoints) {
			scan.drawPointsMesh(pointShader);
		} else if (drawLines) {
			scan.drawLinesMesh(lineShader);
		}

		// Disable the z-buffer to paint the control panel on top of the screen
		hint(DISABLE_DEPTH_TEST);

		// Reset the camera view to position the control panel relative to the 2D plane
		camera();
	}

	/**
	 * Sets the sketch scene lights
	 */
	public void setSceneLights() {
		ambientLight(100, 100, 100);
		directionalLight(255 - 100, 255 - 100, 255 - 100, 0.0f, 0.1f, -0.9f);
		lightSpecular(200, 200, 200);
	}

	/**
	 * Controls the scene angle view when the mouse is dragged
	 */
	public void mouseDragged() {
		// Check that we are not on top of the control panel
		if (!controlPanel.isMouseOver()) {
			noCursor();
			rotX -= map(mouseY - pmouseY, -height, height, -TWO_PI, TWO_PI);
			rotY -= map(mouseX - pmouseX, -width, width, -TWO_PI, TWO_PI);
		} else {
			cursor();
		}
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
		// Check that we are not on top of the control panel
		if (!controlPanel.isMouseOver()) {
			float wheelCount = event.getCount();

			if (wheelCount > 0) {
				zoom *= 1.0 + 0.05 * wheelCount;
			} else {
				zoom /= 1.0 - 0.05 * wheelCount;
			}
		}
	}

	/**
	 * Processes the control panel events
	 * 
	 * @param event the controller event
	 */
	public void controlEvent(ControlEvent event) {
		controlPanel.processEvent(event);
	}

	/**
	 * Executes the Processing sketch
	 * 
	 * @param args arguments to be passed to the sketch
	 */
	static public void main(String[] args) {
		String[] sketchName = new String[] { ScanViewerSketch.class.getName() };

		if (args != null) {
			PApplet.main(concat(sketchName, args));
		} else {
			PApplet.main(sketchName);
		}
	}
}
