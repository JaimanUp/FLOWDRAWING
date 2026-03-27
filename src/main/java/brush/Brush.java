package brush;

import field.VectorField;

/**
 * Brush
 * Represents a brush tool for painting forces onto the vector field.
 * 
 * Phase 4: Brush System
 * - Shape: Circular brush with size parameter
 * - Falloff: Controls how force diminishes from center
 * - Hardness: Controls edge softness (inverse of falloff radius)
 * - Spacing: Minimum distance between brush strokes (prevents over-saturation)
 */
public class Brush {
  private float size;              // Brush radius in pixels
  private float hardness;          // 0.0 = soft (GAUSSIAN), 1.0 = hard (HARD_EDGE)
  private float strength;          // Force magnitude applied per stroke
  private float spacing;           // Minimum pixels between brush applications
  private VectorField.FalloffType falloffType;
  
  public static final float MIN_SIZE = 5.0f;
  public static final float MAX_SIZE = 100.0f;
  public static final float MIN_STRENGTH = 0.5f;
  public static final float MAX_STRENGTH = 2.5f;
  
  public Brush() {
    this.size = 30.0f;
    this.hardness = 0.5f;
    this.strength = 1.0f;
    this.spacing = 5.0f;
    this.falloffType = VectorField.FalloffType.GAUSSIAN;
  }
  
  public Brush(float size, float hardness, float strength, float spacing) {
    this.size = Math.max(MIN_SIZE, Math.min(MAX_SIZE, size));
    this.hardness = Math.max(0.0f, Math.min(1.0f, hardness));
    this.strength = Math.max(MIN_STRENGTH, Math.min(MAX_STRENGTH, strength));
    this.spacing = Math.max(1.0f, spacing);
    this.falloffType = VectorField.FalloffType.GAUSSIAN;
  }
  
  /**
   * Get effective falloff type based on hardness.
   * Hardness: 0.0 = Linear, 0.5 = Gaussian, 1.0 = Hard Edge
   */
  public VectorField.FalloffType getEffectiveFalloffType() {
    if (hardness < 0.33f) {
      return VectorField.FalloffType.LINEAR;
    } else if (hardness < 0.67f) {
      return VectorField.FalloffType.GAUSSIAN;
    } else {
      return VectorField.FalloffType.HARD_EDGE;
    }
  }
  
  // ========== Getters & Setters ==========
  public float getSize() {
    return size;
  }
  
  public void setSize(float size) {
    this.size = Math.max(MIN_SIZE, Math.min(MAX_SIZE, size));
  }
  
  public float getHardness() {
    return hardness;
  }
  
  public void setHardness(float hardness) {
    this.hardness = Math.max(0.0f, Math.min(1.0f, hardness));
  }
  
  public float getStrength() {
    return strength;
  }
  
  public void setStrength(float strength) {
    this.strength = Math.max(MIN_STRENGTH, Math.min(MAX_STRENGTH, strength));
  }
  
  public float getSpacing() {
    return spacing;
  }
  
  public void setSpacing(float spacing) {
    this.spacing = Math.max(1.0f, spacing);
  }
  
  public VectorField.FalloffType getFalloffType() {
    return falloffType;
  }
  
  public void setFalloffType(VectorField.FalloffType type) {
    this.falloffType = type;
  }
}
