package field;

import config.Config;

/**
 * VectorField
 * Represents a 2D grid of vectors that guides bot movement.
 * 
 * From roadmap Phase 2:
 * - Grid of vectors: cols, rows, cellSize
 * - Each vector stores: vx, vy
 * - Magnitude limit: maxMagnitude
 * - Global rule: When 30% of vectors reach maxMagnitude,
 *   scale all vectors proportionally
 */
public class VectorField {
  private int cols;
  private int rows;
  private float cellSize;
  private Vector2D[][] vectors;
  private float maxMagnitude;
  private float fieldMagnitudeScale = 1.0f;
  private float normalizationThreshold;  // 0.3 = 30%
  
  public VectorField(int canvasWidth, int canvasHeight, float cellSize) {
    this.cellSize = cellSize;
    this.cols = Math.max(1, (int) Math.ceil(canvasWidth / cellSize));
    this.rows = Math.max(1, (int) Math.ceil(canvasHeight / cellSize));
    this.vectors = new Vector2D[cols][rows];
    this.maxMagnitude = Config.DEFAULT_MAX_MAGNITUDE;
    this.normalizationThreshold = Config.NORMALIZATION_THRESHOLD;
    
    // Initialize all vectors to zero
    for (int x = 0; x < cols; x++) {
      for (int y = 0; y < rows; y++) {
        vectors[x][y] = new Vector2D(0, 0);
      }
    }
  }
  
  /**
   * Sample the vector field at a given world position using bilinear interpolation.
   */
  public Vector2D sample(float x, float y) {
    float gridX = Math.max(0.0f, Math.min(cols - 1.0f, x / cellSize));
    float gridY = Math.max(0.0f, Math.min(rows - 1.0f, y / cellSize));

    int x0 = (int) Math.floor(gridX);
    int y0 = (int) Math.floor(gridY);
    int x1 = Math.min(cols - 1, x0 + 1);
    int y1 = Math.min(rows - 1, y0 + 1);
    float tx = gridX - x0;
    float ty = gridY - y0;

    Vector2D topLeft = vectors[x0][y0];
    Vector2D topRight = vectors[x1][y0];
    Vector2D bottomLeft = vectors[x0][y1];
    Vector2D bottomRight = vectors[x1][y1];

    float topX = topLeft.vx + (topRight.vx - topLeft.vx) * tx;
    float topY = topLeft.vy + (topRight.vy - topLeft.vy) * tx;
    float bottomX = bottomLeft.vx + (bottomRight.vx - bottomLeft.vx) * tx;
    float bottomY = bottomLeft.vy + (bottomRight.vy - bottomLeft.vy) * tx;

    return new Vector2D(
      (topX + (bottomX - topX) * ty) * fieldMagnitudeScale,
      (topY + (bottomY - topY) * ty) * fieldMagnitudeScale);
  }
  
  /**
   * Add a force to the vector at grid position (gx, gy).
   */
  public void addForce(int gx, int gy, float fx, float fy) {
    if (gx >= 0 && gx < cols && gy >= 0 && gy < rows) {
      vectors[gx][gy].add(fx, fy);
      vectors[gx][gy].clamp(maxMagnitude);
    }
  }
  
  /**
   * Add a force with falloff based on distance from center.
   */
  public void addForceWithFalloff(float centerX, float centerY, float radius, 
                                   float fx, float fy, FalloffType falloff) {
    addForceWithFalloff(centerX, centerY, radius, fx, fy, falloff, 0.0f);
  }

  public void addForceWithFalloff(float centerX, float centerY, float radius,
                                   float fx, float fy, FalloffType falloff, float hardness) {
    int centerGX = (int) (centerX / cellSize);
    int centerGY = (int) (centerY / cellSize);
    int radiusInCells = (int) Math.ceil(radius / cellSize);
    
    // Normalize force by radius so center impact is independent of brush size
    // Larger brush = smaller force per unit area, but same center impact
    float normalizedForce = (radius > 0) ? 1.0f / (radius * 0.1f) : 1.0f;
    float normalizedFx = fx * normalizedForce;
    float normalizedFy = fy * normalizedForce;
    
    for (int gx = centerGX - radiusInCells; gx <= centerGX + radiusInCells; gx++) {
      for (int gy = centerGY - radiusInCells; gy <= centerGY + radiusInCells; gy++) {
        if (gx >= 0 && gx < cols && gy >= 0 && gy < rows) {
          float dx = (gx - centerGX) * cellSize;
          float dy = (gy - centerGY) * cellSize;
          float dist = (float) Math.sqrt(dx * dx + dy * dy);
          
          if (dist < radius) {
            float factor = calculateFalloff(dist, radius, falloff, hardness);
            vectors[gx][gy].add(normalizedFx * factor, normalizedFy * factor);
            vectors[gx][gy].clamp(maxMagnitude);
          }
        }
      }
    }
  }
  
  /**
   * Subtract a force (eraser behavior) with falloff based on distance from center.
   * Moves vectors toward zero proportionally to the eraser strength.
   */
  public void subtractForceWithFalloff(float centerX, float centerY, float radius, 
                                        float fx, float fy, FalloffType falloff) {
    subtractForceWithFalloff(centerX, centerY, radius, fx, fy, falloff, 0.0f);
  }

