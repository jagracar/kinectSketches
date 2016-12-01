package sketches.scanViewer;

import controlP5.Bang;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Group;
import controlP5.ScrollableList;
import controlP5.Slider;
import controlP5.Toggle;

/**
 * Class used to control the Kinect scan viewer sketch
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class ControlPanel {

	/**
	 * The main ControlP5 object
	 */
	private ControlP5 cp5;

	/**
	 * The scan viewer sketch applet
	 */
	private ScanViewerSketch p;

	/**
	 * The controllers horizontal margin
	 */
	private int marginX = 7;

	/**
	 * The controller vertical margin
	 */
	private int marginY = 7;

	/**
	 * The horizontal separation between controllers
	 */
	private int deltaX = 100;

	/**
	 * The vertical separation between controllers
	 */
	private int deltaY = 25;

	/**
	 * The controllers label padding
	 */
	private int padding = 10;

	/**
	 * The buttons size in both dimensions
	 */
	private int buttonSize = 15;

	/**
	 * The slider controllers width
	 */
	private int sliderWidth = 130;

	/**
	 * The group width
	 */
	private int groupWidth = 220;

	/**
	 * The group bar height
	 */
	private int groupBarHeight = 20;

	/**
	 * The group background color
	 */
	private int groupBackgroundColor = 0x33ffffff;

	/**
	 * And internal counter to control the scan drawing mode
	 */
	private int drawingModeIterator = 0;

	/**
	 * An array with the different drawing mode labels
	 */
	private String[] drawingModes = new String[] { "Draw mesh", "Draw points", "Draw lines" };

	/**
	 * Constructs the ControlPanel object
	 * 
	 * @param p the main scan viewer sketch applet
	 */
	public ControlPanel(ScanViewerSketch p) {
		this.p = p;
	}

	/**
	 * Adds and initializes all the panel controllers
	 */
	public void setup() {
		// Create the ControlP5 object
		cp5 = new ControlP5(p);

		// Change the default colors
		cp5.setColorForeground(0xff666666);
		cp5.setColorBackground(0xff999999);
		cp5.setColorActive(0xffffff88);
		cp5.setColorCaptionLabel(0xff111111);
		cp5.setColorValueLabel(0xff111111);

		// Do not broadcast events yet
		cp5.setBroadcast(false);

		// Set the drawing mode and color iterator values
		drawingModeIterator = p.drawMesh ? 0 : (p.drawPoints ? 1 : 2);

		// Create the main group that will contain the rest of the groups
		Group mainGroup = cp5.addGroup("mainGroup");
		mainGroup.setPosition(marginX, marginY + groupBarHeight);
		mainGroup.setSize(groupWidth, 5 * marginY + 6 * buttonSize + 7 * deltaY);
		mainGroup.setBarHeight(groupBarHeight);
		mainGroup.setBackgroundColor(groupBackgroundColor);
		mainGroup.setCaptionLabel("Controls");
		mainGroup.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);

		// Create the scan group and add its controllers
		Group scanGroup = cp5.addGroup("scanGroup");
		scanGroup.setPosition(marginX, marginY);
		scanGroup.setSize(groupWidth - 2 * marginX, marginY + 2 * buttonSize + 5 * deltaY);
		scanGroup.setBarHeight(0);
		scanGroup.setBackgroundColor(groupBackgroundColor);
		scanGroup.getCaptionLabel().setVisible(false);
		scanGroup.disableCollapse();
		scanGroup.setGroup(mainGroup);

		ScrollableList list = cp5.addScrollableList("scanFace");
		list.setPosition(marginX, marginY);
		list.setSize(sliderWidth, 3 * buttonSize);
		list.setBarHeight(0);
		list.setBarVisible(false);
		list.setItemHeight(buttonSize);
		list.setType(ScrollableList.LIST);
		list.addItem("Face 1", 0);
		list.addItem("Face 2", 1);
		list.addItem("Face 3", 2);
		list.addItem("Face 4", 3);
		list.addItem("Dog 1", 4);
		list.addItem("Dog 2", 5);
		list.setValue(p.currentScanIndex);
		list.setGroup(scanGroup);

		Slider slider = cp5.addSlider("resolution");
		slider.setPosition(marginX, marginY + 2 * buttonSize + deltaY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setRange(1, 10);
		slider.setValue(p.startResolution);
		slider.setNumberOfTickMarks(10);
		slider.showTickMarks(false);
		slider.setCaptionLabel("Resolution");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(scanGroup);

		slider = cp5.addSlider("smoothness");
		slider.setPosition(marginX, marginY + 2 * buttonSize + 2 * deltaY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setRange(1, 10);
		slider.setValue(p.startSmothness);
		slider.setNumberOfTickMarks(10);
		slider.showTickMarks(false);
		slider.setCaptionLabel("Smoothness");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(scanGroup);

		slider = cp5.addSlider("fillHoleSize");
		slider.setPosition(marginX, marginY + 2 * buttonSize + 3 * deltaY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setRange(1, 50);
		slider.setValue(p.startFillHoleSize);
		slider.setNumberOfTickMarks(50);
		slider.showTickMarks(false);
		slider.setCaptionLabel("Fill hole size");
		slider.getCaptionLabel().setPaddingX(padding);
		slider.setGroup(scanGroup);

		Bang bang = cp5.addBang("drawingMode");
		bang.setPosition(marginX, marginY + 2 * buttonSize + 4 * deltaY);
		bang.setSize(buttonSize, buttonSize);
		bang.setCaptionLabel(drawingModes[drawingModeIterator]);
		bang.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		bang.setGroup(scanGroup);

		Toggle toogle = cp5.addToggle("useNormals");
		toogle.setPosition(marginX + deltaX, marginY + 2 * buttonSize + 4 * deltaY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(1);
		toogle.setCaptionLabel("Use normals");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(scanGroup);

		// Create the effects group and add its controllers
		Group effectsGroup = cp5.addGroup("effectsGroup");
		effectsGroup.setPosition(marginX, 3 * marginY + 2 * buttonSize + 5 * deltaY);
		effectsGroup.setSize(groupWidth - 2 * marginX, marginY + 4 * buttonSize + 2 * deltaY);
		effectsGroup.setBarHeight(0);
		effectsGroup.setBackgroundColor(groupBackgroundColor);
		effectsGroup.getCaptionLabel().setVisible(false);
		effectsGroup.disableCollapse();
		effectsGroup.setGroup(mainGroup);

		list = cp5.addScrollableList("effect");
		list.setPosition(marginX, marginY);
		list.setSize(sliderWidth, 5 * buttonSize);
		list.setBarHeight(0);
		list.setBarVisible(false);
		list.setItemHeight(buttonSize);
		list.setType(ScrollableList.LIST);
		list.addItem("No effect", 0);
		list.addItem("Pulsation effect", 1);
		list.addItem("Perlin effect", 2);
		list.addItem("Hole effect", 3);
		list.addItem("Circle effect", 4);
		list.addItem("Vertical cut effect", 5);
		list.addItem("Grid effect", 6);
		list.addItem("Edge effect", 7);
		list.setValue(0);
		list.setGroup(effectsGroup);

		toogle = cp5.addToggle("invertEffect");
		toogle.setPosition(marginX, marginY + 4 * buttonSize + deltaY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(0);
		toogle.setCaptionLabel("Invert effect");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(effectsGroup);

		toogle = cp5.addToggle("fillWithColor");
		toogle.setPosition(marginX + deltaX, marginY + 4 * buttonSize + deltaY);
		toogle.setSize(buttonSize, buttonSize);
		toogle.setValue(0);
		toogle.setCaptionLabel("Fill with color");
		toogle.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(padding);
		toogle.setGroup(effectsGroup);

		// Start broadcasting events
		cp5.setBroadcast(true);
	}

	/**
	 * Processes the control panel events
	 * 
	 * @param event the controller event
	 */
	public void processEvent(ControlEvent event) {
		String controllerGroupName = event.getController().getParent().getName();

		if (controllerGroupName.equals("scanGroup")) {
			processScanEvent(event);
		} else if (controllerGroupName.equals("effectsGroup")) {
			processEffectEvent(event);
		}
	}

	/**
	 * Processes the scan group events
	 * 
	 * @param event the controller event
	 */
	protected void processScanEvent(ControlEvent event) {
		Controller<?> controller = event.getController();
		String controllerName = controller.getName();

		if (controllerName.equals("scanFace") || controllerName.equals("resolution")
				|| controllerName.equals("smoothness") || controllerName.equals("fillHoleSize")) {
			// Start with the original scan
			p.scan = p.scans[(int) cp5.getController("scanFace").getValue()].copy();

			// Modify the scan according to the controllers values
			p.scan.reduceResolution((int) cp5.getController("resolution").getValue());
			p.scan.fillHoles((int) cp5.getController("fillHoleSize").getValue());
			p.scan.gaussianSmooth((int) cp5.getController("smoothness").getValue());

			// Calculate the scan point normals
			p.scan.calculateNormals();

			// Calculate the scan meshes
			boolean addNormals = (int) cp5.getController("useNormals").getValue() == 1;
			p.scan.calculateMesh(addNormals);
			p.scan.calculatePointsMesh(addNormals, 2);
			p.scan.calculateLinesMesh(addNormals, 1);
		} else if (controllerName.equals("drawingMode")) {
			drawingModeIterator = drawingModeIterator == 2 ? 0 : drawingModeIterator + 1;
			controller.setCaptionLabel(drawingModes[drawingModeIterator]);

			switch (drawingModeIterator) {
			case 0:
				p.drawMesh = true;
				p.drawPoints = false;
				p.drawLines = false;
				break;
			case 1:
				p.drawMesh = false;
				p.drawPoints = true;
				p.drawLines = false;
				break;
			case 2:
				p.drawMesh = false;
				p.drawPoints = false;
				p.drawLines = true;
				break;
			}
		} else if (controllerName.equals("useNormals")) {
			// Calculate the scan meshes
			boolean addNormals = (int) controller.getValue() == 1;
			p.scan.calculateMesh(addNormals);
			p.scan.calculatePointsMesh(addNormals, 2);
			p.scan.calculateLinesMesh(addNormals, 1);
		}
	}

	/**
	 * Processes the effects group events
	 * 
	 * @param event the controller event
	 */
	protected void processEffectEvent(ControlEvent event) {
		Controller<?> controller = event.getController();
		String controllerName = controller.getName();

		if (controllerName.equals("effect")) {
			int value = (int) controller.getValue();
			p.scanShader.set("effect", value);
			p.pointShader.set("effect", value);
		} else if (controllerName.equals("invertEffect")) {
			int value = (int) controller.getValue();
			p.scanShader.set("invertEffect", value);
			p.pointShader.set("invertEffect", value);
		} else if (controllerName.equals("fillWithColor")) {
			int value = (int) controller.getValue();
			p.scanShader.set("fillWithColor", value);
			p.pointShader.set("fillWithColor", value);
		}
	}

	/**
	 * Checks if the mouse is over the control panel
	 * 
	 * @return true if the mouse is over the control panel
	 */
	public boolean isMouseOver() {
		return cp5.isMouseOver();
	}
}
