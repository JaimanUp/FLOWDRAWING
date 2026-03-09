package render;

import processing.core.PApplet;
import processing.core.PGraphics;

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
  private PGraphics backgroundLayer;
  private PGraphics vectorFieldLayer;
  private PGraphics strokeLayer;
  private PGraphics botLayer;
  
  private int canvasWidth;
  private int canvasHeight;
  private PApplet p;
  
  public LayerRenderer(int w, int h, PApplet parent) {
    this.canvasWidth = w;
    this.canvasHeight = h;
    this.p = parent;
    
    // Initialize layers
    backgroundLayer = p.createGraphics(w, h, PApplet.P2D);
    vectorFieldLayer = p.createGraphics(w, h, PApplet.P2D);
    strokeLayer = p.createGraphics(w, h, PApplet.P2D);
    botLayer = p.createGraphics(w, h, PApplet.P2D);
    
    // Initialize with default background
    initializeBackground();
  }
  
  private void initializeBackground() {
    backgroundLayer.beginDraw();
    backgroundLayer.background(245);
    backgroundLayer.endDraw();
  }
  
  public void renderBackground() {
    p.image(backgroundLayer, 0, 0);
  }
  
  public void renderVectorField() {
    // Vector field rendering (optional)
    // Placeholder: would draw arrows or flow map here
    /*
    p.image(vectorFieldLayer, 0, 0);
    */
  }
  
  public void renderStrokeLayer() {
    // User's brush strokes
    p.image(strokeLayer, 0, 0);
  }
  
  public void renderBotLayer() {
    // Bot paths
    p.image(botLayer, 0, 0);
  }
  
  // Accessors for drawing into layers
  public PGraphics getBackgroundLayer() {
    return backgroundLayer;
  }
  
  public PGraphics getVectorFieldLayer() {
    return vectorFieldLayer;
  }
  
  public PGraphics getStrokeLayer() {
    return strokeLayer;
  }
  
  public PGraphics getBotLayer() {
    return botLayer;
  }
  
  // Clear functions
  public void clearStrokes() {
    strokeLayer.beginDraw();
    strokeLayer.clear();
    strokeLayer.endDraw();
  }
  
  public void clearBots() {
    botLayer.beginDraw();
    botLayer.clear();
    botLayer.endDraw();
  }
}
