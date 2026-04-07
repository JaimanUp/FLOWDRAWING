package bot;

import field.Vector2D;
import java.util.ArrayList;

/**
 * Bot
 * Phase 5: Bot Simulation Engine
 * Represents an autonomous agent that follows the vector field with drift.
 */
public class Bot {
  /**
   * TracePoint: Stores a position, transparency value, and a captured color.
   * The color is captured at creation time based on the oscillation phase at that moment.
   * Each trace point retains its unique color permanently.
   */
  public static class TracePoint {
    public Vector2D position;
    public float transparency;  // Captured transparency at creation time (0.0-1.0)
    public java.awt.Color color;  // Captured color at creation time (based on oscillation phase)
    
    public TracePoint(float x, float y, float transparency, java.awt.Color capturedColor) {
      this.position = new Vector2D(x, y);
      this.transparency = transparency;
      this.color = capturedColor;
    }
  }
  
  private Vector2D position;
  private Vector2D velocity;
  
  private float life;
  private float maxLife;
  
  private float radar;      // Sampling radius for field influence
  private float speed;      // Maximum velocity magnitude
  
  // Force influence multipliers (0.0-1.0) - balance between the 3 directional forces
  private float driftInfluence = 0.3f;        // How much random drift affects movement (0-1)
  private float fieldInfluence = 1.0f;        // How much vector field affects movement (0-1)
  private float repulsionInfluence = 0.0f;    // How much repulsion from traces affects movement (0-1)
  
  // Repulsion parameters
  private float repulsionRadius = 50.0f;  // Distance range for repulsion detection
  
  private Vector2D driftDirection;  // Persistent drift direction that changes slowly
  private int driftChangeCounter;   // Counter for when to change drift direction
  private static final int DRIFT_CHANGE_INTERVAL = 60;  // Change direction every 60 frames
  
  private ArrayList<TracePoint> path;  // Path stores position, transparency, and color captured at creation
  private static final float PATH_POINT_THRESHOLD = 2.0f;  // Only add point if distance > threshold
  
  private java.awt.Color botColor;  // Color for this bot's trace visualization
  private float traceTransparency = 1.0f;  // Current transparency to be captured for new trace points
  private java.awt.Color traceColor = new java.awt.Color(0x379439);  // Current trace color to capture
  
  public Bot(float x, float y, float maxLife, float radar, float speed) {
    this.position = new Vector2D(x, y);
    this.velocity = Vector2D.randomDirection();  // Random initial direction
    this.life = maxLife;
    this.maxLife = maxLife;
    this.radar = radar;
    this.speed = speed;
    this.driftDirection = Vector2D.randomDirection();  // Initialize random drift direction
    this.driftChangeCounter = 0;
    this.path = new ArrayList<>();
    this.path.add(new TracePoint(position.vx, position.vy, 1.0f, traceColor));  // Start path at spawn with captured color
    // Set random color for this bot
    this.botColor = new java.awt.Color(
      (int)(50 + Math.random() * 200),
      (int)(50 + Math.random() * 200),
      (int)(50 + Math.random() * 200)
    );
  }
  
  /**
   * Calculate repulsion force from all nearby traces (from all bots, both live and dead).
   * Returns a normalized repulsion direction vector (unit-length or zero).
   * The caller scales this by repulsionInfluence.
   * @param allBots List of all bots (live and dead) to check traces against
   * @return Normalized repulsion direction vector
   */
  private Vector2D calculateRepulsion(java.util.ArrayList<Bot> allBots) {
    if (repulsionRadius <= 0 || allBots == null || allBots.isEmpty()) {
      return new Vector2D(0, 0);
    }
    
    Vector2D repulsion = new Vector2D(0, 0);
    float searchRadius = repulsionRadius * 2.0f;  // Search within 2x repulsion radius for bot positions
    
    // Check distance to trace points from ALL bots (including dead bots)
    for (Bot bot : allBots) {
      if (bot == null) continue;
      
      // OPTIMIZATION: Skip bots that are too far away (distance-based filtering)
      float botDistance = position.distance(bot.getPosition());
      if (botDistance > searchRadius) {
        continue;
      }
      
      java.util.ArrayList<TracePoint> botPath = bot.getPath();
      if (botPath == null || botPath.isEmpty()) continue;
      
      synchronized (botPath) {
        // OPTIMIZATION: Sample traces instead of checking every point
        int traceStep = Math.max(1, botPath.size() / 50);  // Target ~50 traces per bot
        int startSamplingFrom = Math.max(0, botPath.size() - 10);
        
        for (int i = 0; i < startSamplingFrom; i += traceStep) {
          TracePoint tp = botPath.get(i);
          if (tp != null) {
            float distance = position.distance(tp.position);
            if (distance > 0 && distance < repulsionRadius) {
              Vector2D away = new Vector2D(position.vx - tp.position.vx, position.vy - tp.position.vy);
              float mag = away.magnitude();
              if (mag > 0) {
                away.normalize();
                // Stronger repulsion when closer (inverse-distance falloff)
                float strength = 1.0f - (distance / repulsionRadius);
                away.scale(strength);
                repulsion.add(away);
              }
            }
          }
        }
        
        // Always check the last ~10 trace points for accuracy
        for (int i = startSamplingFrom; i < botPath.size(); i++) {
          TracePoint tp = botPath.get(i);
          if (tp != null) {
            float distance = position.distance(tp.position);
            if (distance > 0 && distance < repulsionRadius) {
              Vector2D away = new Vector2D(position.vx - tp.position.vx, position.vy - tp.position.vy);
              float mag = away.magnitude();
              if (mag > 0) {
                away.normalize();
                float strength = 1.0f - (distance / repulsionRadius);
                away.scale(strength);
                repulsion.add(away);
              }
            }
          }
        }
      }
    }
    
    // Normalize so repulsion is a unit-direction (or zero if no nearby traces)
    float mag = repulsion.magnitude();
    if (mag > 0) {
      repulsion.normalize();
    }
    
    return repulsion;
  }
  
