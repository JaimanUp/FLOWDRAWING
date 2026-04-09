package bot;

import field.Vector2D;
import field.VectorField;
import java.util.ArrayList;

/**
 * BotEngine
 * Phase 5: Bot Simulation Engine
 * Manages bot spawning, updating, and lifecycle.
 */
public class BotEngine {
  private ArrayList<Bot> bots;
  private ArrayList<Bot> deadBots;  // Keeps traces of dead bots for persistent visualization
  private VectorField vectorField;
  
  // Bot parameters (configurable)
  private float botMaxLife = 500;
  private float botRadar = 50;    // Field sampling radius
  private float botSpeed = 2.0f;  // Maximum velocity magnitude
  
  // Repulsion detection range
  private float repulsionRadius = 50.0f;  // Distance range for repulsion detection
  
  // Force influence multipliers (0.0-1.0) - balance between the 3 directional forces
  private float driftInfluence = 0.3f;        // Random drift force weight (0-1)
  private float fieldInfluence = 1.0f;        // Vector field force weight (0-1)
  private float repulsionInfluence = 0.0f;    // Repulsion from traces force weight (0-1)
  
  // Spawn configuration
  private boolean autoSpawnEnabled = false;
  private int botNumber = 50;     // Target number of active bots (auto-spawn goal) or bots to spawn (manual spawn)
  private int maxBots = 500;      // Hard cap on total bots
  
  // Radar sampling configuration
  private VectorField.FalloffType radarSamplingFalloff = VectorField.FalloffType.GAUSSIAN;
  private int radarSamples = config.Config.RADAR_SAMPLES;  // Number of radial samples (default 16)
  private float radarSampleDistance = 0.7f;  // Sample at this fraction of radar radius (0.0-1.0)
  private float centerSampleWeight = 1.5f;  // Extra weight for center sample for stability
  
  // Simulation control
  private boolean simulationRunning = true;  // Start running by default
  
  // Background image luminance decay
  private java.awt.image.BufferedImage backgroundImage = null;
  private boolean luminanceDecayEnabled = false;
  
  public BotEngine(VectorField vectorField) {
    this.vectorField = vectorField;
    this.bots = new ArrayList<>();
    this.deadBots = new ArrayList<>();
  }
  
  /**
   * Update all bots: sample field, apply forces, advance simulation
   */
  public void update() {
    // Skip update if simulation is paused
    if (!simulationRunning) {
      return;
    }
    
    // Spawn new bots if auto-spawn enabled (until reaching target botNumber)
    if (autoSpawnEnabled) {
      while (bots.size() < botNumber && bots.size() < maxBots) {
        spawnRandomBot();
      }
    }
    
    // Create combined list of all bots (live and dead) for repulsion calculations
    java.util.ArrayList<Bot> allBots = new java.util.ArrayList<>();
    synchronized (bots) {
      allBots.addAll(bots);
    }
    synchronized (deadBots) {
      allBots.addAll(deadBots);
    }
    
    // Set force parameters on all bots before updating
    synchronized (bots) {
      for (Bot bot : bots) {
        bot.setRepulsionRadius(repulsionRadius);
        
        // Set force influence multipliers (the 3 directional forces)
        bot.setDriftInfluence(driftInfluence);
        bot.setFieldInfluence(fieldInfluence);
        bot.setRepulsionInfluence(repulsionInfluence);
      }
    }
    
    // Update existing bots
    for (int i = bots.size() - 1; i >= 0; i--) {
      Bot bot = bots.get(i);
      
      if (bot.isAlive()) {
        // Sample field using radar-based multi-point sampling
        Vector2D fieldForce = sampleFieldWithRadar(bot.getPosition(), bot.getRadar());
        
        // Check if there's any field influence in the radar area
        if (fieldForce.magnitude() == 0) {
          // No field information in radar area - remove this bot and respawn elsewhere
          deadBots.add(bots.remove(i));  // Move to deadBots to preserve trace
          
          // Immediately spawn a replacement bot in a valid location
          if (autoSpawnEnabled && bots.size() < maxBots) {
            spawnRandomBot();
          }
        } else {
          // Update bot with aggregated field force
          float lifeDecay;
          if (luminanceDecayEnabled && backgroundImage != null) {
            // Luminance-based life decay: bright = slow decay, dark = fast decay
            float luminance = getLuminanceAtPosition(bot.getPosition());
            lifeDecay = 1.0f - luminance;
          } else {
            // Normal: lose 1 life per frame
            lifeDecay = 1.0f;
          }
          bot.update(fieldForce, allBots, lifeDecay);
        }
      } else {
        // Move dead bots to deadBots list to preserve their traces
        deadBots.add(bots.remove(i));
      }
    }
  }
  
