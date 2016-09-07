package kinectScanner;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Helper class containing some useful methods to manipulate scans and slits
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class KinectHelper {

	/**
	 * This class has no public constructor
	 */
	private KinectHelper() {

	}

	/**
	 * Creates an average scan from a list of scans, assuming that all the scans have the same dimensions
	 * 
	 * @param scanList the list of scans to average
	 * @return the scan average
	 */
	public static Scan averageScans(ArrayList<Scan> scanList) {
		// Create an empty average scan with the same dimensions as the scans in the list
		Scan averageScan = new Scan(scanList.get(0).width, scanList.get(0).height);

		// Loop over the scans in the list and fill the average scan arrays
		int[] counter = new int[averageScan.nPoints];
		int[] red = new int[counter.length];
		int[] green = new int[counter.length];
		int[] blue = new int[counter.length];

		for (Scan scan : scanList) {
			averageScan.center.add(scan.center);

			for (int i = 0; i < averageScan.nPoints; i++) {
				if (scan.visibilityMask[i]) {
					averageScan.points[i].add(scan.points[i]);
					int color = scan.colors[i];
					red[i] += (color >> 16) & 0xff;
					green[i] += (color >> 8) & 0xff;
					blue[i] += color & 0xff;
					counter[i]++;
				}
			}
		}

		averageScan.center.div(scanList.size());

		for (int i = 0; i < averageScan.nPoints; i++) {
			if (counter[i] > 0) {
				averageScan.points[i].div(counter[i]);
				averageScan.colors[i] = ((red[i] / counter[i]) << 16) | ((green[i] / counter[i]) << 8)
						| (blue[i] / counter[i]) | 0xff000000;
				averageScan.visibilityMask[i] = true;
			}
		}

		return averageScan;
	}

	/**
	 * Creates a scan from the combination of several slits, assuming that all have the same orientation and dimensions
	 * 
	 * @param slitList the list of slits to combine
	 * @param rotate if true the slits will rotated around their center
	 * @param commonCenter if true all the slits will be moved to have the same center
	 * @return the scan formed from the combination of the slits
	 */
	public static Scan combineSlits(ArrayList<Slit> slitList, boolean rotate, boolean commonCenter) {
		// Create an empty scan with the same center as the last slit added to the list
		Slit slit = slitList.get(slitList.size() - 1);
		boolean verticalSlits = slit.vertical;
		int width = verticalSlits ? slitList.size() : slit.points.length;
		int height = verticalSlits ? slit.points.length : slitList.size();
		Scan scan = new Scan(width, height);
		scan.center.set(slit.center);

		// Loop over the slits in the list and fill the scan arrays
		for (int i = 0; i < slitList.size(); i++) {
			slit = slitList.get(i);
			float offset = (slitList.size() - 1 - i) * 5;
			float rotationAngle = 4 * (slitList.size() - 1 - i) * PApplet.PI / 180;
			float cos = PApplet.cos(rotationAngle);
			float sin = PApplet.sin(rotationAngle);

			for (int j = 0; j < slit.points.length; j++) {
				if (slit.visibilityMask[j]) {
					int index = verticalSlits ? i + j * width : j + i * width;
					PVector point = scan.points[index];
					point.set(slit.points[j]);

					// Check if the slit points should be rotated or shifted
					if (rotate) {
						point.sub(slit.center);

						if (verticalSlits) {
							point.set(cos * point.x - sin * point.z, point.y, sin * point.x + cos * point.z);
						} else {
							point.set(point.x, cos * point.y - sin * point.z, sin * point.y + cos * point.z);
						}

						point.add(slit.center);
					} else {
						if (verticalSlits) {
							point.x += offset;
						} else {
							point.y += offset;
						}
					}

					// Check if the slit points should be moved to have the same center
					if (commonCenter) {
						point.sub(slit.center);
						point.add(scan.center);
					}

					scan.colors[index] = slit.colors[j];
					scan.visibilityMask[index] = true;
				}
			}
		}

		return scan;
	}
}