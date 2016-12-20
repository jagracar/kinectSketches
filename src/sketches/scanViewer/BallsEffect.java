package sketches.scanViewer;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Class used to simulate the effect of a group of balls moving on a canvas
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class BallsEffect implements Effect {

	/**
	 * The canvas where the balls are moving
	 */
	private PGraphics canvas;

	/**
	 * The balls array
	 */
	private Ball[] balls;

	/**
	 * Constructs the BallsEffect object
	 * 
	 * @param p the parent Processing applet
	 * @param canvasWidth the canvas width
	 * @param canvasHeight the canvas height
	 * @param nBalls the number of balls
	 * @param ballsVelocity the balls velocity
	 * @param ballsRadius the balls radius
	 */
	public BallsEffect(PApplet p, int canvasWidth, int canvasHeight, int nBalls, float ballsVelocity, float ballsRadius) {
		this.canvas = p.createGraphics(canvasWidth, canvasHeight);
		this.balls = new Ball[nBalls];

		// Set the canvas defaults
		this.canvas.beginDraw();
		this.canvas.noStroke();
		this.canvas.fill(255);
		this.canvas.background(0);
		this.canvas.endDraw();

		// Initialize the balls
		for (int i = 0; i < nBalls; i++) {
			PVector position = new PVector(p.random(canvasWidth), p.random(canvasHeight));
			float angle = p.random(PApplet.TWO_PI);
			PVector velocity = new PVector(ballsVelocity * PApplet.sin(angle), ballsVelocity * PApplet.cos(angle));
			this.balls[i] = new Ball(position, velocity, ballsRadius);
		}
	}

	/**
	 * Updates the effect
	 * 
	 * @param cleanBackground if true the canvas will be clean before it's updated
	 */
	public void update(boolean cleanBackground) {
		canvas.beginDraw();

		// Clean the canvas background if necessary
		if (cleanBackground) {
			canvas.background(0);
		}

		// Update the balls positions and paint them on the canvas
		for (Ball ball : balls) {
			ball.update(canvas);
			canvas.ellipse(ball.position.x, ball.position.y, 2 * ball.radius, 2 * ball.radius);
		}

		canvas.endDraw();
	}

	/**
	 * Returns the current canvas image
	 * 
	 * @return the canvas image
	 */
	public PImage getImage() {
		return canvas.get();
	}
}
