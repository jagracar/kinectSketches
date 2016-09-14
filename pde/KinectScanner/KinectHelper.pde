
/**
 * Helper class containing some useful methods to manipulate scans and slits
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
class KinectHelper {

  /**
   * This class has no public constructor
   */
  public KinectHelper() {
  }

  /**
   * Creates an average scan from a list of scans, assuming that all the scans have the same dimensions
   * 
   * @param scanList the list of scans to average
   * @return the scan average
   */
  public Scan averageScans(ArrayList<Scan> scanList) {
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
          int col = scan.colors[i];
          red[i] += (col >> 16) & 0xff;
          green[i] += (col >> 8) & 0xff;
          blue[i] += col & 0xff;
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
  public Scan combineSlits(ArrayList<Slit> slitList, boolean rotate, boolean commonCenter) {
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


  /**
   	 * Creates an image with a circular color gradient
   	 * 
   	 * @param p the parent Processing applet
   	 * @param centralColor the image central color
   	 * @param borderColor the image border color
   	 * @return the image with the circular color gradient
   	 */
  public PImage createGradientImg(PApplet p, int centralColor, int borderColor) {
    // Create the image with the same dimensions as the sketch applet
    PImage img = p.createImage(p.width, p.height, PApplet.RGB);

    // Set the image pixel colors
    float rowCenter = 0.5f * img.height;
    float colCenter = 0.5f * img.width;
    float maxRadius = PApplet.sqrt(PApplet.sq(colCenter) + PApplet.sq(rowCenter));
    float centralRed = p.red(centralColor);
    float centralGreen = p.green(centralColor);
    float centralBlue = p.blue(centralColor);
    float borderRed = p.red(borderColor);
    float borderGreen = p.green(borderColor);
    float borderBlue = p.blue(borderColor);

    img.loadPixels();

    for (int row = 0; row < img.height; row++) {
      for (int col = 0; col < img.width; col++) {
        int index = col + row * img.width;
        float relativeDist = PApplet.sqrt(PApplet.sq(col - colCenter) + PApplet.sq(row - rowCenter))
          / maxRadius;
        float pixelRed = (1 - relativeDist) * centralRed + relativeDist * borderRed;
        float pixelGreen = (1 - relativeDist) * centralGreen + relativeDist * borderGreen;
        float pixelBlue = (1 - relativeDist) * centralBlue + relativeDist * borderBlue;
        img.pixels[index] = color(pixelRed, pixelGreen, pixelBlue);
      }
    }

    img.updatePixels();

    return img;
  }
}