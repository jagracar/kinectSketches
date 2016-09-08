package jagracar.kinect.sculpture;

import processing.core.PApplet;
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
	public Vec3D center;

	/**
	 * The section plane normal
	 */
	public Vec3D normal;

	/**
	 * The section contour points
	 */
	public Vec3D[] points;

	/**
	 * Constructs a sculpture section centered on the two given points and perpendicular to their direction
	 * 
	 * @param pointBefore the point before the section
	 * @param pointAfter the point after the section
	 * @param referencePoint the reference point from the previous section
	 * @param referenceNormal the normal vector from the previous section
	 * @param radius the section radius
	 * @param sides the number of section sides
	 */
	public SculptureSection(Vec3D pointBefore, Vec3D pointAfter, Vec3D referencePoint, Vec3D referenceNormal,
			float radius, int sides) {
		this.center = pointAfter.add(pointBefore).scaleSelf(0.5f);
		this.normal = pointAfter.sub(pointBefore).normalize();
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
	 * Draws the filled section on the screen
	 * 
	 * @param p the parent Processing applet
	 * @param color the color to use
	 */
	public void draw(PApplet p, int color) {
		p.pushStyle();
		p.noStroke();
		p.fill(color);

		p.beginShape(PApplet.TRIANGLE_FAN);
		p.vertex(center.x, center.y, center.z);

		for (Vec3D point : points) {
			p.vertex(point.x, point.y, point.z);
		}

		Vec3D closePoint = points[0];
		p.vertex(closePoint.x, closePoint.y, closePoint.z);
		p.endShape();

		p.popStyle();
	}
}