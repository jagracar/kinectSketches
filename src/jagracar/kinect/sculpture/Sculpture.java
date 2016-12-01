package jagracar.kinect.sculpture;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PShape;
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
	 * The parent Processing applet
	 */
	protected PApplet p;

	/**
	 * The sculpture section radius
	 */
	protected float sectionRadius;

	/**
	 * The number of sides in each section
	 */
	protected int sectionSides;

	/**
	 * The number of vertices between two spline control points
	 */
	protected int subdivisions;

	/**
	 * The 3D spline curve
	 */
	protected Spline3D spline;

	/**
	 * The last control point added to the spline
	 */
	protected Vec3D previousPoint;

	/**
	 * The sections array list
	 */
	protected ArrayList<SculptureSection> sections;

	/**
	 * The sculpture mesh
	 */
	protected PShape mesh;

	/**
	 * The mesh color
	 */
	protected int meshColor;

	/**
	 * The minimum distance allowed between two consecutive spline control points
	 */
	protected float minimumDistanceSq = 50 * 50;

	/**
	 * Constructs an empty sculpture
	 * 
	 * @param p the parent Processing applet
	 * @param sectionRadius the sculpture section radius
	 * @param sectionSides the number of sculpture section sides
	 * @param subdivisions the number of spline vertices between to control points
	 */
	public Sculpture(PApplet p, float sectionRadius, int sectionSides, int subdivisions) {
		this.p = p;
		this.sectionRadius = sectionRadius;
		this.sectionSides = sectionSides;
		this.subdivisions = subdivisions;
		this.spline = new Spline3D();
		this.previousPoint = new Vec3D();
		this.sections = new ArrayList<SculptureSection>();
		this.mesh = null;
		this.meshColor = 0xffffffff;
	}

	/**
	 * Adds a new control point to the sculpture
	 * 
	 * @param newPoint the new control point
	 */
	public void addControlPoint(PVector newPoint) {
		Vec3D controlPoint = new Vec3D(newPoint.x, newPoint.y, newPoint.z);

		if (getNumControlPoints() == 0 || previousPoint.distanceToSquared(controlPoint) > minimumDistanceSq) {
			spline.add(controlPoint);
			previousPoint.set(controlPoint);

			// Calculate the sculpture sections and the mesh
			calculateSections();
			calculateMesh();
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

			// Calculate the sculpture sections and the mesh
			calculateSections();
			calculateMesh();
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

			// Calculate the sculpture sections and the mesh
			calculateSections();
			calculateMesh();
		}
	}

	/**
	 * Sets the number of spline vertices between two control points
	 * 
	 * @param newSubdivisions the new number of spline vertices between two control points
	 */
	public void setSubdivisions(int newSubdivisions) {
		if (subdivisions != newSubdivisions && newSubdivisions > 1) {
			subdivisions = newSubdivisions;

			// Calculate the sculpture sections and the mesh
			calculateSections();
			calculateMesh();
		}
	}

	/**
	 * Sets the sculpture mesh color
	 * 
	 * @param newColor the new mesh color
	 */
	public void setColor(int newColor) {
		if (meshColor != newColor) {
			meshColor = newColor;

			// Update the mesh
			if (mesh != null) {
				mesh.setFill(meshColor);
			}
		}
	}

	/**
	 * Returns the sculpture section radius
	 * 
	 * @return the sculpture section radius
	 */
	public float getSectionRadius() {
		return sectionRadius;
	}

	/**
	 * Returns the number of sculpture section sides
	 * 
	 * @return the number of sculpture section sides
	 */
	public int getSectionSides() {
		return sectionSides;
	}

	/**
	 * Returns the number of spline vertices between two control points
	 * 
	 * @return the number of spline vertices between two control points
	 */
	public int getSubdivisions() {
		return subdivisions;
	}

	/**
	 * Calculates the sculpture sections between consecutive spline vertices
	 */
	protected void calculateSections() {
		// Clear the sections array
		sections.clear();

		if (getNumControlPoints() > 1) {
			// Obtain the new sections
			ArrayList<Vec3D> vertices = (ArrayList<Vec3D>) spline.computeVertices(subdivisions);
			Vec3D refPoint = new Vec3D();
			Vec3D refNormal = vertices.get(1).sub(vertices.get(0)).normalize();

			for (int i = 0; i < vertices.size() - 1; i++) {
				Vec3D pointBefore = vertices.get(i);
				Vec3D pointAfter = vertices.get(i + 1);
				Vec3D center = pointAfter.add(pointBefore).scaleSelf(0.5f);
				Vec3D normal = pointAfter.sub(pointBefore).normalize();
				SculptureSection section = new SculptureSection(center, normal, sectionRadius, sectionSides, refPoint,
						refNormal);
				refPoint = section.points[0];
				refNormal = section.normal;
				sections.add(section);
			}
		}
	}

	/**
	 * Calculates the sculpture mesh
	 */
	protected void calculateMesh() {
		if (sections.size() > 1) {
			// Create the sculpture mesh
			mesh = p.createShape(PApplet.GROUP);
			mesh.fill(meshColor);

			// Add the front side
			mesh.addChild(sections.get(0).calculateMesh(p, meshColor));

			// Calculate and add the mesh surface
			PShape surface = p.createShape();
			surface.beginShape(PApplet.TRIANGLES);
			surface.noStroke();
			surface.fill(meshColor);

			for (int i = 0; i < sections.size() - 1; i++) {
				SculptureSection section1 = sections.get(i);
				SculptureSection section2 = sections.get(i + 1);

				for (int j = 0; j < section1.points.length - 1; j++) {
					Vec3D point1 = section1.points[j];
					Vec3D point2 = section1.points[j + 1];
					Vec3D point3 = section2.points[j];
					Vec3D point4 = section2.points[j + 1];
					surface.vertex(point1.x, point1.y, point1.z);
					surface.vertex(point2.x, point2.y, point2.z);
					surface.vertex(point3.x, point3.y, point3.z);
					surface.vertex(point2.x, point2.y, point2.z);
					surface.vertex(point4.x, point4.y, point4.z);
					surface.vertex(point3.x, point3.y, point3.z);
				}

				Vec3D closePoint1 = section1.points[section1.points.length - 1];
				Vec3D closePoint2 = section1.points[0];
				Vec3D closePoint3 = section2.points[section1.points.length - 1];
				Vec3D closePoint4 = section2.points[0];
				surface.vertex(closePoint1.x, closePoint1.y, closePoint1.z);
				surface.vertex(closePoint2.x, closePoint2.y, closePoint2.z);
				surface.vertex(closePoint3.x, closePoint3.y, closePoint3.z);
				surface.vertex(closePoint2.x, closePoint2.y, closePoint2.z);
				surface.vertex(closePoint4.x, closePoint4.y, closePoint4.z);
				surface.vertex(closePoint3.x, closePoint3.y, closePoint3.z);
			}

			surface.endShape();

			mesh.addChild(surface);

			// Add the back side
			mesh.addChild(sections.get(sections.size() - 1).calculateMesh(p, meshColor));
		}
	}

	/**
	 * Clears the sculpture, removing the control points, the sculpture sections and the mesh
	 */
	public void clear() {
		spline = new Spline3D();
		previousPoint.set(0, 0, 0);
		sections.clear();
		mesh = null;
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

			// Calculate the sculpture sections and the mesh
			calculateSections();
			calculateMesh();
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
	 */
	public void draw() {
		if (mesh != null) {
			p.shape(mesh);
		}
	}

	/**
	 * Initializes the spline from a file
	 * 
	 * @param fileName the name of the file containing the spline control points
	 */
	public void initFromFile(String fileName) {
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

		// Calculate the sculpture sections and the mesh
		calculateSections();
		calculateMesh();
	}

	/**
	 * Saves the sculpture control points
	 * 
	 * @param fileName the name of the file where the points will be saved
	 */
	public void savePoints(String fileName) {
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
