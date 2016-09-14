package sketches.kinectScanner;

import controlP5.Bang;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Group;
import controlP5.Range;
import controlP5.Slider;
import controlP5.Textfield;
import controlP5.Toggle;
import jagracar.kinect.util.KinectHelper;
import processing.core.PApplet;

/**
 * Class used to display and control the main Kinect sketch variables
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class ControlPanel extends PApplet {

	/**
	 * The main ControlP5 object
	 */
	private ControlP5 cp5;

	/**
	 * The Kinect scanner sketch applet
	 */
	private KinectScannerSketch p;

	/**
	 * The panel horizontal screen position
	 */
	private int panelPosX;

	/**
	 * The panel vertical screen position
	 */
	private int panelPosY;

	/**
	 * The panel window width
	 */
	private int panelWidth = 400;

	/**
	 * The panel window height
	 */
	private int panelHeight = 942;

	/**
	 * The panel window title
	 */
	private String panelTitle = "Kinect control panel";

	/**
	 * The controllers horizontal margin
	 */
	private int marginX = 10;

	/**
	 * The controller vertical margin
	 */
	private int marginY = 10;

	/**
	 * The horizontal separation between controllers
	 */
	private int deltaX = 110;

	/**
	 * The vertical separation between controllers
	 */
	private int deltaY = 30;

	/**
	 * The controllers label padding
	 */
	private int padding = 10;

	/**
	 * The controllers group bar height
	 */
	private int groupBarHeight = 25;

	/**
	 * The controllers group background color
	 */
	private int groupBackgroundColor = color(255, 40);

	/**
	 * The buttons size in both dimensions
	 */
	private int buttonSize = 15;

	/**
	 * The slider controllers width
	 */
	private int sliderWidth = 300;

	/**
	 * The text field controller height
	 */
	private int textfieldHeight = 19;

	/**
	 * And internal counter to control the Kinect points drawing mode
	 */
	private int drawingModeIterator = 0;

	/**
	 * An array with the different drawing mode labels
	 */
	private String[] drawingModes = new String[] { "As bands", "As pixels", "As lines" };

	/**
	 * And internal counter to control the Kinect points color
	 */
	private int drawingColorIterator = 0;

	/**
	 * An array with the different drawing color labels
	 */
	private String[] drawingColors = new String[] { "Real colors", "Red", "Green", "Blue" };

	/**
	 * Constructs the ControlPanel object
	 * 
	 * @param p the main Kinect scanner sketch applet
	 * @param panelPosX the control panel x position
	 * @param panelPosY the control panel y position
	 */
	public ControlPanel(KinectScannerSketch p, int panelPosX, int panelPosY) {
		this.p = p;
		this.panelPosX = panelPosX;
		this.panelPosY = panelPosY;
	}

	/**
	 * Sets the control panel window dimensions
	 */
	public void settings() {
		size(panelWidth, panelHeight);
	}

	/**
	 * Adds and initializes all the panel controllers
	 */
	public void setup() {
		// Move the control panel to the desired screen position and change the title
		surface.setLocation(panelPosX, panelPosY);
		surface.setTitle(panelTitle);

		// Create the ControlP5 object
		cp5 = new ControlP5(this);

		// Do not broadcast events yet
		cp5.setBroadcast(false);

		// Set the drawing mode and color iterator values
		drawingModeIterator = p.drawAsBands ? 0 : (p.drawAsPixels ? 1 : 2);
		drawingColorIterator = 0;

		// General parameters group controllers
		Group generalGroup = cp5.addGroup("generalGroup");
		generalGroup.setPosition(marginX, marginY + groupBarHeight);
		generalGroup.setSize(panelWidth - 2 * marginX, marginY + 6 * deltaY);
		generalGroup.setBarHeight(groupBarHeight);
		generalGroup.setBackgroundColor(groupBackgroundColor);
		generalGroup.setCaptionLabel("General parameters");
		generalGroup.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
		generalGroup.disableCollapse();

		Toggle toogle = cp5.addToggle("drawKinectPoints");
		toogle.setPosition(marginX, marginY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(p.drawKinectPoints);
		toogle.setCaptionLabel("Draw points");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(generalGroup);

		Bang bang = cp5.addBang("drawingMode");
		bang.setPosition(marginX + deltaX, marginY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel(drawingModes[drawingModeIterator]);
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(generalGroup);

		bang = cp5.addBang("drawingColors");
		bang.setPosition(marginX + 2 * deltaX, marginY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel(drawingColors[drawingColorIterator]);
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(generalGroup);

		Slider slider = cp5.addSlider("resolution");
		slider.setPosition(marginX, marginY + deltaY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setRange(1, 10);
		slider.setValue(p.resolution);
		slider.setNumberOfTickMarks(10);
		slider.showTickMarks(false);
		slider.setCaptionLabel("Resolution");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(generalGroup);

		Range range = cp5.addRange("xRange");
		range.setPosition(marginX, marginY + 2 * deltaY);
		range.setSize(sliderWidth, buttonSize);
		range.setRange(p.limits[0].x - 0.1f * (p.limits[1].x - p.limits[0].x),
				p.limits[1].x + 0.1f * (p.limits[1].x - p.limits[0].x));
		range.setRangeValues(p.limits[0].x, p.limits[1].x);
		range.setCaptionLabel("X limits");
		range.getCaptionLabel().setPaddingX(padding);
		range.setGroup(generalGroup);

		range = cp5.addRange("yRange");
		range.setPosition(marginX, marginY + 3 * deltaY);
		range.setSize(sliderWidth, buttonSize);
		range.setRange(p.limits[0].y - 0.1f * (p.limits[1].y - p.limits[0].y),
				p.limits[1].y + 0.1f * (p.limits[1].y - p.limits[0].y));
		range.setRangeValues(p.limits[0].y, p.limits[1].y);
		range.setCaptionLabel("Y limits");
		range.getCaptionLabel().setPaddingX(padding);
		range.setGroup(generalGroup);

		range = cp5.addRange("zRange");
		range.setPosition(marginX, marginY + 4 * deltaY);
		range.setSize(sliderWidth, buttonSize);
		range.setRange(p.limits[0].z - 0.1f * (p.limits[1].z - p.limits[0].z),
				p.limits[1].z + 0.1f * (p.limits[1].z - p.limits[0].z));
		range.setRangeValues(p.limits[0].z, p.limits[1].z);
		range.setCaptionLabel("Z limits");
		range.getCaptionLabel().setPaddingX(padding);
		range.setGroup(generalGroup);

		Textfield textfield = cp5.addTextfield("fileName");
		textfield.setPosition(marginX, marginY + 5 * deltaY - (textfieldHeight - buttonSize) / 2);
		textfield.setSize(sliderWidth, textfieldHeight);
		textfield.getValueLabel().setFont(createFont("arial", 15));
		textfield.setAutoClear(false);
		textfield.setCaptionLabel("File name");
		textfield.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		textfield.setGroup(generalGroup);

		// Scan box group controllers
		Group scanBoxGroup = cp5.addGroup("scanBoxGroup");
		scanBoxGroup.setPosition(marginX, 2 * (marginY + groupBarHeight) + marginY + 6 * deltaY);
		scanBoxGroup.setSize(panelWidth - 2 * marginX, marginY + 5 * deltaY);
		scanBoxGroup.setBarHeight(groupBarHeight);
		scanBoxGroup.setBackgroundColor(groupBackgroundColor);
		scanBoxGroup.setCaptionLabel("Scan box");
		scanBoxGroup.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
		scanBoxGroup.disableCollapse();

		toogle = cp5.addToggle("drawBox");
		toogle.setPosition(marginX, marginY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(p.drawBox);
		toogle.setCaptionLabel("Draw box");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(scanBoxGroup);

		bang = cp5.addBang("centerInFace");
		bang.setPosition(marginX + deltaX, marginY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel("Center in face using OpenCV 2");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(scanBoxGroup);

		slider = cp5.addSlider("boxSize");
		slider.setPosition(marginX, marginY + deltaY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setRange(10, 500);
		slider.setValue(p.box.size);
		slider.setCaptionLabel("Size");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(scanBoxGroup);

		slider = cp5.addSlider("xBox");
		slider.setPosition(marginX, marginY + 2 * deltaY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setRange(p.limits[0].x, p.limits[1].x);
		slider.setValue(p.box.center.x);
		slider.setCaptionLabel("X pos");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(scanBoxGroup);

		slider = cp5.addSlider("yBox");
		slider.setPosition(marginX, marginY + 3 * deltaY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setRange(p.limits[0].y, p.limits[1].y);
		slider.setValue(p.box.center.y);
		slider.setCaptionLabel("Y pos");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(scanBoxGroup);

		slider = cp5.addSlider("zBox");
		slider.setPosition(marginX, marginY + 4 * deltaY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setRange(p.limits[0].z, p.limits[1].z);
		slider.setValue(p.box.center.z);
		slider.setCaptionLabel("Z pos");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(scanBoxGroup);

		// Scan group controllers
		Group scanGroup = cp5.addGroup("scanGroup");
		scanGroup.setPosition(marginX, 3 * (marginY + groupBarHeight) + 2 * marginY + 11 * deltaY);
		scanGroup.setSize(panelWidth - 2 * marginX, marginY + 3 * deltaY);
		scanGroup.setBarHeight(groupBarHeight);
		scanGroup.setBackgroundColor(groupBackgroundColor);
		scanGroup.setCaptionLabel("Scan");
		scanGroup.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
		scanGroup.disableCollapse();

		slider = cp5.addSlider("framesPerScan");
		slider.setPosition(marginX, marginY);
		slider.setSize(sliderWidth - 30, buttonSize);
		slider.setRange(1, 30);
		slider.setValue(p.framesPerScan);
		slider.setNumberOfTickMarks(30);
		slider.showTickMarks(false);
		slider.setCaptionLabel("Frames per scan");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(scanGroup);

		bang = cp5.addBang("takeScan");
		bang.setPosition(marginX, marginY + deltaY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel("Take scan");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(scanGroup);

		toogle = cp5.addToggle("drawScan");
		toogle.setPosition(marginX + deltaX, marginY + deltaY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(p.drawScan);
		toogle.setCaptionLabel("Draw scan");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(scanGroup);

		bang = cp5.addBang("saveScan");
		bang.setPosition(marginX, marginY + 2 * deltaY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel("Save scan");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(scanGroup);

		// Slit scan group controllers
		Group slitScanGroup = cp5.addGroup("slitScanGroup");
		slitScanGroup.setPosition(marginX, 4 * (marginY + groupBarHeight) + 3 * marginY + 14 * deltaY);
		slitScanGroup.setSize(panelWidth - 2 * marginX, marginY + 3 * deltaY);
		slitScanGroup.setBarHeight(groupBarHeight);
		slitScanGroup.setBackgroundColor(groupBackgroundColor);
		slitScanGroup.setCaptionLabel("Slit scan");
		slitScanGroup.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
		slitScanGroup.disableCollapse();

		bang = cp5.addBang("orientationSlitScan");
		bang.setPosition(marginX, marginY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel(p.verticalSlitScan ? "Vertical slit" : "Horizontal slit");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(slitScanGroup);

		toogle = cp5.addToggle("rotateSlitScan");
		toogle.setPosition(marginX + deltaX, marginY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(p.rotateSlitScan);
		toogle.setCaptionLabel("Rotate");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(slitScanGroup);

		toogle = cp5.addToggle("centerSlitScan");
		toogle.setPosition(marginX + 2 * deltaX, marginY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(p.centerSlitScan);
		toogle.setCaptionLabel("Move with the box");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(slitScanGroup);

		bang = cp5.addBang("slitScanBang");
		bang.setPosition(marginX, marginY + deltaY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel("Start slit scan");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(slitScanGroup);

		toogle = cp5.addToggle("drawSlitScan");
		toogle.setPosition(marginX + deltaX, marginY + deltaY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(p.drawSlitScan);
		toogle.setCaptionLabel("Draw slit scan");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(slitScanGroup);

		bang = cp5.addBang("clearSlitScan");
		bang.setPosition(marginX + 2 * deltaX, marginY + deltaY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel("Clear");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(slitScanGroup);

		bang = cp5.addBang("saveSlitScan");
		bang.setPosition(marginX, marginY + 2 * deltaY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel("Save slit scan");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(slitScanGroup);

		// Sculpture group controllers
		Group sculptureGroup = cp5.addGroup("sculptureGroup");
		sculptureGroup.setPosition(marginX, 5 * (marginY + groupBarHeight) + 4 * marginY + 17 * deltaY);
		sculptureGroup.setSize(panelWidth - 2 * marginX, marginY + 4 * deltaY);
		sculptureGroup.setBarHeight(groupBarHeight);
		sculptureGroup.setBackgroundColor(groupBackgroundColor);
		sculptureGroup.setCaptionLabel("Sculpture");
		sculptureGroup.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
		sculptureGroup.disableCollapse();

		slider = cp5.addSlider("sculptureRadius");
		slider.setPosition(marginX, marginY);
		slider.setSize(sliderWidth - 30, buttonSize);
		slider.setRange(10, 100);
		slider.setValue(p.sculpture.getSectionRadius());
		slider.setNumberOfTickMarks(91);
		slider.showTickMarks(false);
		slider.setCaptionLabel("Radius");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(sculptureGroup);

		slider = cp5.addSlider("sculptureSides");
		slider.setPosition(marginX, marginY + deltaY);
		slider.setSize(sliderWidth - 30, buttonSize);
		slider.setRange(2, 50);
		slider.setValue(p.sculpture.getSectionSides());
		slider.setNumberOfTickMarks(49);
		slider.showTickMarks(false);
		slider.setCaptionLabel("Circular detail");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(sculptureGroup);

		bang = cp5.addBang("sculptureBang");
		bang.setPosition(marginX, marginY + 2*deltaY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel("Start sculpture");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(sculptureGroup);

		toogle = cp5.addToggle("drawSculpture");
		toogle.setPosition(marginX + deltaX, marginY + 2*deltaY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(p.drawSculpture);
		toogle.setCaptionLabel("Draw sculpture");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(sculptureGroup);

		bang = cp5.addBang("clearSculpture");
		bang.setPosition(marginX + 2 * deltaX, marginY + 2*deltaY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel("Clear");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(sculptureGroup);

		bang = cp5.addBang("saveSculpture");
		bang.setPosition(marginX, marginY + 3 * deltaY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel("Save sculpture");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(sculptureGroup);

		// Other effects group controllers
		Group otherEffectsGroup = cp5.addGroup("otherEffectsGroup");
		otherEffectsGroup.setPosition(marginX, 6 * (marginY + groupBarHeight) + 5 * marginY + 21 * deltaY);
		otherEffectsGroup.setSize(panelWidth - 2 * marginX, marginY + deltaY);
		otherEffectsGroup.setBarHeight(groupBarHeight);
		otherEffectsGroup.setBackgroundColor(groupBackgroundColor);
		otherEffectsGroup.setCaptionLabel("Other effects");
		otherEffectsGroup.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
		otherEffectsGroup.disableCollapse();

		toogle = cp5.addToggle("oktoberfest");
		toogle.setPosition(marginX, marginY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(p.oktoberfest);
		toogle.setCaptionLabel("Oktoberfest fun!");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(otherEffectsGroup);

		bang = cp5.addBang("recenterBang");
		bang.setPosition(marginX + deltaX, marginY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel("Recenter");
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(otherEffectsGroup);

		toogle = cp5.addToggle("handControl");
		toogle.setPosition(marginX + 2 * deltaX, marginY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(p.handControl);
		toogle.setCaptionLabel("Control with the hand");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(otherEffectsGroup);

		// Add the control listener
		cp5.addListener(new ControlListener() {

			@Override
			public void controlEvent(ControlEvent event) {
				processEvent(event);
			}
		});
		
		// Start broadcasting events
		cp5.setBroadcast(true);
	}

	/**
	 * Cleans the control panel window in each loop
	 */
	public void draw() {
		background(0);
	}

	/**
	 * Processes the control panel events
	 * 
	 * @param event the controller event
	 */
	void processEvent(ControlEvent event) {
		String controllerGroupName = event.getController().getParent().getName();

		if (controllerGroupName.equals("generalGroup")) {
			processGeneralEvent(event);
		} else if (controllerGroupName.equals("scanBoxGroup")) {
			processScanBoxEvent(event);
		} else if (controllerGroupName.equals("scanGroup")) {
			processScanEvent(event);
		} else if (controllerGroupName.equals("slitScanGroup")) {
			processSlitScanEvent(event);
		} else if (controllerGroupName.equals("sculptureGroup")) {
			processSculptureEvent(event);
		} else if (controllerGroupName.equals("otherEffectsGroup")) {
			processOtherEffectsEvent(event);
		}
	}

	/**
	 * Processes the general parameters group events
	 * 
	 * @param event the controller event
	 */
	private void processGeneralEvent(ControlEvent event) {
		Controller<?> controller = event.getController();
		String controllerName = controller.getName();

		if (controllerName.equals("drawKinectPoints")) {
			p.drawKinectPoints = ((Toggle) controller).getBooleanValue();
		} else if (controllerName.equals("drawingMode")) {
			drawingModeIterator = drawingModeIterator == 2 ? 0 : drawingModeIterator + 1;
			controller.setCaptionLabel(drawingModes[drawingModeIterator]);

			switch (drawingModeIterator) {
			case 0:
				p.drawAsBands = true;
				p.drawAsPixels = false;
				p.drawAsLines = false;
				break;
			case 1:
				p.drawAsBands = false;
				p.drawAsPixels = true;
				p.drawAsLines = false;
				break;
			case 2:
				p.drawAsBands = false;
				p.drawAsPixels = false;
				p.drawAsLines = true;
				break;
			}
		} else if (controllerName.equals("drawingColors")) {
			drawingColorIterator = drawingColorIterator == 3 ? 0 : drawingColorIterator + 1;
			controller.setCaptionLabel(drawingColors[drawingColorIterator]);

			switch (drawingColorIterator) {
			case 0:
				p.monochrome = false;
				p.monochromeColor = p.color(255, 255, 255);
				break;
			case 1:
				p.monochrome = true;
				p.monochromeColor = p.color(220, 50, 50);
				break;
			case 2:
				p.monochrome = true;
				p.monochromeColor = p.color(50, 220, 50);
				break;
			case 3:
				p.monochrome = true;
				p.monochromeColor = p.color(50, 50, 220);
				break;
			}
		} else if (controllerName.equals("resolution")) {
			p.resolution = Math.round(controller.getValue());
		} else if (controllerName.equals("xRange")) {
			p.limits[0].x = controller.getArrayValue(0);
			p.limits[1].x = controller.getArrayValue(1);
		} else if (controllerName.equals("yRange")) {
			p.limits[0].y = controller.getArrayValue(0);
			p.limits[1].y = controller.getArrayValue(1);
		} else if (controllerName.equals("zRange")) {
			p.limits[0].z = controller.getArrayValue(0);
			p.limits[1].z = controller.getArrayValue(1);
		} else if (controllerName.equals("fileName")) {
			p.fileName = ((Textfield) controller).getText();
			System.out.println("Data will be saved in " + p.outputDir + p.fileName + "-*.*");
		}
	}

	/**
	 * Processes the scan box group events
	 * 
	 * @param event the controller event
	 */
	private void processScanBoxEvent(ControlEvent event) {
		Controller<?> controller = event.getController();
		String controllerName = controller.getName();

		if (controllerName.equals("drawBox")) {
			p.drawBox = ((Toggle) controller).getBooleanValue();
		} else if (controllerName.equals("centerInFace")) {
			boolean boxCentered = p.box.centerInFace(p, p.kPoints);

			if (boxCentered) {
				cp5.getController("xBox").setValue(p.box.center.x);
				cp5.getController("yBox").setValue(p.box.center.y);
				cp5.getController("zBox").setValue(p.box.center.z);
			}
		} else if (controllerName.equals("boxSize")) {
			p.box.size = controller.getValue();
		} else if (controllerName.equals("xBox")) {
			p.box.center.x = controller.getValue();
		} else if (controllerName.equals("yBox")) {
			p.box.center.y = controller.getValue();
		} else if (controllerName.equals("zBox")) {
			p.box.center.z = controller.getValue();
		}
	}

	/**
	 * Processes the scan group events
	 * 
	 * @param event the controller event
	 */
	private void processScanEvent(ControlEvent event) {
		Controller<?> controller = event.getController();
		String controllerName = controller.getName();

		if (controllerName.equals("framesPerScan")) {
			p.framesPerScan = Math.round(controller.getValue());
		} else if (controllerName.equals("takeScan")) {
			p.takeScan = true;
		} else if (controllerName.equals("drawScan")) {
			p.drawScan = ((Toggle) controller).getBooleanValue();
		} else if (controllerName.equals("saveScan")) {
			p.saveScan = true;
		}
	}

	/**
	 * Processes the slit scan group events
	 * 
	 * @param event the controller event
	 */
	private void processSlitScanEvent(ControlEvent event) {
		Controller<?> controller = event.getController();
		String controllerName = controller.getName();

		if (controllerName.equals("orientationSlitScan")) {
			p.verticalSlitScan = !p.verticalSlitScan;

			if (p.verticalSlitScan) {
				controller.setCaptionLabel("Vertical slit");
			} else {
				controller.setCaptionLabel("Horizontal slit");
			}
		} else if (controllerName.equals("rotateSlitScan")) {
			p.rotateSlitScan = ((Toggle) controller).getBooleanValue();

			if (!p.takeSlitScan && p.slits.size() > 0) {
				p.slitScan = KinectHelper.combineSlits(p.slits, p.rotateSlitScan, p.centerSlitScan);
			}
		} else if (controllerName.equals("centerSlitScan")) {
			p.centerSlitScan = ((Toggle) controller).getBooleanValue();

			if (!p.takeSlitScan && p.slits.size() > 0) {
				p.slitScan = KinectHelper.combineSlits(p.slits, p.rotateSlitScan, p.centerSlitScan);
			}
		} else if (controllerName.equals("slitScanBang")) {
			p.takeSlitScan = !p.takeSlitScan;

			if (p.takeSlitScan) {
				controller.setCaptionLabel("Stop slit scan");
			} else {
				controller.setCaptionLabel("Restart slit scan");
			}
		} else if (controllerName.equals("drawSlitScan")) {
			p.drawSlitScan = ((Toggle) controller).getBooleanValue();
		} else if (controllerName.equals("clearSlitScan")) {
			p.slits.clear();
			p.slitScan = null;

			if (!p.takeSlitScan) {
				cp5.getController("slitScanBang").setCaptionLabel("Start slit scan");
			}
		} else if (controllerName.equals("saveSlitScan")) {
			p.saveSlitScan = true;
		}
	}

	/**
	 * Processes the sculpture group events
	 * 
	 * @param event the controller event
	 */
	private void processSculptureEvent(ControlEvent event) {
		Controller<?> controller = event.getController();
		String controllerName = controller.getName();

		if (controllerName.equals("sculptureRadius")) {
			p.sculpture.setSectionRadius(controller.getValue());
		} else if (controllerName.equals("sculptureSides")) {
			p.sculpture.setSectionSides(Math.round(controller.getValue()));
		} else if (controllerName.equals("sculptureBang")) {
			p.takeSculpture = !p.takeSculpture;

			if (p.takeSculpture) {
				controller.setCaptionLabel("Stop sculpture");
			} else {
				controller.setCaptionLabel("Restart sculpture");
			}
		} else if (controllerName.equals("drawSculpture")) {
			p.drawSculpture = ((Toggle) controller).getBooleanValue();
		} else if (controllerName.equals("clearSculpture")) {
			p.sculpture.clear();

			if (!p.takeSculpture) {
				cp5.getController("sculptureBang").setCaptionLabel("Start sculpture");
			}
		} else if (controllerName.equals("saveSculpture")) {
			p.saveSculpture = true;
		}
	}

	/**
	 * Processes the other effects group events
	 * 
	 * @param event the controller event
	 */
	private void processOtherEffectsEvent(ControlEvent event) {
		Controller<?> controller = event.getController();
		String controllerName = controller.getName();

		if (controllerName.equals("oktoberfest")) {
			p.oktoberfest = ((Toggle) controller).getBooleanValue();

			if (p.oktoberfest && p.handControl) {
				cp5.getController("handControl").setValue(0);
			}
		} else if (controllerName.equals("recenterBang")) {
			p.zoom = p.initZoom;
			p.rotX = p.initRotX;
			p.rotY = p.initRotY;
		} else if (controllerName.equals("handControl")) {
			p.handControl = ((Toggle) controller).getBooleanValue();

			if (!p.handControl) {
				p.zoom = p.initZoom;
				p.rotX = p.initRotX;
				p.rotY = p.initRotY;
			}

			if (p.oktoberfest && p.handControl) {
				cp5.getController("oktoberfest").setValue(0);
			}
		}
	}
}
