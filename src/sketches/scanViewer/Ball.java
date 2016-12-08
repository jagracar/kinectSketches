package sketches.scanViewer;

import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Class used to simulate the movement of a ball
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class Ball {

	/**
	 * The ball position
	 */
	public PVector position;

	/**
	 * The ball velocity
	 */
	public PVector velocity;

	/**
	 * The ball radius
	 */
	public float radius;

	/**
	 * Constructs the Ball object
	 * 
	 * @param position the initial ball position
	 * @param velocity the initial ball velocity
	 * @param radius the ball radius
	 */
	public Ball(PVector position, PVector velocity, float radius) {
		this.position = position.copy();
		this.velocity = velocity.copy();
		this.radius = radius;
	}

	/**
	 * Updates the ball position and velocity on the canvas
	 * 
	 * @param canvas the canvas where the ball is moving
	 */
	public void update(PGraphics canvas) {
		// Move the ball
		position.add(velocity);

		// Make sure that the ball is not leaving the canvas
		if (position.x < radius) {
			position.x = radius;
			velocity.x *= -1;
		} else if (position.x > canvas.width - radius) {
			position.x = canvas.width - radius;
			velocity.x *= -1;
		}

		if (position.y < radius) {
			position.y = radius;
			velocity.y *= -1;
		} else if (position.y > canvas.height - radius) {
			position.y = canvas.height - radius;
			velocity.y *= -1;
		}
	}
}
