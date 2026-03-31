package render;

import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

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
  
  /**
   * Set pan values directly.
   * Used for centering operations.
   */
  public void setPan(float x, float y) {
    panX = x;
    panY = y;
  }
  
  public void zoom(float factor) {
    zoomLevel *= factor;
    // Clamp zoom to reasonable bounds
    zoomLevel = Math.max(0.1f, Math.min(10.0f, zoomLevel));
  }
  
  /**
   * Set zoom level directly.
   * Used for fit-to-window operations.
   */
  public void setZoom(float level) {
    zoomLevel = Math.max(0.1f, Math.min(10.0f, level));
  }
  
  /**
   * Zoom centered on a specific mouse position in screen space.
   * Keeps the exact world position under the mouse cursor fixed.
   * 
   * Correct formula for zoom-to-point:
   * If a world point maps to screen position before zoom, after zoom
   * we adjust pan so the SAME world point maps to the SAME screen position.
   * 
   * The screen transform is: screen = (world - canvasCenter) * zoom + canvasCenter + pan
   * 
   * Rearranging for pan after zoom:
   * panX_new = panX_old + (screenX - canvasCenter.x - panX_old) * (1 - zoomNew/zoomOld)
   * 
   * @param factor Zoom factor (1.1 = zoom in, 0.9 = zoom out)
   * @param screenX Mouse X in screen coordinates (relative to canvas)
   * @param screenY Mouse Y in screen coordinates (relative to canvas)
   * @param canvasW Canvas width in pixels
   * @param canvasH Canvas height in pixels
   */
  public void zoomAtMouse(float factor, int screenX, int screenY, int canvasW, int canvasH) {
    // Store old zoom
    float oldZoom = zoomLevel;
    
    // Apply zoom
    zoomLevel *= factor;
    zoomLevel = Math.max(0.1f, Math.min(10.0f, zoomLevel));
    float newZoom = zoomLevel;
    
    // Canvas center
    float centerX = canvasW / 2.0f;
    float centerY = canvasH / 2.0f;
    
    // Zoom ratio
    float zoomRatio = newZoom / oldZoom;
    
    // Adjust pan to keep screen point fixed
    // Formula: pan_new = pan_old + (screen - center - pan_old) * (1 - zoomRatio)
    panX = panX + (screenX - centerX - panX) * (1 - zoomRatio);
    panY = panY + (screenY - centerY - panY) * (1 - zoomRatio);
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
  
  /**
   * Convert screen space coordinates to world space coordinates.
   * Used for brush painting with camera transforms (zoom/pan).
   */
  public java.awt.geom.Point2D.Float screenToWorld(float screenX, float screenY, int w, int h) {
    try {
      // Build the inverse transform
      AffineTransform transform = new AffineTransform();
      transform.translate(panX, panY);
      transform.translate(w / 2.0f, h / 2.0f);
      transform.scale(zoomLevel, zoomLevel);
      transform.translate(-w / 2.0f, -h / 2.0f);
      
      // Invert it
      AffineTransform inverse = transform.createInverse();
      
      // Transform screen coordinates to world coordinates
      java.awt.geom.Point2D.Float screenPoint = new java.awt.geom.Point2D.Float(screenX, screenY);
      java.awt.geom.Point2D.Float worldPoint = new java.awt.geom.Point2D.Float();
      inverse.transform(screenPoint, worldPoint);
      
      return worldPoint;
    } catch (java.awt.geom.NoninvertibleTransformException e) {
      // Fallback: return screen coordinates if transform is singular
      return new java.awt.geom.Point2D.Float(screenX, screenY);
    }
  }
}
