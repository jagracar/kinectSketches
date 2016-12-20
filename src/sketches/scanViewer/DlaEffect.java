package sketches.scanViewer;

import java.util.ArrayList;
import java.util.Iterator;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Class used to simulate a diffuse-limited aggregation effect
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class DlaEffect implements Effect {

	/**
	 * The canvas where the particles are painted
	 */
	private PGraphics canvas;

	/**
	 * The particles array list
	 */
	private ArrayList<Particle> particles;

	/**
	 * The mask to keep track of the aggregated particles
	 */
	private boolean[] aggregatedMask;

	/**
	 * Constructs the DlaEffect object
	 * 
	 * @param p the parent Processing applet
	 * @param canvasWidth the canvas width
	 * @param canvasHeight the canvas height
	 * @param nParticles the number of particles
	 */
	public DlaEffect(PApplet p, int canvasWidth, int canvasHeight, int nParticles) {
		this.canvas = p.createGraphics(canvasWidth, canvasHeight);
		this.particles = new ArrayList<Particle>(nParticles);
		this.aggregatedMask = new boolean[canvasWidth * canvasHeight];

		// Set the canvas defaults
		this.canvas.beginDraw();
		this.canvas.noStroke();
		this.canvas.fill(255);
		this.canvas.background(0);
		this.canvas.endDraw();

		// Initialize the particles
		for (int i = 0; i < nParticles; i++) {
			int x = (int) Math.floor(canvasWidth * Math.random());
			int y = (int) Math.floor(canvasHeight * Math.random());
			particles.add(new Particle(x, y));
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

		// Iterate over the particles
		Iterator<Particle> iter = particles.iterator();

		while (iter.hasNext()) {
			// Update the particle position
			Particle particle = iter.next();
			particle.update();

			// Check if the particle should be aggregated
			if (aggregate(particle)) {
				// Update the aggregated mask
				updateAggregatedMask(particle);

				// Paint the particle on the canvas
				canvas.ellipse(particle.xPrev, particle.yPrev, 2, 2);

				// Remove the particle from the list
				iter.remove();
			}
		}

		canvas.endDraw();
	}

	/**
	 * Checks if a particle should be aggregated
	 * 
	 * @param particle the particle to check for aggregation
	 * @return true if the particle should be aggregated
	 */
	private boolean aggregate(Particle particle) {
		int x = particle.x;
		int y = particle.y;

		if (x >= 0 && x < canvas.width && y >= 0 && y < canvas.height) {
			return aggregatedMask[x + y * canvas.width];
		} else {
			return false;
		}
	}

	/**
	 * Updates the aggregated mask with a new particle
	 * 
	 * @param particle the particle to aggregate
	 */
	private void updateAggregatedMask(Particle particle) {
		int x = particle.xPrev;
		int y = particle.yPrev;

		if (x > 0 && x < canvas.width - 1 && y > 0 && y < canvas.height - 1) {
			int loc = x + y * canvas.width;
			aggregatedMask[loc] = true;
			aggregatedMask[loc + 1] = true;
			aggregatedMask[loc - 1] = true;
			aggregatedMask[loc + canvas.width] = true;
			aggregatedMask[loc - canvas.width] = true;
		}
	}

	/**
	 * Adds an aggregation seed at the given position
	 * 
	 * @param x the horizontal seed position
	 * @param y the vertical seed position
	 */
	public void addSeed(int x, int y) {
		if (x >= 0 && x < canvas.width && y >= 0 && y < canvas.height) {
			aggregatedMask[x + y * canvas.width] = true;
		}
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
