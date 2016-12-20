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
	public float[] backColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	public float[] effectColor = new float[] { 0.3f, 0.3f, 0.3f, 1.0f };
	public boolean drawMesh = true;
	public boolean drawLines = false;
	public boolean drawPoints = false;
	public int cursorArraySize = 300;

	// Main sketch objects
	public Scan[] scans;
	public Scan scan;
	public float startTime;
	public PShader scanShader;
	public PShader pointShader;
	public PShader lineShader;
	public PImage backgroundImg;
	public Effect effectMask;
	public float[] cursorArray;
	public int cursorCounter;
	public ControlPanel controlPanel;

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

		// Save the starting time
		startTime = millis();

		// Load the scan mesh shader and set its initial uniform values
		scanShader = loadShader(shadersDir + meshShaderFiles[0], shadersDir + meshShaderFiles[1]);
		scanShader.set("illuminateFrontFace", false);
		scanShader.set("backColor", backColor[0], backColor[1], backColor[2], backColor[3]);
		scanShader.set("time", startTime);
		scanShader.set("effect", currentEffect);
		scanShader.set("invertEffect", false);
		scanShader.set("effectColor", effectColor[0], effectColor[1], effectColor[2], effectColor[3]);
		scanShader.set("fillWithColor", false);
		scanShader.set("cursorArraySize", cursorArraySize / 3);

		// Load the scan point shader and set its initial uniform values
		pointShader = loadShader(shadersDir + pointShaderFiles[0], shadersDir + pointShaderFiles[1]);
		pointShader.set("time", startTime);
		pointShader.set("effect", currentEffect);
		pointShader.set("invertEffect", false);
		pointShader.set("effectColor", effectColor[0], effectColor[1], effectColor[2], effectColor[3]);
		pointShader.set("fillWithColor", false);
		pointShader.set("cursorArraySize", cursorArraySize / 3);

		// Load the scan line shader and set its initial uniform values
		lineShader = loadShader(shadersDir + lineShaderFiles[0], shadersDir + lineShaderFiles[1]);
		lineShader.set("time", startTime);
		lineShader.set("effect", currentEffect);
		lineShader.set("invertEffect", false);
		lineShader.set("effectColor", effectColor[0], effectColor[1], effectColor[2], effectColor[3]);
		lineShader.set("fillWithColor", false);
		lineShader.set("cursorArraySize", cursorArraySize / 3);

		// Create the image that will be used as sketch background
		backgroundImg = ImageHelper.createGradientImg(this, color(240), color(100));

		// Initialize the control panel object
		controlPanel = new ControlPanel(this);
		controlPanel.setup();

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

		// Update the shaders uniforms
		updateUniforms();

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
	 * Updates the scan mesh, point and line shader uniforms
	 */
	public void updateUniforms() {
		// Update the shaders time uniform
		float time = millis() - startTime;
		scanShader.set("time", time);
		pointShader.set("time", time);
		lineShader.set("time", time);

		// Update the effects if they are active
		if (currentEffect >= 8 && currentEffect < 11) {
			// Initialize the effect mask if necessary
			if (effectMask == null) {
				int canvasWidth = 500;
				int canvasHeight = 500;

				if (currentEffect == 8) {
					int nBalls = 200;
					float velocity = 0.5f;
					float radius = 10;
					effectMask = new BallsEffect(this, canvasWidth, canvasHeight, nBalls, velocity, radius);
				} else if (currentEffect == 9) {
					int nBalls = 80;
					float velocity = 0.5f;
					float radius = 5;
					effectMask = new BallsEffect(this, canvasWidth, canvasHeight, nBalls, velocity, radius);
				} else if (currentEffect == 10) {
					int nParticles = Math.round(0.12f * canvasWidth * canvasHeight);
					effectMask = new DlaEffect(this, canvasWidth, canvasHeight, nParticles);

					// Add some seeds to start the aggregation
					for (int seed = 0; seed < 20; seed++) {
						int x = (int) (canvasWidth * Math.random());
						int y = (int) (canvasHeight * Math.random());
						((DlaEffect) effectMask).addSeed(x, y);
					}
				}
			}

			// Update the effect mask
			if (currentEffect == 8) {
				effectMask.update(false);
			} else if (currentEffect == 9) {
				effectMask.update(true);
			} else if (currentEffect == 10) {
				effectMask.update(false);
			}

			// Update the shaders mask uniform
			PImage mask = effectMask.getImage();
			scanShader.set("mask", mask);
			pointShader.set("mask", mask);
			lineShader.set("mask", mask);
		} else if (currentEffect == 11) {
			// Initialize the cursor array if necessary
			if (cursorArray == null) {
				cursorArray = new float[cursorArraySize];

				// Set its values outside of the screen
				for (int i = 0; i < cursorArraySize; i++) {
					cursorArray[i] = -100000;
				}

				// Initialize the counter
				cursorCounter = 0;
			}

			// Get the scan point that is closer to the mouse position
			PVector point = scan.getPointUnderScreenPosition(mouseX, mouseY, 20);

			// Set the point outside of the screen if the mouse is not over the scan
			if (point == null) {
				point = new PVector(-100000, -100000, -100000);
			}

			// Update the cursor array with the new point
			int loc = cursorCounter % cursorArraySize;
			cursorArray[loc] = point.x;
			cursorArray[loc + 1] = point.y;
			cursorArray[loc + 2] = point.z;
			cursorCounter += 3;

			// Update the shaders cursor array
			scanShader.set("cursorArray", cursorArray, 3);
			pointShader.set("cursorArray", cursorArray, 3);
			lineShader.set("cursorArray", cursorArray, 3);
		}
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
