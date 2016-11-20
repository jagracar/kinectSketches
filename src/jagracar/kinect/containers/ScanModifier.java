package jagracar.kinect.containers;

import processing.core.PApplet;

/**
 * This class can be used to modify scans temporally
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class ScanModifier {

	/**
	 * The original scan before any modification has been done
	 */
	public Scan originalScan;

	/**
	 * The current version of the modified scan
	 */
	public Scan modifiedScan;

	/**
	 * The initial visibility mask of the modified scan
	 */
	boolean[] initialVisibilityMask;

	/**
	 * The current visibility mask of the modified scan
	 */
	boolean[] activeVisibilityMask;

	/**
	 * Constructs a scan modifier that will act on a copy of the provided scan
	 * 
	 * @param scan the reference scan that will be modified
	 */
	public ScanModifier(Scan scan) {
		originalScan = scan.copy();
		modifiedScan = scan.copy();

		// Initialize the scan modifier visibility masks
		initializeVisibilityMasks();

		// Initialize other scan modifier internal variables
		init();
	}

	/**
	 * Initializes the visibility masks used by the scan modifier
	 */
	private void initializeVisibilityMasks() {
		// Create the mask arrays if necessary
		if (activeVisibilityMask == null || activeVisibilityMask.length != modifiedScan.nPoints) {
			initialVisibilityMask = new boolean[modifiedScan.nPoints];
			activeVisibilityMask = new boolean[modifiedScan.nPoints];
		}

		// Fill the arrays
		for (int i = 0; i < modifiedScan.nPoints; i++) {
			boolean maskValue = modifiedScan.visibilityMask[i];
			initialVisibilityMask[i] = maskValue;
			activeVisibilityMask[i] = maskValue;
		}
	}

	/**
	 * Initializes some of the scan modifier internal variables
	 */
	public void init() {

	}

	/**
	 * Resets the scan modifier to its initial state
	 */
	public void reset() {
		// Set the modified scan data to the original scan data
		modifiedScan.update(originalScan);

		// Initialize the scan modifier visibility masks
		initializeVisibilityMasks();

		// Initialize other scan modifier internal variables
		init();
	}

	/**
	 * Updates the scan modifier internal variables
	 */
	public void update() {

	}

	/**
	 * Updates the scan modifier internal variables
	 * 
	 * @param t variable controlling the update process
	 */
	public void update(float t) {

	}

	/**
	 * Applies the current visibility mask to the modified scan
	 */
	public void applyVisibilityMask() {
		for (int i = 0; i < modifiedScan.nPoints; i++) {
			modifiedScan.visibilityMask[i] = activeVisibilityMask[i];
		}
	}

	/**
	 * Reduces the modified scan resolution by a given factor
	 * 
	 * @param reductionFactor the scale reduction factor
	 */
	public void reduceResolution(int reductionFactor) {
		modifiedScan.update(originalScan);
		modifiedScan.reduceResolution(reductionFactor);
		initializeVisibilityMasks();
		init();
	}

	/**
	 * Fills the modified scan holes interpolating between valid points in the same scan row
	 * 
	 * @param maxHoleGap the maximum number of points missing in order to fill the hole
	 */
	public void fillHoles(int maxHoleGap) {
		modifiedScan.update(originalScan);
		modifiedScan.fillHoles(maxHoleGap);
		initializeVisibilityMasks();
		init();
	}

	/**
	 * Smoothes the modified scan points using a Gaussian kernel
	 * 
	 * @param kernelSize the kernel size. Should be an odd number larger than 1
	 */
	public void gaussianSmooth(int kernelSize) {
		modifiedScan.update(originalScan);
		modifiedScan.gaussianSmooth(kernelSize);
		initializeVisibilityMasks();
		init();
	}

	public void draw(PApplet p) {
		modifiedScan.drawAsTriangles(p);
		modifiedScan.drawBackSide(p, 0xffffffff);
	}
}
