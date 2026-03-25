package brush;

import field.VectorField;
import field.Vector2D;

/**
 * BrushEngine
 * Manages brush state and applies brush strokes to the vector field.
 * 
 * Phase 4: Brush System
 * - Tracks brush position and applies forces
 * - Implements spacing to prevent over-saturation
 * - Calculates force direction based on brush movement
 * - Handles continuous painting (mouse drag)
 */
public class BrushEngine {
  private Brush brush;
  private VectorField field;
  private float lastPaintX, lastPaintY;
  private boolean isPainting = false;
  
  public BrushEngine(Brush brush, VectorField field) {
    this.brush = brush;
    this.field = field;
    this.lastPaintX = -1;
    this.lastPaintY = -1;
  }
  
  /**
   * Start a brush stroke at the given position.
   */
  public void startStroke(float x, float y) {
    isPainting = true;
    lastPaintX = x;
    lastPaintY = y;
  }
  
  /**
   * Continue painting with mouse movement.
   * Returns true if brush was applied (respects spacing).
   */
  public boolean paintAt(float x, float y) {
    if (!isPainting) return false;
    
    // Calculate distance from last paint position
    float dx = x - lastPaintX;
    float dy = y - lastPaintY;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);
    
    // Apply if distance exceeds spacing threshold
    if (distance >= brush.getSpacing()) {
      // Calculate force direction from movement
      float forceX = 0, forceY = 0;
      if (distance > 0) {
        forceX = (dx / distance) * brush.getStrength();
        forceY = (dy / distance) * brush.getStrength();
      }
      
      // Apply brush force to field with falloff
      field.addForceWithFalloff(
        x, y,
        brush.getSize(),
        forceX,
        forceY,
        brush.getEffectiveFalloffType()
      );
      
      // Update last paint position for next spacing check
      lastPaintX = x;
      lastPaintY = y;
      
      return true;
    }
    
    return false;
  }
  
  /**
   * End the current brush stroke.
   * Applies field normalization after painting.
   */
  public void endStroke() {
    if (isPainting) {
      isPainting = false;
      // Apply normalization rule after stroke
      field.normalizeFieldIfNeeded();
    }
  }
  
  /**
   * Paint a single dot at the given position (no spacing requirement).
   */
  public void paintDot(float x, float y) {
    field.addForceWithFalloff(
      x, y,
      brush.getSize(),
      brush.getStrength(),
      0,  // No vertical component for dot
      brush.getEffectiveFalloffType()
    );
    field.normalizeFieldIfNeeded();
  }
  
  // ========== Getters & Setters ==========
  public Brush getBrush() {
    return brush;
  }
  
  public void setBrush(Brush brush) {
    this.brush = brush;
  }
  
  public boolean isPainting() {
    return isPainting;
  }
  
  public VectorField getField() {
    return field;
  }
}
