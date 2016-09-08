package jagracar.kinect.util;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Class used to represent the sketch floor
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class Floor {

	/**
	 * The texture used to paint the floor
	 */
	private PImage texture;

	/**
	 * Constructs a default floor object
	 * 
	 * @param p the parent Processing applet
	 * @param color the floor color
	 */
	public Floor(PApplet p, int color) {
		this.texture = p.createImage(10, 150, PApplet.ARGB);

		// Set the texture pixel colors
		float redColor = p.red(color);
		float greenColor = p.green(color);
		float blueColor = p.blue(color);

		this.texture.loadPixels();

		for (int y = 0; y < texture.height; y++) {
			int rowColor = p.color(redColor, greenColor, blueColor, Math.min(y, 255));

			for (int x = 0; x < texture.width; x++) {
				texture.pixels[x + y * texture.width] = rowColor;
			}
		}

		this.texture.updatePixels();
	}

	/**
	 * Constructs a floor object using the provided image
	 * 
	 * @param floorImage the image to use to paint the floor
	 */
	public Floor(PImage floorImage) {
		this.texture = floorImage;
	}

	/**
	 * Draws the floor on the screen
	 * 
	 * @param p the parent Processing sketch
	 * @param limits the sketch visibility limits
	 */
	public void draw(PApplet p, PVector[] limits) {
		float zStart = limits[0].z - 0.25f * (limits[1].z - limits[0].z);
		float zEnd = limits[1].z + 0.5f * (limits[1].z - limits[0].z);

		p.noStroke();
		p.beginShape();
		p.texture(texture);
		p.vertex(limits[0].x, limits[0].y, zEnd, 0, 0);
		p.vertex(limits[1].x, limits[0].y, zEnd, texture.width, 0);
		p.vertex(limits[1].x, limits[0].y, zStart, texture.width, texture.height);
		p.vertex(limits[0].x, limits[0].y, zStart, 0, texture.height);
		p.endShape();
	}
}