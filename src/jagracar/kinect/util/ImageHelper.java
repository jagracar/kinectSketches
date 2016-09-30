package jagracar.kinect.util;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Helper class containing some useful methods to create and manipulate images
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class ImageHelper {

	/**
	 * This class has no public constructor, only static methods
	 */
	private ImageHelper() {

	}

	/**
	 * Creates an image with a circular color gradient
	 * 
	 * @param p the parent Processing applet
	 * @param centralColor the image central color
	 * @param borderColor the image border color
	 * @return the image with the circular color gradient
	 */
	public static PImage createGradientImg(PApplet p, int centralColor, int borderColor) {
		// Create the image with the same dimensions as the sketch applet
		PImage img = p.createImage(p.width, p.height, PApplet.RGB);

		// Set the image pixel colors
		float rowCenter = 0.5f * img.height;
		float colCenter = 0.5f * img.width;
		float maxRadiusSq = PApplet.sq(colCenter) + PApplet.sq(rowCenter);
		int centralRed = (centralColor >> 16) & 0xff;
		int centralGreen = (centralColor >> 8) & 0xff;
		int centralBlue = centralColor & 0xff;
		int borderRed = (borderColor >> 16) & 0xff;
		int borderGreen = (borderColor >> 8) & 0xff;
		int borderBlue = borderColor & 0xff;

		img.loadPixels();

		for (int row = 0; row < img.height; row++) {
			for (int col = 0; col < img.width; col++) {
				float relativeDist = PApplet
						.sqrt((PApplet.sq(col - colCenter) + PApplet.sq(row - rowCenter)) / maxRadiusSq);
				int pixelRed = Math.round((1 - relativeDist) * centralRed + relativeDist * borderRed);
				int pixelGreen = Math.round((1 - relativeDist) * centralGreen + relativeDist * borderGreen);
				int pixelBlue = Math.round((1 - relativeDist) * centralBlue + relativeDist * borderBlue);
				img.pixels[col + row * img.width] = (pixelRed << 16) | (pixelGreen << 8) | pixelBlue | 0xff000000;
			}
		}

		img.updatePixels();

		return img;
	}
}