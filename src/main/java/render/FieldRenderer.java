package render;

import field.VectorField;
import field.Vector2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;

/**
 * FieldRenderer
 * Handles visualization of the vector field with zoom-aware resolution.
 * 
 * Two modes: Arrow mode and Flow map mode (Phase 3)
 * 
 * Zoom-aware rendering:
 * - LOW zoom (<0.5x): Skip vectors for clarity
 * - MEDIUM zoom (0.5-2x): Render all vectors
 * - HIGH zoom (>2x): Interpolate for sub-grid detail
 */
public class FieldRenderer {
  private VectorField field;
  private boolean arrowMode = true;  // true = arrows, false = flow map
  private float arrowScale = 5.0f;  // Scale factor for arrow display
  
  // Zoom tiers (in world space screen pixels)
  private static final float LOW_ZOOM_THRESHOLD = 0.5f;
  private static final float HIGH_ZOOM_THRESHOLD = 2.0f;
  
  // Base subdivisions for HIGH zoom (will be multiplied by zoom factor)
  private static final int BASE_HIGH_ZOOM_SUBDIVISIONS = 2;  // 2x2 at 2.0x zoom
  
  public FieldRenderer(VectorField field) {
    this.field = field;
  }
  
  /**
   * Calculate dynamic subdivision factor based on zoom level.
   * At 2x zoom: 2x2 subdivisions
   * At 4x zoom: 3x3 subdivisions
   * At 8x zoom: 4x4 subdivisions
   * Formula: subdivisions = BASE + floor(log2(zoom / HIGH_ZOOM_THRESHOLD))
   */
  private int getSubdivisionFactor(float zoom) {
    if (zoom <= HIGH_ZOOM_THRESHOLD) {
      return BASE_HIGH_ZOOM_SUBDIVISIONS;
    }
    // Calculate: how many times higher than HIGH_ZOOM_THRESHOLD is zoom?
    float zoomMultiple = zoom / HIGH_ZOOM_THRESHOLD;
    int additionalLevels = (int) (Math.log(zoomMultiple) / Math.log(2.0d));
    return BASE_HIGH_ZOOM_SUBDIVISIONS + additionalLevels;
  }
  
  public void render(Graphics2D g2d, CameraController camera) {
    if (field == null) return;
    
    // Get zoom level (default to 1.0 if no camera)
    float zoom = (camera != null) ? camera.getZoom() : 1.0f;
    
    if (arrowMode) {
      renderArrowMode(g2d, zoom);
    } else {
      renderFlowMapMode(g2d, zoom);
    }
  }
  
  /**
   * Determine zoom tier based on zoom level.
   */
  private ZoomTier getZoomTier(float zoom) {
    if (zoom > HIGH_ZOOM_THRESHOLD) {
      return ZoomTier.HIGH;
    } else {
      return ZoomTier.MEDIUM;  // LOW zoom uses MEDIUM rendering
    }
  }
  
  /**
   * Render as arrows showing direction and magnitude with zoom-aware resolution.
   */
  private void renderArrowMode(Graphics2D g2d, float zoom) {
    Vector2D[][] vectors = field.getVectors();
    float cellSize = field.getCellSize();
    ZoomTier tier = getZoomTier(zoom);
    
    // Enable antialiasing for smooth vector graphics
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                         java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, 
                         java.awt.RenderingHints.VALUE_STROKE_PURE);
    
