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
  private VectorField vectorField;
  
  // Bot parameters (configurable)
  private float botMaxLife = 500;
  private float botRadar = 50;    // Field sampling radius
  private float botDrift = 0.3f;  // Random walk influence (0-1)
  private float botSpeed = 2.0f;  // Velocity magnitude
  
  // Spawn configuration
  private boolean autoSpawnEnabled = false;
  private int spawnRate = 5;      // Bots per frame
  private int maxBots = 500;
  
  public BotEngine(VectorField vectorField) {
    this.vectorField = vectorField;
    this.bots = new ArrayList<>();
  }
  
  /**
   * Update all bots: sample field, apply forces, advance simulation
   */
  public void update() {
    // Spawn new bots if auto-spawn enabled
    if (autoSpawnEnabled) {
      for (int i = 0; i < spawnRate && bots.size() < maxBots; i++) {
        spawnRandomBot();
      }
    }
    
    // Update existing bots
    for (int i = bots.size() - 1; i >= 0; i--) {
      Bot bot = bots.get(i);
      
      if (bot.isAlive()) {
        // Sample field at bot position using bilinear interpolation
        Vector2D fieldForce = sampleFieldBilinear(bot.getPosition());
        
        // Update bot with field force
        bot.update(fieldForce);
      } else {
        // Remove dead bots
        bots.remove(i);
      }
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
    
    float x = (float) (Math.random() * canvasWidth);
    float y = (float) (Math.random() * canvasHeight);
    
    Bot bot = new Bot(x, y, botMaxLife, botRadar, botDrift, botSpeed);
    bots.add(bot);
  }
  
  /**
   * Manually spawn a bot at specified position
   */
  public void spawnBotAt(float x, float y) {
    if (bots.size() < maxBots) {
      Bot bot = new Bot(x, y, botMaxLife, botRadar, botDrift, botSpeed);
      bots.add(bot);
    }
  }
  
  /**
   * Clear all bots
   */
  public void clearBots() {
    bots.clear();
  }
  
  // Getters
  public ArrayList<Bot> getBots() {
    return bots;
  }
  
  public int getBotCount() {
    return bots.size();
  }
  
  public float getBotMaxLife() {
    return botMaxLife;
  }
  
  public float getBotRadar() {
    return botRadar;
  }
  
  public float getBotDrift() {
    return botDrift;
  }
  
  public float getBotSpeed() {
    return botSpeed;
  }
  
  public boolean isAutoSpawnEnabled() {
    return autoSpawnEnabled;
  }
  
  public int getSpawnRate() {
    return spawnRate;
  }
  
  public int getMaxBots() {
    return maxBots;
  }
  
  // Setters
  public void setBotMaxLife(float life) {
    this.botMaxLife = life;
  }
  
  public void setBotRadar(float radar) {
    this.botRadar = radar;
  }
  
  public void setBotDrift(float drift) {
    this.botDrift = Math.max(0, Math.min(1, drift));
  }
  
  public void setBotSpeed(float speed) {
    this.botSpeed = speed;
  }
  
  public void setAutoSpawnEnabled(boolean enabled) {
    this.autoSpawnEnabled = enabled;
  }
  
  public void setSpawnRate(int rate) {
    this.spawnRate = Math.max(1, rate);
  }
  
  public void setMaxBots(int max) {
    this.maxBots = Math.max(1, max);
  }
}
