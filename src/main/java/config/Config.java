package config;

/**
 * Config
 * Global configuration constants.
 * 
 * These values can be exposed to the UI later for runtime adjustment.
 */
public class Config {
  // Canvas defaults
  public static final int CANVAS_WIDTH = 800;
  public static final int CANVAS_HEIGHT = 600;
  
  // Vector field
  public static final float CELL_SIZE = 10.0f;  // pixels between grid points
  public static final float DEFAULT_MAX_MAGNITUDE = 5.0f;
  public static final float NORMALIZATION_THRESHOLD = 0.3f;  // 30% of vectors at max
  
  // Brush
  public static final float DEFAULT_BRUSH_SIZE = 50.0f;
  public static final float DEFAULT_BRUSH_HARDNESS = 0.5f;
  public static final float DEFAULT_BRUSH_SPACING = 5.0f;
  
  // Bot simulation
  public static final int DEFAULT_BOT_COUNT = 50;
  public static final float DEFAULT_BOT_LIFE = 300.0f;
  public static final float DEFAULT_BOT_RADAR = 50.0f;
  public static final float DEFAULT_BOT_DRIFT = 0.1f;
  public static final float BOT_SPEED = 2.0f;
  public static final int RADAR_SAMPLES = 16;
  
  // Render
  public static final int DEFAULT_FPS = 60;
}
