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
  private float distanceSinceLastDab;
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
    distanceSinceLastDab = 0.0f;
    applyDab(x, y, 1.0f, 0.0f);
  }
  
  /**
   * Continue painting with mouse movement.
   * Returns true if brush was applied (respects spacing).
   * Behavior depends on brush mode:
   *   BRUSH: add forces to the field
   *   ERASER: subtract forces (move vectors toward zero)
   */
  public boolean paintAt(float x, float y) {
    if (!isPainting) return false;
    
    float dx = x - lastPaintX;
    float dy = y - lastPaintY;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);
    if (distance <= 0.0f) {
      return false;
    }

    float spacing = Math.max(1.0f, brush.getSpacing());
    float distanceToNextDab = spacing - distanceSinceLastDab;
    float nextDabDistance = distanceToNextDab;
    boolean applied = false;
    while (nextDabDistance <= distance) {
      float fraction = nextDabDistance / distance;
      float dabX = lastPaintX + dx * fraction;
      float dabY = lastPaintY + dy * fraction;
      applyDab(dabX, dabY, dx / distance, dy / distance);
      nextDabDistance += spacing;
      applied = true;
    }

    distanceSinceLastDab = (distanceSinceLastDab + distance) % spacing;
    lastPaintX = x;
    lastPaintY = y;
    return applied;
  }

  private void applyDab(float x, float y, float directionX, float directionY) {
    float strength = brush.getStrength();
    if (brush.getMode() == Brush.BrushMode.BRUSH) {
      field.addForceWithFalloff(
        x, y, brush.getSize(), directionX * strength, directionY * strength,
        brush.getEffectiveFalloffType(), brush.getHardness());
    } else {
      field.subtractForceWithFalloff(
        x, y, brush.getSize(), strength, strength,
        brush.getEffectiveFalloffType(), brush.getHardness());
    }
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
    applyDab(x, y, 1.0f, 0.0f);
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