  /**
   * Sample vector field using radar-based multi-point aggregation.
   * Takes multiple radial samples around the bot and combines them using distance-based weighting.
   * Supports different falloff types (Linear, Gaussian, Hard Edge) for flexible motion characteristics.
   * @param centerPosition The center position to sample around
   * @param radarRadius The radius of the sampling area
   * @return Aggregated vector from all samples
   */
  private Vector2D sampleFieldWithRadar(Vector2D centerPosition, float radarRadius) {
    // Use configured number of radial samples around the bot position
    Vector2D aggregatedForce = new Vector2D(0, 0);
    float totalWeight = 0;
    
    // Take radial samples around the bot
    for (int i = 0; i < radarSamples; i++) {
      // Calculate angle for this sample
      float angle = (float) (2 * Math.PI * i / radarSamples);
      
      // Calculate sample position within radar radius
      float sampleDist = radarRadius * radarSampleDistance;  // Sample at configured fraction of radius
      float sampleX = centerPosition.vx + (float)(Math.cos(angle) * sampleDist);
      float sampleY = centerPosition.vy + (float)(Math.sin(angle) * sampleDist);
      
      // Sample the field at this position using bilinear interpolation
      Vector2D fieldSample = sampleFieldBilinear(new Vector2D(sampleX, sampleY));
      
      // Calculate distance-based weight using configured falloff type
      float distance = sampleDist;
      float weight = calculateRadarFalloff(distance, radarRadius);
      
      // Accumulate weighted sample
      fieldSample.scale(weight);
      aggregatedForce.add(fieldSample);
      totalWeight += weight;
    }
    
    // Include center sample with configured extra weight for stability
    Vector2D centerSample = sampleFieldBilinear(centerPosition);
    centerSample.scale(centerSampleWeight);  // Center sample has extra influence
    aggregatedForce.add(centerSample);
    totalWeight += centerSampleWeight;
    
    // Normalize by total weight to get average field influence
    if (totalWeight > 0) {
      aggregatedForce.scale(1.0f / totalWeight);
    }
    
    return aggregatedForce;
  }
  
  /**
   * Calculate falloff weight for radar sampling based on distance and configured falloff type.
   * @param distance Distance from center (0 to radarRadius)
   * @param radarRadius The maximum radar radius
   * @return Weight value (0.0 to 1.0) for this sample
   */
  private float calculateRadarFalloff(float distance, float radarRadius) {
    float normalized = distance / radarRadius;  // 0 to 1 (though we typically sample < radarRadius)
    normalized = Math.max(0, Math.min(1, normalized));  // Clamp to [0,1]
    
    switch (radarSamplingFalloff) {
      case LINEAR:
        // Linear: 1 at center, 0 at edge
        return 1.0f - normalized;
        
      case GAUSSIAN:
        // Gaussian: smoother falloff, emphasizes center more
        // Using sigma = 0.5 for appropriate falloff curve
        float sigma = 0.5f;
        return (float) Math.exp(-(normalized * normalized) / (2 * sigma * sigma));
        
      case HARD_EDGE:
        // Hard edge: constant weight (flat weighting)
        return 1.0f;
        
      default:
        return 1.0f - normalized;  // Default to linear
    }
  }
  
  /**
   * Sample vector field at arbitrary position using bilinear interpolation
   * @param position The world position to sample at
   * @return Interpolated vector at that position
   */
  private Vector2D sampleFieldBilinear(Vector2D position) {
    // Get field grid parameters
    int cols = vectorField.getCols();
    int rows = vectorField.getRows();
    float cellSize = vectorField.getCellSize();
    
    // Convert world position to grid coordinates
    float gridX = position.vx / cellSize;
    float gridY = position.vy / cellSize;
    
    // Get integer grid indices
    int x0 = (int) Math.floor(gridX);
    int y0 = (int) Math.floor(gridY);
    int x1 = x0 + 1;
    int y1 = y0 + 1;
    
    // Clamp to grid bounds
    x0 = Math.max(0, Math.min(x0, cols - 1));
    x1 = Math.max(0, Math.min(x1, cols - 1));
    y0 = Math.max(0, Math.min(y0, rows - 1));
    y1 = Math.max(0, Math.min(y1, rows - 1));
    
    // Get fractional parts for interpolation
    float fx = gridX - (int)gridX;
    float fy = gridY - (int)gridY;
    
    // Clamp fractional parts
    fx = Math.max(0, Math.min(1, fx));
    fy = Math.max(0, Math.min(1, fy));
    
    // Get 4 surrounding vectors from field
    Vector2D v00 = vectorField.getVector(x0, y0);
    Vector2D v10 = vectorField.getVector(x1, y0);
    Vector2D v01 = vectorField.getVector(x0, y1);
    Vector2D v11 = vectorField.getVector(x1, y1);
    
    // Bilinear interpolation
    float vx = lerp(
        lerp(v00.vx, v10.vx, fx),
        lerp(v01.vx, v11.vx, fx),
        fy
    );
    
    float vy = lerp(
        lerp(v00.vy, v10.vy, fx),
        lerp(v01.vy, v11.vy, fx),
        fy
    );
    
    return new Vector2D(vx, vy);
  }
  
