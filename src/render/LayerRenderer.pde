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
class LayerRenderer {
  private PGraphics backgroundLayer;
  private PGraphics vectorFieldLayer;
  private PGraphics strokeLayer;
  private PGraphics botLayer;
  
  private int canvasWidth;
  private int canvasHeight;
  
  LayerRenderer(int w, int h) {
    this.canvasWidth = w;
    this.canvasHeight = h;
    
    // Initialize layers
    backgroundLayer = createGraphics(w, h, P2D);
    vectorFieldLayer = createGraphics(w, h, P2D);
    strokeLayer = createGraphics(w, h, P2D);
    botLayer = createGraphics(w, h, P2D);
    
    // Initialize with default background
    initializeBackground();
  }
  
  private void initializeBackground() {
    backgroundLayer.beginDraw();
    backgroundLayer.background(245);
    backgroundLayer.endDraw();
  }
  
  void renderBackground() {
    image(backgroundLayer, 0, 0);
  }
  
  void renderVectorField() {
    // Vector field rendering (optional)
    // Placeholder: would draw arrows or flow map here
    // For now, subtle grid visualization
    /*
    image(vectorFieldLayer, 0, 0);
    */
  }
  
  void renderStrokeLayer() {
    // User's brush strokes
    image(strokeLayer, 0, 0);
  }
  
  void renderBotLayer() {
    // Bot paths
    image(botLayer, 0, 0);
  }
  
  // Accessors for drawing into layers
  PGraphics getBackgroundLayer() {
    return backgroundLayer;
  }
  
  PGraphics getVectorFieldLayer() {
    return vectorFieldLayer;
  }
  
  PGraphics getStrokeLayer() {
    return strokeLayer;
  }
  
  PGraphics getBotLayer() {
    return botLayer;
  }
  
  // Clear functions
  void clearStrokes() {
    strokeLayer.beginDraw();
    strokeLayer.clear();
    strokeLayer.endDraw();
  }
  
  void clearBots() {
    botLayer.beginDraw();
    botLayer.clear();
    botLayer.endDraw();
  }
}
