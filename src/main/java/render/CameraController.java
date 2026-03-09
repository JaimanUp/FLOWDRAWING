package render;

import processing.core.PApplet;

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
    panX += dx;
    panY += dy;
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
  
  public void apply(PApplet p) {
    // Apply transformations centered on canvas origin
    p.translate(panX, panY);
    p.translate(canvasWidth / 2.0f, canvasHeight / 2.0f);
    p.scale(zoomLevel);
    p.translate(-canvasWidth / 2.0f, -canvasHeight / 2.0f);
  }
  
  public void unapply() {
    // No additional work needed; resetMatrix() called in main draw
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
