package sketches.scanViewer;

import processing.core.PImage;

/**
 * Defines the effect interface methods
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public interface Effect {

	/**
	 * Updates the effect
	 * 
	 * @param cleanBackground if true the canvas will be clean before it's updated
	 */
	public void update(boolean cleanBackground);

	/**
	 * Returns the current canvas image
	 * 
	 * @return the canvas image
	 */
	public PImage getImage();
}
