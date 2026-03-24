package render;

import field.VectorField;
import field.Vector2D;
import java.awt.Graphics2D;
import java.awt.Color;

/**
 * FieldRenderer
 * Handles visualization of the vector field.
 * Two modes: Arrow mode and Flow map mode (Phase 3)
 */
public class FieldRenderer {
  private VectorField field;
  private boolean arrowMode = true;  // true = arrows, false = flow map
  private float arrowScale = 5.0f;  // Scale factor for arrow display
  
  public FieldRenderer(VectorField field) {
    this.field = field;
  }
  
  public void render(Graphics2D g2d, CameraController camera) {
    if (field == null) return;
    
    if (arrowMode) {
      renderArrowMode(g2d);
    } else {
      renderFlowMapMode(g2d);
    }
  }
  
  /**
   * Render as arrows showing direction and magnitude.
   */
  private void renderArrowMode(Graphics2D g2d) {
    Vector2D[][] vectors = field.getVectors();
    float cellSize = field.getCellSize();
    
    g2d.setColor(new Color(100, 150, 200, 150));  // Semi-transparent blue
    g2d.setStroke(new java.awt.BasicStroke(1.0f));
    
    for (int x = 0; x < vectors.length; x++) {
      for (int y = 0; y < vectors[x].length; y++) {
        float worldX = x * cellSize + cellSize / 2;
        float worldY = y * cellSize + cellSize / 2;
        
        Vector2D v = vectors[x][y];
        float mag = v.magnitude();
        
        if (mag > 0.1f) {  // Only draw if significant
          // Normalize for display
          float displayLen = Math.min(mag * arrowScale, cellSize * 0.4f);
          float nx = v.vx / mag;
          float ny = v.vy / mag;
          
          float endX = worldX + nx * displayLen;
          float endY = worldY + ny * displayLen;
          
          // Draw line
          g2d.drawLine(
            (int) worldX, (int) worldY,
            (int) endX, (int) endY
          );
          
          // Draw arrowhead
          double angle = Math.atan2(ny, nx);
          drawArrowHead(g2d, (int) endX, (int) endY, angle, 5);
        }
      }
    }
  }
  
  /**
   * Render as flow map (color-coded by direction and magnitude).
   */
  private void renderFlowMapMode(Graphics2D g2d) {
    Vector2D[][] vectors = field.getVectors();
    float cellSize = field.getCellSize();
    
    for (int x = 0; x < vectors.length; x++) {
      for (int y = 0; y < vectors[x].length; y++) {
        Vector2D v = vectors[x][y];
        float mag = v.magnitude();
        
        // Color based on direction (hue) and brightness based on magnitude
        float hue = (float) Math.atan2(v.vy, v.vx) / (float) Math.PI * 0.5f + 0.5f;
        float brightness = Math.min(mag / field.getMaxMagnitude(), 1.0f);
        
        int color = java.awt.Color.HSBtoRGB(hue, 1.0f, brightness);
        g2d.setColor(new Color(color | 0xFF000000, false));  // Ensure opaque
        
        int px = (int) (x * cellSize);
        int py = (int) (y * cellSize);
        g2d.fillRect(px, py, (int) cellSize, (int) cellSize);
      }
    }
  }
  
  private void drawArrowHead(Graphics2D g2d, int x, int y, double angle, int size) {
    int[] xpts = new int[3];
    int[] ypts = new int[3];
    
    xpts[0] = x;
    ypts[0] = y;
    xpts[1] = (int) (x - size * Math.cos(angle - Math.PI / 6));
    ypts[1] = (int) (y - size * Math.sin(angle - Math.PI / 6));
    xpts[2] = (int) (x - size * Math.cos(angle + Math.PI / 6));
    ypts[2] = (int) (y - size * Math.sin(angle + Math.PI / 6));
    
    g2d.fillPolygon(xpts, ypts, 3);
  }
  
  public void setArrowMode(boolean arrows) {
    this.arrowMode = arrows;
  }
  
  public boolean isArrowMode() {
    return arrowMode;
  }
}
