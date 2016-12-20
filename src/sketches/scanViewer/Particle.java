package sketches.scanViewer;

/**
 * Class used to simulate a moving particle
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
public class Particle {

	/**
	 * The particle horizontal position
	 */
	public int x;

	/**
	 * The particle vertical position
	 */
	public int y;

	/**
	 * The particle previous horizontal position
	 */
	public int xPrev;

	/**
	 * The particle previous vertical position
	 */
	public int yPrev;

	/**
	 * Constructs the particle object
	 * 
	 * @param x the particle initial horizontal position
	 * @param y the particle initial vertical position
	 */
	public Particle(int x, int y) {
		this.x = x;
		this.y = y;
		this.xPrev = this.x;
		this.yPrev = this.y;
	}

	/**
	 * Updates the particle position
	 */
	public void update() {
		// Save the previous position
		xPrev = x;
		yPrev = y;

		// Calculate the new position
		switch ((int) Math.floor(8 * Math.random())) {
		case 0:
			x++;
			break;
		case 1:
			x++;
			y++;
			break;
		case 2:
			y++;
			break;
		case 3:
			x--;
			y++;
			break;
		case 4:
			x--;
			break;
		case 5:
			x--;
			y--;
			break;
		case 6:
			y--;
			break;
		case 7:
			x++;
			y--;
			break;
		}
	}
}