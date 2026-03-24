import render.CameraController;
import render.LayerRenderer;

/**
 * CanvasManager
 * Central coordinator for canvas operations.
 * Integrates camera, layers, and future field/bot/brush systems.
 */
public class CanvasManager {
  private int canvasWidth;
  private int canvasHeight;
  private CameraController cameraController;
  private LayerRenderer layerRenderer;
  
  public CanvasManager(int w, int h, CameraController cam, LayerRenderer renderer) {
    this.canvasWidth = w;
    this.canvasHeight = h;
    this.cameraController = cam;
    this.layerRenderer = renderer;
  }
  
  public void update() {
    // Main update loop placeholder
    // Will integrate field simulation, bot simulation, etc.
  }
  
  // Accessors
  public int getWidth() {
    return canvasWidth;
  }
  
  public int getHeight() {
    return canvasHeight;
  }
  
  public CameraController getCameraController() {
    return cameraController;
  }
  
  public LayerRenderer getLayerRenderer() {
    return layerRenderer;
  }
  
  // Canvas operations (stubs for future phases)
  public void clearAll() {
    // Will clear both stroke and bot layers in Phase 4+
  }
  
  public void initializeField() {
    // Phase 2: Initialize vector field
  }
  
  public void startSimulation() {
    // Phase 5: Start bot simulation
  }
  
  public void stopSimulation() {
    // Phase 5: Stop bot simulation
  }
}
