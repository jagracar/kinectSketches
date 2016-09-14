package sketches.sculptureViewer;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Slider;

/**
 * Class used to display and control the main Kinect sketch variables
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class ControlPanel {

	/**
	 * The main ControlP5 object
	 */
	private ControlP5 cp5;

	/**
	 * The sculpture viewer sketch applet
	 */
	private SculptureViewerSketch p;

	/**
	 * The controllers horizontal margin
	 */
	private int marginX = 10;

	/**
	 * The controller vertical margin
	 */
	private int marginY = 10;

	/**
	 * The vertical separation between controllers
	 */
	private int deltaY = 30;

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
	private int sliderWidth = 150;

	/**
	 * Constructs the ControlPanel object
	 * 
	 * @param p the main sculpture viewer sketch applet
	 */
	public ControlPanel(SculptureViewerSketch p) {
		this.p = p;
	}

	/**
	 * Adds and initializes all the panel controllers
	 */
	public void setup() {
		// Create the ControlP5 object
		cp5 = new ControlP5(p);
		
		cp5.setBroadcast(false);

		Slider slider = cp5.addSlider("sides");
		slider.setPosition(marginX, marginY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setValue(p.sculpture.getSectionSides());
		slider.setRange(2, 100);
		slider.setNumberOfTickMarks(99);
		slider.showTickMarks(false);
		slider.setCaptionLabel("Sides");
		slider.getCaptionLabel().setPaddingX(padding);

		slider = cp5.addSlider("radius");
		slider.setPosition(marginX, marginY + deltaY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setValue(p.sculpture.getSectionRadius());
		slider.setRange(10, 100);
		slider.setCaptionLabel("Radius");
		slider.getCaptionLabel().setPaddingX(padding);

		slider = cp5.addSlider("subdivisions");
		slider.setPosition(marginX, marginY + 2 * deltaY);
		slider.setSize(sliderWidth, buttonSize);
		slider.setValue(p.sculpture.getSubdivisions());
		slider.setRange(2, 30);
		slider.setNumberOfTickMarks(29);
		slider.showTickMarks(false);
		slider.setCaptionLabel("Subdivisions");
		slider.getCaptionLabel().setPaddingX(padding);

		cp5.setBroadcast(true);
	}

	/**
	 * Processes the controller events
	 * 
	 * @param event the controller event
	 */
	public void processEvent(ControlEvent event) {
		Controller<?> controller = event.getController();
		String controllerName = controller.getName();

		if (controllerName.equals("sides")) {
			p.sculpture.setSectionSides(Math.round(controller.getValue()));
		} else if (controllerName.equals("radius")) {
			p.sculpture.setSectionRadius(controller.getValue());
		} else if (controllerName.equals("subdivisions")) {
			p.sculpture.setSubdivisions(Math.round(controller.getValue()));
		}
	}
}
