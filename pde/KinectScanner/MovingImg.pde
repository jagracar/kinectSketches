
/**
 * Simple class to work with moving images
 * 
 * @author Javier Graciá Carpio (jagracar)
 */
class MovingImg {

  /**
   * The image array
   */
  private PImage img;

  /**
   * The image position
   */
  public PVector position;

  /**
   * The image velocity
   */
  public PVector velocity;

  /**
   * The image rotation angle
   */
  public float angle;

  /**
   * The image visibility
   */
  public boolean visible;

  /**
   * Constructs a MovingImg object
   * 
   * @param img the image array
   */
  public MovingImg(PImage img) {
    this.img = img;
    this.position = new PVector();
    this.velocity = new PVector();
    this.angle = 0;
    this.visible = true;
  }

  /**
   * Updates the image position adding the image velocity
   */
  public void update() {
    position.add(velocity);
  }

  /**
   * Checks if the image is close to a given position
   * 
   * @param positionToCheck the position to check
   */
  public boolean closeToPosition(PVector positionToCheck) {
    return position.dist(positionToCheck) < 100;
  }

  /**
   * Draws the image on the screen if it's declared visible
   * 
   * @param p the parent Processing applet
   */
  public void draw(PApplet p) {
    if (visible) {
      p.pushStyle();
      p.imageMode(PApplet.CENTER);
      p.pushMatrix();
      p.translate(position.x, position.y, position.z);
      p.rotateZ(angle);
      p.image(img, 0, 0);
      p.popMatrix();
      p.popStyle();
    }
  }
}