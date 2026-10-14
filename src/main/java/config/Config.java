package config;

import java.awt.Dimension;

/**
 * Config
 * Global configuration constants.
 * 
 * These values can be exposed to the UI later for runtime adjustment.
 */
public class Config {
  // Canvas defaults
  public static final int CANVAS_WIDTH = 2000;
  public static final int CANVAS_HEIGHT = 2000;

  public static Dimension canvasSizeForViewport(int viewportWidth, int viewportHeight) {
    if (viewportWidth <= 0 || viewportHeight <= 0) {
      return new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    if (viewportWidth >= viewportHeight) {
      return new Dimension(
        Math.max(CANVAS_WIDTH, Math.round(CANVAS_HEIGHT * viewportWidth / (float) viewportHeight)),
        CANVAS_HEIGHT);
    }

    return new Dimension(
      CANVAS_WIDTH,
      Math.max(CANVAS_HEIGHT, Math.round(CANVAS_WIDTH * viewportHeight / (float) viewportWidth)));
  }
  
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
  public static final int DEFAULT_BOT_SPAWN_RATE = 6;
  public static final float DEFAULT_BOT_LIFE = 300.0f;
  public static final float DEFAULT_BOT_RADAR = 50.0f;
  public static final float DEFAULT_BOT_DRIFT = 0.1f;
  public static final float BOT_SPEED = 2.0f;
  public static final float BOT_SMOOTHING_MIN_FACTOR = 0.2f;
  public static final float BOT_SMOOTHING_MAX_FACTOR = 0.8f;
  public static final float BOT_STABILITY_FIELD_VARIANCE_THRESHOLD = 1.0f;
  public static final int BOT_STABILITY_NEARBY_BOT_THRESHOLD = 8;
  public static final float BOT_STABILITY_CURVATURE_VARIANCE_THRESHOLD = 0.5f;
  public static final int MIN_RADAR_SAMPLES = 4;
  public static final int RADAR_SAMPLES = 16;
  public static final int MAX_RADAR_SAMPLES = 32;
  
  // Render
  public static final int DEFAULT_FPS = 60;
}
