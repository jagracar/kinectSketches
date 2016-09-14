package jagracar.kinect.sculpture;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Spline3D;
import toxi.geom.Vec3D;

/**
 * Class used to store and paint sculptures created with the Kinect sensor
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class Sculpture {

	/**
	 * The minimum distance allowed between two consecutive spline control points
	 */
	private static final float MINIMUM_DISTANCE_SQ = 50 * 50;

	/**
	 * The sculpture section radius
	 */
	private float sectionRadius;

	/**
	 * The number of sides in each section
	 */
	private int sectionSides;

	/**
	 * The number of vertices between two spline control points
	 */
	private int subdivisions;

	/**
	 * The 3D spline curve
	 */
	private Spline3D spline;

	/**
	 * The last control point added to the spline
	 */
	private Vec3D previousPoint;

	/**
	 * The sections array list
	 */
	private ArrayList<SculptureSection> sections;

	/**
	 * Constructs an empty sculpture
	 * 
	 * @param sectionRadius the sculpture section radius
	 * @param sectionSides the number of sculpture section sides
	 * @param subdivisions the number of spline vertices between to control points
	 */
	public Sculpture(float sectionRadius, int sectionSides, int subdivisions) {
		this.sectionRadius = sectionRadius;
		this.sectionSides = sectionSides;
		this.subdivisions = subdivisions;
		this.spline = new Spline3D();
		this.previousPoint = new Vec3D();
		this.sections = new ArrayList<SculptureSection>();
	}

	/**
	 * Adds a new control point to the sculpture
	 * 
	 * @param point the new control point
	 */
	public void addControlPoint(PVector point) {
		Vec3D toxiPoint = new Vec3D(point.x, point.y, point.z);

		if (getNumControlPoints() == 0 || previousPoint.distanceToSquared(toxiPoint) > MINIMUM_DISTANCE_SQ) {
			spline.add(toxiPoint);
			previousPoint.set(toxiPoint);

			// Calculate the sculpture sections
			calculateSections();
		}
	}

	/**
	 * Returns the number of control points in the sculpture's spline
	 * 
	 * @return the spline control points
	 */
	public int getNumControlPoints() {
		return spline.getPointList().size();
	}

	/**
	 * Sets the sculpture section radius
	 * 
	 * @param newSectionRadius the new sculpture sections radius
	 */
	public void setSectionRadius(float newSectionRadius) {
		if (sectionRadius != newSectionRadius) {
			sectionRadius = newSectionRadius;

			// Calculate the sculpture sections
			calculateSections();
		}
	}

	/**
	 * Sets the number of sides in each sculpture section
	 * 
	 * @param newSectionSides the new number of sculpture section sides
	 */
	public void setSectionSides(int newSectionSides) {
		if (sectionSides != newSectionSides && newSectionSides > 1) {
			sectionSides = newSectionSides;

			// Calculate the sculpture sections
			calculateSections();
		}
	}

	/**
	 * Sets the number of spline vertices between to control points
	 * 
	 * @param newSubdivisions the new number of spline vertices between to control points
	 */
	public void setSubdivisions(int newSubdivisions) {
		if (subdivisions != newSubdivisions && newSubdivisions > 1) {
			subdivisions = newSubdivisions;

			// Calculate the sculpture sections
			calculateSections();
		}
	}

	/**
	 * Returns the number of sculpture section radius
	 * 
	 * @return the number of sculpture section radius
	 */
	public float getSectionRadius() {
		return sectionRadius;
	}

	/**
	 * Returns the number of sculpture section sides
	 * 
	 * @return the number of sculpture section sides
	 */
	public float getSectionSides() {
		return sectionSides;
	}

	/**
	 * Returns the number of spline vertices between to control points
	 * 
	 * @return the number of spline vertices between to control points
	 */
	public int getSubdivisions() {
		return subdivisions;
	}

	/**
	 * Calculates the sculpture sections between consecutive spline vertices
	 */
	private void calculateSections() {
		// Clear the sections array
		sections.clear();

		if (getNumControlPoints() > 1) {
			// Obtain the new sections
			ArrayList<Vec3D> vertices = (ArrayList<Vec3D>) spline.computeVertices(subdivisions);
			Vec3D refPoint = new Vec3D();
			Vec3D refNormal = vertices.get(1).sub(vertices.get(0)).normalize();

			for (int i = 0; i < vertices.size() - 1; i++) {
				SculptureSection section = new SculptureSection(vertices.get(i), vertices.get(i + 1), refPoint,
						refNormal, sectionRadius, sectionSides);
				refPoint = section.points[0];
				refNormal = section.normal;
				sections.add(section);
			}
		}
	}

	/**
	 * Clears the sculpture, removing the control points and the sculpture sections
	 */
	public void clear() {
		spline = new Spline3D();
		previousPoint.set(0, 0, 0);
		sections.clear();
	}

	/**
	 * Centers the sculpture at a given position
	 * 
	 * @param newCenter the sculpture new central position
	 */
	public void center(PVector newCenter) {
		if (getNumControlPoints() > 0) {
			// Find the current center of the sculpture
			Vec3D sculptureCenter = new Vec3D();
			ArrayList<Vec3D> controlPoints = (ArrayList<Vec3D>) spline.getPointList();

			for (Vec3D controlPoint : controlPoints) {
				sculptureCenter.addSelf(controlPoint);
			}

			sculptureCenter.scaleSelf(1f / controlPoints.size());

			// Subtract the new center to the current center
			sculptureCenter.subSelf(newCenter.x, newCenter.y, newCenter.z);

			// Update the spline control points
			for (Vec3D controlPoint : controlPoints) {
				controlPoint.subSelf(sculptureCenter);
			}

			previousPoint.subSelf(sculptureCenter);

			// Calculate the sculpture sections
			calculateSections();
		}
	}

	/**
	 * Calculates the corner limits that contain all the sculpture control points
	 * 
	 * @return a points array with the lower and upper corner limits
	 */
	public PVector[] calculateLimits() {
		// Get the control points
		ArrayList<Vec3D> controlPoints = (ArrayList<Vec3D>) spline.getPointList();

		float xMin = Float.MAX_VALUE;
		float yMin = Float.MAX_VALUE;
		float zMin = Float.MAX_VALUE;
		float xMax = -Float.MAX_VALUE;
		float yMax = -Float.MAX_VALUE;
		float zMax = -Float.MAX_VALUE;

		for (Vec3D point : controlPoints) {
			if (point.x < xMin) {
				xMin = point.x;
			}

			if (point.x > xMax) {
				xMax = point.x;
			}

			if (point.y < yMin) {
				yMin = point.y;
			}

			if (point.y > yMax) {
				yMax = point.y;
			}

			if (point.z < zMin) {
				zMin = point.z;
			}

			if (point.z > zMax) {
				zMax = point.z;
			}
		}

		// Check that there was at least a visible point
		if ((xMax - xMin) >= 0) {
			return new PVector[] { new PVector(xMin, yMin, zMin), new PVector(xMax, yMax, zMax) };
		} else {
			return null;
		}
	}

	/**
	 * Draws the sculpture on the screen
	 * 
	 * @param p the parent Processing applet
	 * @param color the sculpture color
	 */
	public void draw(PApplet p, int color) {
		if (sections.size() > 0) {
			// Draw the front side
			sections.get(0).draw(p, color);

			// Draw the sculpture surface
			p.pushStyle();
			p.noStroke();
			p.fill(color);

			for (int i = 0; i < sections.size() - 1; i++) {
				SculptureSection section1 = sections.get(i);
				SculptureSection section2 = sections.get(i + 1);

				p.beginShape(PApplet.TRIANGLE_STRIP);

				for (int j = 0; j < section1.points.length; j++) {
					Vec3D point1 = section1.points[j];
					Vec3D point2 = section2.points[j];
					p.vertex(point1.x, point1.y, point1.z);
					p.vertex(point2.x, point2.y, point2.z);
				}

				Vec3D closePoint1 = section1.points[0];
				Vec3D closePoint2 = section2.points[0];
				p.vertex(closePoint1.x, closePoint1.y, closePoint1.z);
				p.vertex(closePoint2.x, closePoint2.y, closePoint2.z);
				p.endShape();
			}

			p.popStyle();

			// Draw the back side
			sections.get(sections.size() - 1).draw(p, color);
		}
	}

	/**
	 * Initializes the spline from a file
	 * 
	 * @param p the parent Processing applet
	 * @param fileName the name of the file containing the spline control points
	 */
	public void initFromFile(PApplet p, String fileName) {
		// Load the file containing the control points
		String[] fileLines = p.loadStrings(fileName);

		// Add the control points to a new spline object
		spline = new Spline3D();

		for (String line : fileLines) {
			String[] coordinates = line.split(" ");

			if (coordinates.length >= 3) {
				spline.add(Float.valueOf(coordinates[0]), Float.valueOf(coordinates[1]), Float.valueOf(coordinates[2]));
			} else {
				System.out.println("Sculpture class: there was a problem reading the control points from " + fileName);
			}
		}

		// Get the last added point
		previousPoint.set(spline.getPointList().get(getNumControlPoints() - 1));

		// Calculate the sculpture sections
		calculateSections();
	}

	/**
	 * Saves the sculpture control points
	 * 
	 * @param p the parent Processing applet
	 * @param fileName the name of the file where the points will be saved
	 */
	public void savePoints(PApplet p, String fileName) {
		// Save sculpture control points in the file
		ArrayList<Vec3D> controlPoints = (ArrayList<Vec3D>) spline.getPointList();
		String[] pointsCoordinates = new String[controlPoints.size()];

		for (int i = 0; i < pointsCoordinates.length; i++) {
			Vec3D point = controlPoints.get(i);
			pointsCoordinates[i] = point.x + " " + point.y + " " + point.z;
		}

		p.saveStrings(fileName, pointsCoordinates);
	}
}