  /**
   * Update bot position and velocity based on 3 weighted forces:
   *   velocity = (field × fieldInfluence + drift × driftInfluence + repulsion × repulsionInfluence) × speed
   * 
   * Forces are NOT normalized — their relative magnitudes matter.
   * Speed is a global scalar that caps the final velocity magnitude.
   * @param fieldForce The sampled vector field direction at bot position
   * @param allBots All bots (live and dead) used for repulsion calculation
   * @param lifeDecay How much life to subtract this frame (1.0 = normal, luminance-based = 0.0–1.0)
   */
  public void update(Vector2D fieldForce, java.util.ArrayList<Bot> allBots, float lifeDecay) {
    if (life <= 0) {
      return;
    }
    
    // Update drift direction slowly (change direction every DRIFT_CHANGE_INTERVAL frames)
    driftChangeCounter++;
    if (driftChangeCounter >= DRIFT_CHANGE_INTERVAL) {
      driftChangeCounter = 0;
      driftDirection = Vector2D.randomDirection();
    }
    
    // --- 3 directional forces, each scaled by its influence slider ---
    
    // 1. Vector Field force: the flow direction at this position
    Vector2D fieldComponent = fieldForce.copy();
    float fieldMag = fieldComponent.magnitude();
    if (fieldMag > 0) fieldComponent.normalize();  // Normalize field to unit direction
    fieldComponent.scale(fieldInfluence);
    
    // 2. Drift force: persistent random walk direction
    Vector2D driftComponent = driftDirection.copy();  // Already unit-length
    driftComponent.scale(driftInfluence);
    
    // 3. Repulsion force: avoidance of nearby traces
    Vector2D repulsionComponent = calculateRepulsion(allBots);  // Returns unit direction or zero
    repulsionComponent.scale(repulsionInfluence);
    
    // --- Sum all forces (no normalize — magnitudes carry the balance) ---
    Vector2D force = fieldComponent;
    force.add(driftComponent);
    force.add(repulsionComponent);
    
    // --- Apply speed as magnitude cap ---
    float forceMag = force.magnitude();
    if (forceMag > 0) {
      // Scale to speed, but if forces partially cancel out, velocity is proportionally slower
      velocity = force.copy();
      if (forceMag > 1.0f) {
        velocity.normalize();  // Only normalize if above unit length
      }
      velocity.scale(speed);
    } else {
      velocity.zero();
    }
    
    // Update position
    position.add(velocity);
    
    // Add point to path if distance is sufficient
    if (path.isEmpty() || 
        position.distance(path.get(path.size() - 1).position) > PATH_POINT_THRESHOLD) {
      path.add(new TracePoint(position.vx, position.vy, traceTransparency, traceColor));
    }
    
    // Decrease life
    life -= lifeDecay;
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
  
  public java.awt.Color getColor() {
    return botColor;
  }
  
  public void setColor(java.awt.Color color) {
    this.botColor = color;
  }
  
  public void setTraceTransparency(float transparency) {
    this.traceTransparency = Math.max(0.0f, Math.min(1.0f, transparency));
  }
  
  public void setTraceColor(java.awt.Color color) {
    this.traceColor = color != null ? color : new java.awt.Color(0x379439);
  }
  
  public java.awt.Color getTraceColor() {
    return traceColor;
  }
  
  public void setRepulsionRadius(float radius) {
    this.repulsionRadius = Math.max(0, radius);
  }
  
  public void setDriftInfluence(float influence) {
    this.driftInfluence = Math.max(0, Math.min(1, influence));
  }
  
  public void setFieldInfluence(float influence) {
    this.fieldInfluence = Math.max(0, Math.min(1, influence));
  }
  
  public void setRepulsionInfluence(float influence) {
    this.repulsionInfluence = Math.max(0, Math.min(1, influence));
  }
  
  public float getRadar() {
    return radar;
  }
  
  public ArrayList<TracePoint> getPath() {
    return path;
  }
  
  // Setters
  public void setRadar(float radar) {
    this.radar = radar;
  }
  
  public void setSpeed(float speed) {
    this.speed = speed;
  }
}