    switch (tier) {
      case MEDIUM:
        renderArrowModeMediumZoom(g2d, vectors, cellSize);
        break;
      case HIGH:
        renderArrowModeHighZoom(g2d, vectors, cellSize, zoom);
        break;
    }
  }
  
  /**
   * MEDIUM zoom: Render all vectors at normal resolution.
   */
  private void renderArrowModeMediumZoom(Graphics2D g2d, Vector2D[][] vectors, float cellSize) {
    for (int x = 0; x < vectors.length; x++) {
      for (int y = 0; y < vectors[x].length; y++) {
        Vector2D v = vectors[x][y];
        float mag = v.magnitude();
        
        if (mag > 0.1f) {
          float worldX = x * cellSize + cellSize / 2;
          float worldY = y * cellSize + cellSize / 2;
          
          drawVectorAsTriangle(g2d, worldX, worldY, v.vx, v.vy, cellSize, mag);
        }
      }
    }
  }
  
  /**
   * HIGH zoom: Subdivide cells and interpolate vectors for higher resolution.
   * Uses bilinear interpolation to estimate vectors at sub-grid positions.
   * Subdivisions increase with zoom level for progressive detail.
   */
  private void renderArrowModeHighZoom(Graphics2D g2d, Vector2D[][] vectors, float cellSize, float zoom) {
    int subdivisions = getSubdivisionFactor(zoom);
    float subdivisionSize = cellSize / subdivisions;
    
    for (int gx = 0; gx < vectors.length; gx++) {
      for (int gy = 0; gy < vectors[gx].length; gy++) {
        // For each grid cell, render subdivisions
        for (int sx = 0; sx < subdivisions; sx++) {
          for (int sy = 0; sy < subdivisions; sy++) {
            // Position within subdivision
            float localX = (sx + 0.5f) / subdivisions;
            float localY = (sy + 0.5f) / subdivisions;
            
            // Interpolate vector at this position
            Vector2D interpolated = interpolateVector(vectors, gx, gy, localX, localY);
            float mag = interpolated.magnitude();
            
            if (mag > 0.1f) {
              float worldX = gx * cellSize + sx * subdivisionSize + subdivisionSize / 2;
              float worldY = gy * cellSize + sy * subdivisionSize + subdivisionSize / 2;
              
              drawVectorAsTriangle(g2d, worldX, worldY, interpolated.vx, interpolated.vy, subdivisionSize, mag);
            }
          }
        }
      }
    }
  }
  
  /**
   * Bilinear interpolation of vector at position (lx, ly) within cell (gx, gy).
   * lx, ly are normalized coordinates 0-1 within the cell.
   */
  private Vector2D interpolateVector(Vector2D[][] vectors, int gx, int gy, float lx, float ly) {
    // Clamp grid coordinates
    int x0 = Math.max(0, Math.min(gx, vectors.length - 1));
    int y0 = Math.max(0, Math.min(gy, vectors[0].length - 1));
    int x1 = Math.min(x0 + 1, vectors.length - 1);
    int y1 = Math.min(y0 + 1, vectors[0].length - 1);
    
    // Get corner vectors
    Vector2D v00 = vectors[x0][y0];
    Vector2D v10 = vectors[x1][y0];
    Vector2D v01 = vectors[x0][y1];
    Vector2D v11 = vectors[x1][y1];
    
    // Bilinear interpolation weights
    float wx0 = 1.0f - lx;
    float wx1 = lx;
    float wy0 = 1.0f - ly;
    float wy1 = ly;
    
    // Interpolate components
    float vx = wx0 * wy0 * v00.vx + wx1 * wy0 * v10.vx +
               wx0 * wy1 * v01.vx + wx1 * wy1 * v11.vx;
    float vy = wx0 * wy0 * v00.vy + wx1 * wy0 * v10.vy +
               wx0 * wy1 * v01.vy + wx1 * wy1 * v11.vy;
    
    return new Vector2D(vx, vy);
  }
  
  /**
   * Render as flow map (color-coded by direction and magnitude).
   */
  private void renderFlowMapMode(Graphics2D g2d, float zoom) {
    Vector2D[][] vectors = field.getVectors();
    float cellSize = field.getCellSize();
    ZoomTier tier = getZoomTier(zoom);
    
    switch (tier) {
      case MEDIUM:
        renderFlowMapMediumZoom(g2d, vectors, cellSize);
        break;
      case HIGH:
        renderFlowMapHighZoom(g2d, vectors, cellSize, zoom);
        break;
    }
  }
  
  /**
   * LOW zoom flow map: Skip cells for clarity.
   */
  /**
   * MEDIUM zoom flow map: All cells at normal resolution.
   */
  private void renderFlowMapMediumZoom(Graphics2D g2d, Vector2D[][] vectors, float cellSize) {
    int width = (int) (vectors.length * cellSize);
    int height = (int) (vectors[0].length * cellSize);
    for (int px = 0; px < width; px++) {
      for (int py = 0; py < height; py++) {
        // Map pixel to field space
        float fx = px / cellSize;
        float fy = py / cellSize;
        int gx = (int) Math.floor(fx);
        int gy = (int) Math.floor(fy);
        float lx = fx - gx;
        float ly = fy - gy;
        Vector2D interpolated = interpolateVector(vectors, gx, gy, lx, ly);
        float mag = interpolated.magnitude();
        float hue = (float) Math.atan2(interpolated.vy, interpolated.vx) / (float) Math.PI * 0.5f + 0.5f;
        float brightness = Math.min(mag / field.getMaxMagnitude(), 1.0f);
        int color = java.awt.Color.HSBtoRGB(hue, 1.0f, brightness);
        g2d.setColor(new Color(color | 0xFF000000, false));
        g2d.fillRect(px, py, 1, 1);
      }
    }
  }
  
  /**
   * HIGH zoom flow map: Subdivide cells with interpolated colors.
   * Subdivisions increase with zoom level for progressive detail.
   */
  private void renderFlowMapHighZoom(Graphics2D g2d, Vector2D[][] vectors, float cellSize, float zoom) {
    int width = (int) (vectors.length * cellSize);
    int height = (int) (vectors[0].length * cellSize);
    for (int px = 0; px < width; px++) {
      for (int py = 0; py < height; py++) {
        float fx = px / cellSize;
        float fy = py / cellSize;
        int gx = (int) Math.floor(fx);
        int gy = (int) Math.floor(fy);
        float lx = fx - gx;
        float ly = fy - gy;
        Vector2D interpolated = interpolateVector(vectors, gx, gy, lx, ly);
        float mag = interpolated.magnitude();
        float hue = (float) Math.atan2(interpolated.vy, interpolated.vx) / (float) Math.PI * 0.5f + 0.5f;
        float brightness = Math.min(mag / field.getMaxMagnitude(), 1.0f);
        int color = java.awt.Color.HSBtoRGB(hue, 1.0f, brightness);
        g2d.setColor(new Color(color | 0xFF000000, false));
        g2d.fillRect(px, py, 1, 1);
      }
    }
  }
  
  /**
   * Draw a vector as an arrow with vector-based graphics for better quality.
   * Uses antialiasing from parent Graphics2D context.
   * 
   * @param g2d Graphics2D context with antialiasing enabled
   * @param x World X coordinate of vector origin (anchor point at base center)
   * @param y World Y coordinate of vector origin (anchor point at base center)
   * @param vx Vector X component
   * @param vy Vector Y component
   * @param cellSize Size of grid cell (for scaling)
   * @param magnitude Magnitude of vector for visualization
   */
  private void drawVectorAsTriangle(Graphics2D g2d, float x, float y, float vx, float vy, float cellSize, float magnitude) {
    if (magnitude < 0.01f) return; // No representation for very small vectors
    
    // Normalize direction
    float nx = vx / magnitude;
    float ny = vy / magnitude;
    
    // Use normalized magnitude for triangle height
    float maxFieldMagnitude = field.getMaxMagnitude();
    float normalizedMag = Math.min(magnitude / maxFieldMagnitude, 1.0f);
    float maxHeight = 0.35f * cellSize;
    float triangleHeight = normalizedMag * maxHeight; // Scale factor for visualization
    
    // Isosceles triangle: two equal longer sides, base = 1/3 of longer sides
    float longerSide = 3.0f * triangleHeight;
    float baseSide = longerSide / 3.0f; // = triangleHeight
    
    // Calculate actual perpendicular height of triangle
    // For isosceles triangle: h = sqrt(longerSide² - (baseSide/2)²)
    float actualHeight = (float) Math.sqrt(longerSide * longerSide - (baseSide / 2.0f) * (baseSide / 2.0f));
    
    // Triangle apex points in the vector direction
    float apexX = x + nx * actualHeight;
    float apexY = y + ny * actualHeight;
    
    // Base perpendicular to direction (rotated 90 degrees)
    // Perpendicular = (-ny, nx)
    float perpX = -ny;
    float perpY = nx;
    
    // Base corners (perpendicular to direction, centered at anchor)
    float base1X = x + perpX * (baseSide / 2.0f);
    float base1Y = y + perpY * (baseSide / 2.0f);
    float base2X = x - perpX * (baseSide / 2.0f);
    float base2Y = y - perpY * (baseSide / 2.0f);
    
    // Set vector color (#B7CBE9)
    g2d.setColor(new Color(0xB7CBE9));
    
    // Draw filled triangle: apex and two base corners
    int[] xPoints = {(int) apexX, (int) base1X, (int) base2X};
    int[] yPoints = {(int) apexY, (int) base1Y, (int) base2Y};
    g2d.fillPolygon(xPoints, yPoints, 3);
  }
  
  public void setArrowMode(boolean arrows) {
    this.arrowMode = arrows;
  }
  
  public boolean isArrowMode() {
    return arrowMode;
  }
  
  /**
   * Zoom tier enumeration for adaptive rendering strategies.
   */
  private enum ZoomTier {
    LOW,      // Zoom < 0.5x: Skip vectors for clarity
    MEDIUM,   // Zoom 0.5-2x: Render all vectors normally
    HIGH      // Zoom > 2x: Subdivide and interpolate for detail
  }
}
