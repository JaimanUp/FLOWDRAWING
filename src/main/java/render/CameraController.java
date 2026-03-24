package render;

import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;

/**
 * CameraController
 * Manages zoom and pan transformations.
 * 
 * Important: zoom and pan affect visualization only, NOT simulation coordinates.
 */
public class CameraController {
  private float zoomLevel = 1.0f;
  private float panX = 0;
  private float panY = 0;
  
  private int canvasWidth;
  private int canvasHeight;
  
  public CameraController(int w, int h) {
    this.canvasWidth = w;
    this.canvasHeight = h;
  }
  
  public void pan(float dx, float dy) {
    // Reverse: negate the pan direction
    panX -= dx;
    panY -= dy;
  }
  
  public void zoom(float factor) {
    zoomLevel *= factor;
    // Clamp zoom to reasonable bounds
    zoomLevel = Math.max(0.1f, Math.min(10.0f, zoomLevel));
  }
  
  public void reset() {
    zoomLevel = 1.0f;
    panX = 0;
    panY = 0;
  }
  
  public void apply(Graphics2D g2d, int w, int h) {
    // Apply transformations centered on canvas origin
    g2d.translate(panX, panY);
    g2d.translate(w / 2.0f, h / 2.0f);
    g2d.scale(zoomLevel, zoomLevel);
    g2d.translate(-w / 2.0f, -h / 2.0f);
  }
  
  public float getZoom() {
    return zoomLevel;
  }
  
  public float getPanX() {
    return panX;
  }
  
  public float getPanY() {
    return panY;
  }
}
