package bot;

import config.Config;
import field.Vector2D;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;

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
  private final Deque<Float> recentTurnAngles = new ArrayDeque<>();
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
    float searchRadiusSquared = repulsionRadius * repulsionRadius;

    // Check stored trace points, independent of the bot's current position.
    for (Bot bot : allBots) {
      if (bot == null) continue;

      java.util.ArrayList<TracePoint> botPath = bot.getPath();
      if (botPath == null || botPath.isEmpty()) continue;
      
      synchronized (botPath) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (TracePoint point : botPath) {
          if (point == null || point.position == null) continue;
          minX = Math.min(minX, point.position.vx);
          minY = Math.min(minY, point.position.vy);
          maxX = Math.max(maxX, point.position.vx);
          maxY = Math.max(maxY, point.position.vy);
        }

        if (minX == Float.POSITIVE_INFINITY) continue;
        float nearestX = Math.max(minX, Math.min(position.vx, maxX));
        float nearestY = Math.max(minY, Math.min(position.vy, maxY));
        float boundsDx = position.vx - nearestX;
        float boundsDy = position.vy - nearestY;
        if (boundsDx * boundsDx + boundsDy * boundsDy > searchRadiusSquared) continue;

        // Ignore this bot's own trailing segment still inside the radius: it always sits
        // right behind the bot and would push it off course. Older self-crossings still repel.
        int end = botPath.size();
        if (bot == this) {
          while (end > 0 && position.distance(botPath.get(end - 1).position) < repulsionRadius) {
            end--;
          }
        }

        // OPTIMIZATION: Sample traces instead of checking every point
        int traceStep = Math.max(1, end / 50);  // Target ~50 traces per bot
        int startSamplingFrom = Math.max(0, end - 10);
        
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
        for (int i = startSamplingFrom; i < end; i++) {
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
    update(fieldForce, allBots, lifeDecay, 0.0f);
  }

  public void update(Vector2D fieldForce, java.util.ArrayList<Bot> allBots,
                     float lifeDecay, float instability) {
    update(fieldForce, allBots, lifeDecay, instability, null);
  }

  public void update(Vector2D fieldForce, java.util.ArrayList<Bot> allBots,
                     float lifeDecay, float instability, Vector2D driftOverride) {
    if (life <= 0) {
      return;
    }
    float boundedInstability = Math.max(0.0f, Math.min(1.0f, instability));
    
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
    
    // 2. Drift force: persistent random walk direction (max force = 0.25)
    Vector2D driftComponent = driftOverride == null
      ? driftDirection.copy() : driftOverride.copy();
    driftComponent.scale(driftInfluence * 0.25f * (1.0f - boundedInstability * 0.75f));
    
    // 3. Repulsion force: avoidance of nearby traces (max force = 2.0)
    Vector2D repulsionComponent = calculateRepulsion(allBots);  // Returns unit direction or zero
    repulsionComponent.scale(repulsionInfluence * 2.0f);
    
    // --- Sum all forces (no normalize — magnitudes carry the balance) ---
    Vector2D force = fieldComponent;
    force.add(driftComponent);
    force.add(repulsionComponent);
    
    // --- Apply speed as magnitude cap ---
    float forceMag = force.magnitude();
    Vector2D targetVelocity;
    if (forceMag > 0) {
      targetVelocity = force.copy();
      if (forceMag > 1.0f) {
        targetVelocity.normalize();
      }
      targetVelocity.scale(speed * (1.0f - boundedInstability * 0.25f));
    } else {
      targetVelocity = new Vector2D(0.0f, 0.0f);
    }

    float previousMagnitude = velocity.magnitude();
    float targetMagnitude = targetVelocity.magnitude();
    if (previousMagnitude > 0.0f && targetMagnitude > 0.0f) {
      Vector2D previousDirection = velocity.copy();
      Vector2D targetDirection = targetVelocity.copy();
      previousDirection.normalize();
      targetDirection.normalize();
      float dot = Math.max(-1.0f, Math.min(1.0f,
        previousDirection.vx * targetDirection.vx + previousDirection.vy * targetDirection.vy));
      float angle = (float) Math.acos(dot);
      recordTurnAngle(angle);
      float turnAmount = angle / (float) Math.PI;
      float smoothing = Config.BOT_SMOOTHING_MAX_FACTOR
        - turnAmount * (Config.BOT_SMOOTHING_MAX_FACTOR - Config.BOT_SMOOTHING_MIN_FACTOR);
      velocity.scale(1.0f - smoothing);
      velocity.add(targetVelocity.vx * smoothing, targetVelocity.vy * smoothing);
    } else {
      velocity = targetVelocity;
    }
    
    // Update position
    position.add(velocity);
    
    // Add point to path if distance is sufficient
    if (path.isEmpty() || 
        position.distance(path.get(path.size() - 1).position) > PATH_POINT_THRESHOLD) {
      synchronized (path) {
        path.add(new TracePoint(position.vx, position.vy, traceTransparency, traceColor));
      }
    }
    
    // Decrease life
    life -= lifeDecay;
  }

  private void recordTurnAngle(float angle) {
    if (recentTurnAngles.size() == 12) {
      recentTurnAngles.removeFirst();
    }
    recentTurnAngles.addLast(angle);
  }

  public float getCurvatureVariance() {
    if (recentTurnAngles.size() < 2) {
      return 0.0f;
    }
    float mean = 0.0f;
    for (float angle : recentTurnAngles) {
      mean += angle;
    }
    mean /= recentTurnAngles.size();
    float variance = 0.0f;
    for (float angle : recentTurnAngles) {
      float difference = angle - mean;
      variance += difference * difference;
    }
    return variance / recentTurnAngles.size();
  }
  
  /**
   * Check if bot is still alive
   */
  public boolean isAlive() {
    return life > 0;
  }

  /**
   * End this bot's life immediately; its trace is kept.
   */
  public void kill() {
    life = 0;
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

  public float getSpeed() {
    return speed;
  }
  
  public ArrayList<TracePoint> getPath() {
    return path;
  }
  
  // Setters
  public void setRadar(float radar) {
    this.radar = Math.max(0.0f, radar);
  }
  
  public void setSpeed(float speed) {
    this.speed = Math.max(0.0f, speed);
  }

  public void setMaxLife(float maxLife) {
    float normalizedLife = getLifeNormalized();
    this.maxLife = Math.max(1.0f, maxLife);
    this.life = normalizedLife * this.maxLife;
  }
}