  public void subtractForceWithFalloff(float centerX, float centerY, float radius,
                                        float fx, float fy, FalloffType falloff, float hardness) {
    int centerGX = (int) (centerX / cellSize);
    int centerGY = (int) (centerY / cellSize);
    int radiusInCells = (int) Math.ceil(radius / cellSize);
    
    for (int gx = centerGX - radiusInCells; gx <= centerGX + radiusInCells; gx++) {
      for (int gy = centerGY - radiusInCells; gy <= centerGY + radiusInCells; gy++) {
        if (gx >= 0 && gx < cols && gy >= 0 && gy < rows) {
          float dx = (gx - centerGX) * cellSize;
          float dy = (gy - centerGY) * cellSize;
          float dist = (float) Math.sqrt(dx * dx + dy * dy);
          
          if (dist < radius) {
            float strength = Math.min(1.0f, (Math.abs(fx) + Math.abs(fy)) * 0.5f);
            float factor = calculateFalloff(dist, radius, falloff, hardness) * strength;
            vectors[gx][gy].scale(1.0f - factor);
          }
        }
      }
    }
  }
  
  /**
   * Calculate falloff based on distance and type.
   */
  private float calculateFalloff(float distance, float radius, FalloffType type, float hardness) {
    float normalizedHardness = Math.max(0.0f, Math.min(1.0f, hardness));
    
    switch (type) {
      case LINEAR:
      case GAUSSIAN:
        if (distance <= radius * normalizedHardness) {
          return 1.0f;
        }
        float softEdgeWidth = radius * (1.0f - normalizedHardness);
        if (softEdgeWidth <= 0.0f) {
          return 0.0f;
        }
        float normalized = (distance - radius * normalizedHardness) / softEdgeWidth;
        return type == FalloffType.LINEAR
          ? 1.0f - normalized
          : (float) Math.exp(-(normalized * normalized));
      case HARD_EDGE:
        return 1.0f;  // No falloff
      default:
        return 1.0f - (distance / radius);
    }
  }
  
  /**
   * Check if normalization is needed and apply if threshold exceeded.
   * Global rule: When 30% of vectors reach maxMagnitude, scale all proportionally.
   */
  public void normalizeFieldIfNeeded() {
    int countAtMax = 0;
    float maxMag = 0;
    
    // Count vectors at or near maxMagnitude and track max
    for (int x = 0; x < cols; x++) {
      for (int y = 0; y < rows; y++) {
        float mag = vectors[x][y].magnitude();
        if (mag > maxMag) {
          maxMag = mag;
        }
        if (mag >= maxMagnitude * 0.95f) {  // Allow 5% tolerance
          countAtMax++;
        }
      }
    }
    
    // Check if threshold exceeded
    int totalVectors = cols * rows;
    float percentAtMax = (float) countAtMax / totalVectors;
    
    if (percentAtMax >= normalizationThreshold && maxMag > 0.0f) {
      float targetMagnitude = maxMagnitude * (1.0f - normalizationThreshold);
      float scaleFactor = Math.min(1.0f, targetMagnitude / maxMag);
      for (int x = 0; x < cols; x++) {
        for (int y = 0; y < rows; y++) {
          vectors[x][y].scale(scaleFactor);
        }
      }
    }
  }
  
  /**
   * Clear all vectors (reset field to zero).
   */
  public void clear() {
    for (int x = 0; x < cols; x++) {
      for (int y = 0; y < rows; y++) {
        vectors[x][y].vx = 0;
        vectors[x][y].vy = 0;
      }
    }
  }

  public void randomize() {
    for (int x = 0; x < cols; x++) {
      for (int y = 0; y < rows; y++) {
        double angle = Math.random() * 2.0 * Math.PI;
        float strength = 0.5f + (float) (Math.random() * 2.0);
        vectors[x][y].vx = (float) Math.cos(angle) * strength;
        vectors[x][y].vy = (float) Math.sin(angle) * strength;
      }
    }
  }

  public void normalizeVectors() {
    for (int x = 0; x < cols; x++) {
      for (int y = 0; y < rows; y++) {
        vectors[x][y].normalize();
      }
    }
  }
  
  // ========== Getters ==========
  public Vector2D[][] getVectors() {
    return vectors;
  }
  
  public Vector2D getVector(int x, int y) {
    if (x >= 0 && x < cols && y >= 0 && y < rows) {
      return vectors[x][y];
    }
    return new Vector2D(0, 0);  // Return zero vector for out-of-bounds
  }
  
  public float getCellSize() {
    return cellSize;
  }
  
  public float getMaxMagnitude() {
    return maxMagnitude;
  }

  public float getFieldMagnitudeScale() {
    return fieldMagnitudeScale;
  }

  public void setFieldMagnitudeScale(float scale) {
    if (Float.isNaN(scale) || Float.isInfinite(scale)) {
      return;
    }
    fieldMagnitudeScale = Math.max(0.0f, Math.min(2.0f, scale));
  }
  
  public int getCols() {
    return cols;
  }
  
  public int getRows() {
    return rows;
  }
  
  /**
   * Falloff type enum for brush force application.
   */
  public enum FalloffType {
    LINEAR,      // Linear falloff: 1 - (dist/radius)
    GAUSSIAN,    // Gaussian falloff: exp(-(dist²))
    HARD_EDGE    // No falloff: constant 1.0
  }
}
