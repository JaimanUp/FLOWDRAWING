/**
 * Config
 * Global configuration constants.
 * 
 * These values can be exposed to the UI later for runtime adjustment.
 */
class Config {
  // Canvas defaults
  static final int CANVAS_WIDTH = 800;
  static final int CANVAS_HEIGHT = 600;
  
  // Vector field
  static final float CELL_SIZE = 10.0;  // pixels between grid points
  static final float DEFAULT_MAX_MAGNITUDE = 5.0;
  static final float NORMALIZATION_THRESHOLD = 0.3;  // 30% of vectors at max
  
  // Brush
  static final float DEFAULT_BRUSH_SIZE = 50.0;
  static final float DEFAULT_BRUSH_HARDNESS = 0.5;
  static final float DEFAULT_BRUSH_SPACING = 5.0;
  
  // Bot simulation
  static final int DEFAULT_BOT_COUNT = 50;
  static final float DEFAULT_BOT_LIFE = 300.0;
  static final float DEFAULT_BOT_RADAR = 50.0;
  static final float DEFAULT_BOT_DRIFT = 0.1;
  static final float BOT_SPEED = 2.0;
  static final int RADAR_SAMPLES = 16;
  
  // Render
  static final int DEFAULT_FPS = 60;
}
