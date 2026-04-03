package bot;

import field.Vector2D;
import java.util.ArrayList;

/**
 * Bot
 * Phase 5: Bot Simulation Engine
 * Represents an autonomous agent that follows the vector field with drift.
 */
public class Bot {
  private Vector2D position;
  private Vector2D velocity;
  
  private float life;
  private float maxLife;
  
  private float radar;      // Sampling radius for field influence
  private float drift;      // Random walk influence (0-1)
  private float speed;      // Velocity magnitude
  
  private Vector2D driftDirection;  // Persistent drift direction that changes slowly
  private int driftChangeCounter;   // Counter for when to change drift direction
  private static final int DRIFT_CHANGE_INTERVAL = 60;  // Change direction every 60 frames
  
  private ArrayList<Vector2D> path;
  private static final float PATH_POINT_THRESHOLD = 2.0f;  // Only add point if distance > threshold
  
  public Bot(float x, float y, float maxLife, float radar, float drift, float speed) {
    this.position = new Vector2D(x, y);
    this.velocity = Vector2D.randomDirection();  // Random initial direction
    this.life = maxLife;
    this.maxLife = maxLife;
    this.radar = radar;
    this.drift = Math.max(0, Math.min(1, drift));  // Clamp to [0, 1]
    this.speed = speed;
    this.driftDirection = Vector2D.randomDirection();  // Initialize random drift direction
    this.driftChangeCounter = 0;
    this.path = new ArrayList<>();
    this.path.add(new Vector2D(position.vx, position.vy));  // Start path at spawn location
  }
  
  /**
   * Update bot position and velocity based on field force and drift
   * @param fieldForce The sampled vector field at bot position
   */
  public void update(Vector2D fieldForce) {
    if (life <= 0) {
      return;
    }
    
    // Update drift direction slowly (change direction every DRIFT_CHANGE_INTERVAL frames)
    driftChangeCounter++;
    if (driftChangeCounter >= DRIFT_CHANGE_INTERVAL) {
      driftChangeCounter = 0;
      driftDirection = Vector2D.randomDirection();
    }
    
    // Apply persistent drift direction with controlled influence
    Vector2D driftVector = driftDirection.copy();
    driftVector.scale(drift);  // Scale proportional to drift slider
    
    // Combine field force and drift
    Vector2D direction = fieldForce.copy();
    direction.add(driftVector);
    
    // Normalize and apply speed
    float dirMagnitude = direction.magnitude();
    if (dirMagnitude > 0) {
      direction.normalize();
      velocity = direction;
      velocity.scale(speed);
    } else {
      velocity.zero();
    }
    
    // Update position
    position.add(velocity);
    
    // Add point to path if distance is sufficient
    if (path.isEmpty() || 
        position.distance(path.get(path.size() - 1)) > PATH_POINT_THRESHOLD) {
      path.add(new Vector2D(position.vx, position.vy));
    }
    
    // Decrease life
    life -= 1.0f;
  }
  
  /**
   * Check if bot is still alive
   */
  public boolean isAlive() {
    return life > 0;
  }
  
  /**
   * Get life as a normalized value (0-1, where 1 is just spawned)
   */
  public float getLifeNormalized() {
    return Math.max(0, life / maxLife);
  }
  
  // Getters
  public Vector2D getPosition() {
    return position;
  }
  
  public Vector2D getVelocity() {
    return velocity;
  }
  
  public float getLife() {
    return life;
  }
  
  public float getMaxLife() {
    return maxLife;
  }
  
  public float getRadar() {
    return radar;
  }
  
  public float getDrift() {
    return drift;
  }
  
  public float getSpeed() {
    return speed;
  }
  
  public ArrayList<Vector2D> getPath() {
    return path;
  }
  
  // Setters
  public void setRadar(float radar) {
    this.radar = radar;
  }
  
  public void setDrift(float drift) {
    this.drift = Math.max(0, Math.min(1, drift));
  }
  
  public void setSpeed(float speed) {
    this.speed = speed;
  }
}
