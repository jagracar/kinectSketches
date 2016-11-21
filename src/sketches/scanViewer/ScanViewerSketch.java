package sketches.scanViewer;

import controlP5.ControlEvent;
import jagracar.kinect.containers.Scan;
import jagracar.kinect.util.ImageHelper;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.opengl.PShader;

/**
 * A Processing sketch that can be used to visualize and manipulate scans obtained with the Kinect sensor
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class ScanViewerSketch extends PApplet {

	// Sketch control variables
	public String fileName = "scan1.points";
	public String scanDir = "data/scans/";
	public String shadersDir = "data/shaders/";
	public PVector limits[];

	// Main sketch objects
	public Scan scan;
	public PImage backgroundImg;
	public ControlPanel controlPanel;
	PShader scanShader;

	// Scene perspective variables
	public float zoom = 1.5f;
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
		// Load the scan
		scan = new Scan(this);
		scan.updateFromFile(scanDir + fileName);
		scan.setCenter(new PVector(0, 0, -100));
		scan.reduceResolution(2);
		scan.crop();
		scan.gaussianSmooth(5);
		scan.calculateNormals();
		scan.fillHoles(15);
		scan.calculateShape(false, color(255f, 50f, 50f));

		// Calculate the sculpture limits to use them for the floor dimensions, adding some offset in the y direction
		limits = scan.calculateLimits();
		limits[0].y -= 200;

		// Create the image that will be used as sketch background
		backgroundImg = ImageHelper.createGradientImg(this, color(240), color(100));

		// Initialize the control panel object
		controlPanel = new ControlPanel(this);
		controlPanel.setup();

		scanShader = loadShader(shadersDir + "scanFrag.glsl", shadersDir + "scanVert.glsl");
		scanShader.set("effect", 4);
		scanShader.set("invertEffect", false);
		scanShader.set("backColor", 1.0f, 1.0f, 1.0f, 1.0f);
		scanShader.set("effectColor", 0.3f, 0.3f, 0.3f, 1.0f);
		scanShader.set("fillWithColor", true);
		scanShader.set("illuminateFrontFace", true);
		//hint(DISABLE_OPTIMIZED_STROKE);
	}

	/**
	 * Draw method
	 */
	public void draw() {
		// Write the frame rate on the screen title
		surface.setTitle("Kinect sculpture viewer // " + (int) frameRate + " fps");

		// Use the z-buffer to paint all the 3D objects according to their z position
		hint(ENABLE_DEPTH_TEST);

		// Draw the background
		if (backgroundImg != null) {
			background(backgroundImg);
		} else {
			background(220);
		}

		// Define the scene lights if we are not using real colors
		setSceneLights();

		// Position the scene
		translate(width / 2f, height / 2f, 0);
		rotateX(rotX);
		rotateY(rotY);
		scale(zoom);

		scanShader.set("time", millis());
		// shader(scanShader);

		// scan.drawAsTriangles();
		scan.drawShape(scanShader);

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
		// Avoid the region covered by the control panel
		if ((mouseX > 240) || (mouseY > 100)) {
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
		float wheelCount = event.getCount();

		if (wheelCount > 0) {
			zoom *= 1.0 + 0.05 * wheelCount;
		} else {
			zoom /= 1.0 - 0.05 * wheelCount;
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
