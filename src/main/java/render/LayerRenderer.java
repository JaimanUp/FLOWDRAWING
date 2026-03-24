package render;

import java.awt.Graphics2D;
import java.awt.Color;

/**
 * LayerRenderer
 * Manages multi-layer rendering system.
 * 
 * Layers (in order):
 * 1. Background layer
 * 2. Vector field layer (optional visibility)
 * 3. Stroke layer (user drawing)
 * 4. Bot layer (bot traces)
 */
public class LayerRenderer {
  private int canvasWidth;
  private int canvasHeight;
  
  public LayerRenderer(int w, int h) {
    this.canvasWidth = w;
    this.canvasHeight = h;
  }
  
  public void render(Graphics2D g2d, int w, int h) {
    // Render background
    g2d.setColor(new Color(245, 245, 245));
    g2d.fillRect(0, 0, w, h);
    
    // Render vector field (placeholder)
    // Will add grid visualization in Phase 3
    
    // Render stroke layer (placeholder)
    // User-drawn strokes will be added in Phase 4
    
    // Render bot layer (placeholder)
    // Bot paths will be added in Phase 5
  }
}
