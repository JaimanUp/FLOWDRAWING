package field;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

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
  private float normalizationThreshold;  // 0.3 = 30%
  
  public VectorField(int canvasWidth, int canvasHeight, float cellSize) {
    this.cellSize = cellSize;
    this.cols = Math.max(1, (int) Math.ceil(canvasWidth / cellSize));
    this.rows = Math.max(1, (int) Math.ceil(canvasHeight / cellSize));
    this.vectors = new Vector2D[cols][rows];
    this.maxMagnitude = 5.0f;  // Default from Config
    this.normalizationThreshold = 0.3f;  // 30% from Config
    
    // Initialize all vectors to zero
    for (int x = 0; x < cols; x++) {
      for (int y = 0; y < rows; y++) {
        vectors[x][y] = new Vector2D(0, 0);
      }
    }
  }
  
  /**
   * Sample the vector field at a given world position.
   * Uses nearest-neighbor for now (TODO: implement bilinear).
   */
  public Vector2D sample(float x, float y) {
    float gridX = x / cellSize;
    float gridY = y / cellSize;
    
    int ix = (int) Math.floor(gridX);
    int iy = (int) Math.floor(gridY);
    
    // Clamp to grid bounds
    ix = Math.max(0, Math.min(cols - 1, ix));
    iy = Math.max(0, Math.min(rows - 1, iy));
    
    return vectors[ix][iy].copy();
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
            float factor = calculateFalloff(dist, radius, falloff);
            vectors[gx][gy].add(normalizedFx * factor, normalizedFy * factor);
            vectors[gx][gy].clamp(maxMagnitude);
          }
        }
      }
    }
  }
  
  /**
   * Calculate falloff based on distance and type.
   */
  private float calculateFalloff(float distance, float radius, FalloffType type) {
    float normalized = distance / radius;  // 0 to 1
    
    switch (type) {
      case LINEAR:
        return 1.0f - normalized;
      case GAUSSIAN:
        return (float) Math.exp(-(normalized * normalized));
      case HARD_EDGE:
        return 1.0f;  // No falloff
      default:
        return 1.0f - normalized;
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
    
    if (percentAtMax >= normalizationThreshold) {
      float[] sumBefore = sumVectors();
      // Scale all vectors proportionally
      if (maxMag > maxMagnitude) {
        float scaleFactor = maxMagnitude / maxMag;
        for (int x = 0; x < cols; x++) {
          for (int y = 0; y < rows; y++) {
            vectors[x][y].scale(scaleFactor);
          }
        }
        float[] sumAfter = sumVectors();
        logNormalizationWithSums(percentAtMax, maxMag, scaleFactor, sumBefore, sumAfter);
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
  
  private void logNormalization(float percentAtMax, float maxMag, float scaleFactor) {
    try (FileWriter fw = new FileWriter("normalization_log.txt", true)) {
      fw.write(String.format("%s - Normalization triggered: percentAtMax=%.3f, maxMag=%.3f, scaleFactor=%.3f\n",
        LocalDateTime.now(), percentAtMax, maxMag, scaleFactor));
    } catch (IOException e) {
      // Ignore logging errors
    }
  }
  
  private void logNormalizationWithSums(float percentAtMax, float maxMag, float scaleFactor, float[] sumBefore, float[] sumAfter) {
    try (FileWriter fw = new FileWriter("normalization_log.txt", true)) {
      fw.write(String.format("%s - Normalization: percentAtMax=%.3f, maxMag=%.3f, scaleFactor=%.3f\n",
        LocalDateTime.now(), percentAtMax, maxMag, scaleFactor));
      fw.write(String.format("  Sum before: vx=%.3f, vy=%.3f\n", sumBefore[0], sumBefore[1]));
      fw.write(String.format("  Sum after:  vx=%.3f, vy=%.3f\n", sumAfter[0], sumAfter[1]));
    } catch (IOException e) {
      // Ignore logging errors
    }
  }
  
  private float[] sumVectors() {
    float sumX = 0, sumY = 0;
    for (int x = 0; x < cols; x++) {
      for (int y = 0; y < rows; y++) {
        sumX += vectors[x][y].vx;
        sumY += vectors[x][y].vy;
      }
    }
    return new float[]{sumX, sumY};
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
