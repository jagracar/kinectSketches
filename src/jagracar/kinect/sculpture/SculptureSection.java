package jagracar.kinect.sculpture;

import processing.core.PApplet;
import processing.core.PShape;
import toxi.geom.Vec3D;

/**
 * A class used to represent a sculpture section
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class SculptureSection {

	/**
	 * The section center
	 */
	protected Vec3D center;

	/**
	 * The section plane normal
	 */
	protected Vec3D normal;

	/**
	 * The section contour points
	 */
	protected Vec3D[] points;

	/**
	 * Constructs a sculpture section
	 * 
	 * @param center the section center
	 * @param normal the section plane normal
	 * @param radius the section radius
	 * @param sides the number of section sides
	 * @param referencePoint the reference point from the previous section
	 * @param referenceNormal the normal vector from the previous section
	 */
	public SculptureSection(Vec3D center, Vec3D normal, float radius, int sides, Vec3D referencePoint,
			Vec3D referenceNormal) {
		this.center = center.copy();
		this.normal = normal.copy();
		this.points = new Vec3D[Math.max(2, sides)];

		// Calculate the intersection point between the line defined by the reference point and the reference normal
		// and the section plane
		float c = (this.center.dot(this.normal) - referencePoint.dot(this.normal)) / referenceNormal.dot(this.normal);
		Vec3D intersectionPoint = referenceNormal.scale(c).addSelf(referencePoint);

		// Calculate the section points
		Vec3D perpendicularPoint = intersectionPoint.subSelf(this.center).normalizeTo(radius);
		float deltaAngle = PApplet.TWO_PI / sides;

		for (int i = 0; i < this.points.length; i++) {
			this.points[i] = this.center.add(perpendicularPoint);
			perpendicularPoint.rotateAroundAxis(this.normal, deltaAngle);
		}
	}

	/**
	 * Calculates the mesh formed by the section points
	 * 
	 * @param p the parent Processing applet
	 * @param color the mesh color
	 * @return the section mesh
	 */
	public PShape calculateMesh(PApplet p, int color) {
		PShape mesh = p.createShape();
		mesh.beginShape();
		mesh.noStroke();
		mesh.fill(color);

		for (Vec3D point : points) {
			mesh.vertex(point.x, point.y, point.z);
		}

		mesh.endShape(PApplet.CLOSE);

		return mesh;
	}

	/**
	 * Draws the filled section on the screen
	 * 
	 * @param p the parent Processing applet
	 * @param color the color to use
	 */
	public void draw(PApplet p, int color) {
		PShape mesh = calculateMesh(p, color);
		mesh.setFill(color);
		p.shape(mesh);
	}
}