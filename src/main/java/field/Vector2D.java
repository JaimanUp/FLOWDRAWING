package field;

/**
 * Vector2D
 * Represents a 2D vector with x and y components.
 * Used as individual elements in the vector field grid.
 */
public class Vector2D {
  public float vx;
  public float vy;
  
  public Vector2D() {
    this.vx = 0;
    this.vy = 0;
  }
  
  public Vector2D(float vx, float vy) {
    this.vx = vx;
    this.vy = vy;
  }
  
  public float magnitude() {
    return (float) Math.sqrt(vx * vx + vy * vy);
  }
  
  public void normalize() {
    float mag = magnitude();
    if (mag > 0) {
      vx /= mag;
      vy /= mag;
    }
  }
  
  public void scale(float factor) {
    vx *= factor;
    vy *= factor;
  }
  
  public void add(Vector2D other) {
    this.vx += other.vx;
    this.vy += other.vy;
  }
  
  public void add(float x, float y) {
    this.vx += x;
    this.vy += y;
  }
  
  public void clamp(float maxValue) {
    float mag = magnitude();
    if (mag > maxValue && mag > 0) {
      scale(maxValue / mag);
    }
  }
  
  public Vector2D copy() {
    return new Vector2D(vx, vy);
  }
  
  public void zero() {
    vx = 0;
    vy = 0;
  }
  
  public float distance(Vector2D other) {
    float dx = this.vx - other.vx;
    float dy = this.vy - other.vy;
    return (float) Math.sqrt(dx * dx + dy * dy);
  }
  
  /**
   * Create a random direction vector (normalized, unit length)
   */
  public static Vector2D randomDirection() {
    double angle = Math.random() * 2 * Math.PI;
    return new Vector2D((float)Math.cos(angle), (float)Math.sin(angle));
  }
}
