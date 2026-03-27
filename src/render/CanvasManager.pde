/**
 * CanvasManager
 * Central coordinator for canvas operations.
 * Integrates camera, layers, and future field/bot/brush systems.
 */
class CanvasManager {
  private int canvasWidth;
  private int canvasHeight;
  private CameraController cameraController;
  private LayerRenderer layerRenderer;
  
  CanvasManager(int w, int h, CameraController cam, LayerRenderer renderer) {
    this.canvasWidth = w;
    this.canvasHeight = h;
    this.cameraController = cam;
    this.layerRenderer = renderer;
  }
  
  void update() {
    // Main update loop placeholder
    // Will integrate field simulation, bot simulation, etc.
  }
  
  // Accessors
  int getWidth() {
    return canvasWidth;
  }
  
  int getHeight() {
    return canvasHeight;
  }
  
  CameraController getCameraController() {
    return cameraController;
  }
  
  LayerRenderer getLayerRenderer() {
    return layerRenderer;
  }
  
  // Canvas operations (stubs for future phases)
  void clearAll() {
    layerRenderer.clearStrokes();
    layerRenderer.clearBots();
  }
  
  void initializeField() {
    // Phase 2: Initialize vector field
  }
  
  void startSimulation() {
    // Phase 5: Start bot simulation
  }
  
  void stopSimulation() {
    // Phase 5: Stop bot simulation
  }
}