  /**
   * Linear interpolation helper
   */
  private float lerp(float a, float b, float t) {
    return a + (b - a) * t;
  }
  
  /**
   * Spawn a bot at random position with configured parameters
   */
  public void spawnRandomBot() {
    float canvasWidth = vectorField.getCols() * vectorField.getCellSize();
    float canvasHeight = vectorField.getRows() * vectorField.getCellSize();
    
    // Try to find a location with field influence (up to 10 attempts)
    float x = 0, y = 0;
    boolean foundGoodLocation = false;
    for (int attempt = 0; attempt < 10; attempt++) {
      x = (float) (Math.random() * canvasWidth);
      y = (float) (Math.random() * canvasHeight);
      
      // Check if there's field influence at this location
      int gridX = Math.round(x / vectorField.getCellSize());
      int gridY = Math.round(y / vectorField.getCellSize());
      
      // Clamp to valid grid bounds
      gridX = Math.max(0, Math.min(vectorField.getCols() - 1, gridX));
      gridY = Math.max(0, Math.min(vectorField.getRows() - 1, gridY));
      
      Vector2D fieldVector = vectorField.getVector(gridX, gridY);
      if (fieldVector != null && fieldVector.magnitude() > 0) {
        foundGoodLocation = true;
        break;
      }
    }
    
    // If no good location found after 10 attempts, use last attempt location anyway
    Bot bot = new Bot(x, y, botMaxLife, botRadar, botSpeed);
    bots.add(bot);
  }
  
  /**
   * Manually spawn a bot at specified position
   */
  public void spawnBotAt(float x, float y) {
    if (bots.size() < maxBots) {
      Bot bot = new Bot(x, y, botMaxLife, botRadar, botSpeed);
      bots.add(bot);
    }
  }
  
  /**
   * Spawn multiple bots based on bot number (exactly botNumber bots)
   */
  public void spawnMultipleBots() {
    for (int i = 0; i < botNumber && bots.size() < maxBots; i++) {
      spawnRandomBot();
    }
  }
  
  /**
   * Clear all bots and dead bot traces
   */
  public void clearBots() {
    bots.clear();
    deadBots.clear();
  }
  
  /**
   * Clear only dead bot traces, keeping live bots.
   */
  public void clearTraces() {
    deadBots.clear();
    // Clear path history of live bots (keep only current position)
    synchronized (bots) {
      for (Bot bot : bots) {
        java.util.ArrayList<Bot.TracePoint> path = bot.getPath();
        if (path != null && path.size() > 1) {
          Bot.TracePoint last = path.get(path.size() - 1);
          path.clear();
          path.add(last);
        }
      }
    }
  }
  
  // Getters
  public ArrayList<Bot> getBots() {
    return bots;
  }
  
  public ArrayList<Bot> getDeadBots() {
    return deadBots;
  }
  
  public int getBotCount() {
    return bots.size();
  }
  
  public int getBotNumber() {
    return botNumber;
  }
  
  /**
   * @deprecated Use getBotNumber() instead
   */
  @Deprecated
  public int getSpawnRate() {
    return botNumber;
  }
  
  // Setters
  public void setBotMaxLife(float life) {
    this.botMaxLife = life;
  }
  
  public void setBotRadar(float radar) {
    this.botRadar = radar;
  }
  
