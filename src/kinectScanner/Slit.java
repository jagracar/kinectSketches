package kinectScanner;

import processing.core.PVector;

/**
 * Class used to save the data points from a vertical or horizontal slit
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class Slit {

	/**
	 * The minimum point distance with respect to the slit center to consider it as part of the slit
	 */
	private static final float MINIMUM_DISTANTE = 5f;

	/**
	 * The slit orientation
	 */
	public boolean vertical;

	/**
	 * The slit central point position
	 */
	public PVector center;

	/**
	 * Array containing the points coordinates
	 */
	public PVector[] points;

	/**
	 * Array containing the points colors
	 */
	public int[] colors;

	/**
	 * Array containing the points visibility mask
	 */
	public boolean[] visibilityMask;

	/**
	 * Constructs a slit of the given orientation centered on the scan box position
	 * 
	 * @param kp the KinectPoints object
	 * @param box the scan box from which the slit will be selected
	 * @param vertical true if the slit should have a vertical orientation, false for horizontal orientation
	 */
	public Slit(KinectPoints kp, ScanBox box, boolean vertical) {
		this.vertical = vertical;
		this.center = box.center.copy();
		this.points = new PVector[this.vertical ? kp.height : kp.width];
		this.colors = new int[this.points.length];
		this.visibilityMask = new boolean[this.points.length];

		// Find the slit position in the KinectPoints object
		int slitPos = -1;
		float minDistance = MINIMUM_DISTANTE;

		for (int y = 0; y < kp.height; y++) {
			for (int x = 0; x < kp.width; x++) {
				int index = x + y * kp.width;
				PVector point = kp.points[index];

				if (kp.visibilityMask[index] && box.isInside(point)) {
					float distance = this.vertical ? Math.abs(point.x - this.center.x)
							: Math.abs(point.y - this.center.y);

					if (distance < minDistance) {
						slitPos = this.vertical ? x : y;
						minDistance = distance;
					}
				}
			}
		}

		// Populate the slit arrays
		for (int i = 0; i < this.points.length; i++) {
			this.points[i] = new PVector();

			if (slitPos >= 0) {
				int index = this.vertical ? slitPos + i * kp.width : i + slitPos * kp.width;
				PVector point = kp.points[index];

				if (kp.visibilityMask[index] && box.isInside(point)) {
					this.points[i].set(point);
					this.colors[i] = kp.colors[index];
					this.visibilityMask[i] = kp.visibilityMask[index];
				}
			}
		}
	}
}