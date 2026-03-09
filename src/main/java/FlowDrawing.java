import processing.core.PApplet;

/**
 * Flow-Guided Generative Drawing Engine
 * Main application entry point
 * 
 * Phase 1: Core Canvas System
 * - Resizable canvas with P2D renderer
 * - Camera controls (zoom, pan)
 * - Multi-layer rendering (background, vector field, strokes, bots)
 */
public class FlowDrawing extends PApplet {
  
  // Global state
  private CanvasManager canvasManager;
  private CameraController cameraController;
  private LayerRenderer layerRenderer;
  
  public static void main(String[] args) {
    PApplet.main("FlowDrawing");
  }
  
  @Override
  public void settings() {
    size(800, 600, P2D);
  }
  
  @Override
  public void setup() {
    // Initialize managers
    cameraController = new CameraController(width, height);
    layerRenderer = new LayerRenderer(width, height, this);
    canvasManager = new CanvasManager(width, height, cameraController, layerRenderer);
    
    // Frame rate
    frameRate(60);
    
    // Initial hint for smooth rendering
    hint(DISABLE_OPTIMIZED_STROKE);
  }
  
  @Override
  public void draw() {
    background(255);
    
    // Apply camera transform
    cameraController.apply(this);
    
    // Render layers
    layerRenderer.renderBackground();
    layerRenderer.renderVectorField();
    layerRenderer.renderStrokeLayer();
    layerRenderer.renderBotLayer();
    
    // Reset camera for UI
    resetMatrix();
    cameraController.unapply();
    
    // Display frame rate (debugging)
    fill(0);
    textSize(12);
    text("FPS: " + nf(frameRate, 0, 1), 10, 20);
  }
  
  @Override
  public void mouseDragged() {
    // Pan on mouse drag
    cameraController.pan(pmouseX - mouseX, pmouseY - mouseY);
  }
  
  @Override
  public void mouseWheel(processing.event.MouseEvent event) {
    // Zoom on mouse wheel
    float e = event.getCount();
    cameraController.zoom(e > 0 ? 1.1f : 0.9f);
  }
  
  @Override
  public void keyPressed() {
    // Reset view on 'r'
    if (key == 'r' || key == 'R') {
      cameraController.reset();
    }
  }
}
