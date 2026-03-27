/**
 * CameraController
 * Manages zoom and pan transformations.
 * 
 * Important: zoom and pan affect visualization only, NOT simulation coordinates.
 */
class CameraController {
  private float zoomLevel = 1.0;
  private float panX = 0;
  private float panY = 0;
  
  private int canvasWidth;
  private int canvasHeight;
  
  CameraController(int w, int h) {
    this.canvasWidth = w;
    this.canvasHeight = h;
  }
  
  void pan(float dx, float dy) {
    panX += dx;
    panY += dy;
  }
  
  void zoom(float factor) {
    zoomLevel *= factor;
    // Clamp zoom to reasonable bounds
    zoomLevel = constrain(zoomLevel, 0.1, 10.0);
  }
  
  void reset() {
    zoomLevel = 1.0;
    panX = 0;
    panY = 0;
  }
  
  void apply() {
    // Apply transformations centered on canvas origin
    translate(panX, panY);
    translate(canvasWidth / 2, canvasHeight / 2);
    scale(zoomLevel);
    translate(-canvasWidth / 2, -canvasHeight / 2);
  }
  
  void unapply() {
    // No additional work needed; resetMatrix() called in main draw
  }
  
  float getZoom() {
    return zoomLevel;
  }
  
  PVector getPan() {
    return new PVector(panX, panY);
  }
}