  public void setBotSpeed(float speed) {
    this.botSpeed = speed;
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
  
  public void setAutoSpawnEnabled(boolean enabled) {
    this.autoSpawnEnabled = enabled;
  }
  
  public void setBotNumber(int number) {
    this.botNumber = Math.max(1, Math.min(number, maxBots));
  }
  
  /**
   * @deprecated Use setBotNumber() instead
   */
  @Deprecated
  public void setSpawnRate(int rate) {
    setBotNumber(rate);
  }
  
  public void setMaxBots(int max) {
    this.maxBots = Math.max(1, max);
  }
  
  // Simulation control
  public void startSimulation() {
    this.simulationRunning = true;
  }
  
  public void pauseSimulation() {
    this.simulationRunning = false;
  }
  
  /**
   * Toggle between running and paused states.
   * @return true if simulation is now running, false if paused
   */
  public boolean toggleSimulation() {
    this.simulationRunning = !this.simulationRunning;
    return this.simulationRunning;
  }
  
  /**
   * Reset simulation: stop, clear all bots and their traces.
   */
  public void resetSimulation() {
    this.simulationRunning = false;
    this.bots.clear();
    this.deadBots.clear();
  }
  
  public boolean isSimulationRunning() {
    return simulationRunning;
  }
  
  /**
   * Get the number of currently active (alive) bots.
   */
  public int getActiveBotCount() {
    return bots.size();
  }
  
  /**
   * Get the number of dead bots (traces preserved).
   */
  public int getDeadBotCount() {
    return deadBots.size();
  }
  
  // Background image and luminance decay
  public void setBackgroundImage(java.awt.image.BufferedImage img) {
    this.backgroundImage = img;
  }
  
  public void setLuminanceDecayEnabled(boolean enabled) {
    this.luminanceDecayEnabled = enabled;
  }
  
  // Radar sampling configuration
  /**
   * Set the falloff type used for radar sampling.
   * Affects how distance-based weighting is applied to field samples.
   * @param falloff The falloff type (LINEAR, GAUSSIAN, or HARD_EDGE)
   */
  public void setRadarSamplingFalloff(VectorField.FalloffType falloff) {
    if (falloff != null) {
      this.radarSamplingFalloff = falloff;
    }
  }
  
  /**
   * Get the current falloff type for radar sampling.
   */
  public VectorField.FalloffType getRadarSamplingFalloff() {
    return radarSamplingFalloff;
  }
  
  /**
   * Set the number of radial samples to take around each bot.
   * More samples = smoother motion but more computation.
   * @param samples Number of samples (typically 8-32)
   */
  public void setRadarSamples(int samples) {
    this.radarSamples = Math.max(1, samples);
  }
  
  /**
   * Get the current number of radar samples.
   */
  public int getRadarSamples() {
    return radarSamples;
  }
  
  /**
   * Set the distance at which samples are taken relative to radar radius.
   * 0.7 means samples are taken at 70% of radar radius.
   * @param distance Fraction of radar radius (0.0-1.0)
   */
  public void setRadarSampleDistance(float distance) {
    this.radarSampleDistance = Math.max(0.1f, Math.min(1.0f, distance));
  }
  
  /**
   * Get the current radar sample distance fraction.
   */
  public float getRadarSampleDistance() {
    return radarSampleDistance;
  }
  
  /**
   * Set the extra weight multiplier for the center sample.
   * Higher values emphasize the center position over surrounding samples.
   * @param weight Weight multiplier (typically 1.0-3.0)
   */
  public void setCenterSampleWeight(float weight) {
    this.centerSampleWeight = Math.max(0.1f, weight);
  }
  
  /**
   * Get the current center sample weight.
   */
  public float getCenterSampleWeight() {
    return centerSampleWeight;
  }
  
  /**
   * Get the current radar sampling falloff type as a string.
   * Useful for UI display.
   */
  public String getRadarSamplingFalloffName() {
    switch (radarSamplingFalloff) {
      case LINEAR:
        return "Linear";
      case GAUSSIAN:
        return "Gaussian";
      case HARD_EDGE:
        return "Hard Edge";
      default:
        return "Unknown";
    }
  }
  
  /**
   * Get luminance (brightness) at a position in the background image.
   * Returns 0.0 (black) to 1.0 (white).
   * Returns 1.0 if no background image is loaded.
   */
  public float getLuminanceAtPosition(Vector2D position) {
    if (backgroundImage == null || !luminanceDecayEnabled) {
      return 1.0f;  // No modulation if no image or feature disabled
    }
    
    // Clamp position to image bounds
    int x = (int) Math.max(0, Math.min(backgroundImage.getWidth() - 1, position.vx));
    int y = (int) Math.max(0, Math.min(backgroundImage.getHeight() - 1, position.vy));
    
    // Sample pixel and convert to luminance
    int rgb = backgroundImage.getRGB(x, y);
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;
    
    // Standard luminance formula: 0.299*R + 0.587*G + 0.114*B
    float luminance = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f;
    return luminance;
  }
}
