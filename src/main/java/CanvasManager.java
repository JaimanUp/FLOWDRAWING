import render.CameraController;
import render.LayerRenderer;
import render.FieldRenderer;
import field.VectorField;
import config.Config;

/**
 * CanvasManager
 * Central coordinator for canvas operations.
 * Integrates camera, layers, field, and future bot/brush systems.
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
    // Phase 2: Field normalization on demand
    VectorField field = getVectorField();
    if (field != null) {
      field.normalizeFieldIfNeeded();
    }
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
  
  public VectorField getVectorField() {
    // Get field from LayerRenderer (source of truth)
    return layerRenderer.getVectorField();
  }
  
  public FieldRenderer getFieldRenderer() {
    return layerRenderer.getFieldRenderer();
  }
  
  // Canvas operations (stubs for future phases)
  public void clearAll() {
    // Will clear both stroke and bot layers in Phase 4+
  }
  
  public void resetCanvas() {
    cameraController.reset();
    layerRenderer.resetBackground();
    VectorField field = getVectorField();
    if (field != null) {
      field.clear();
    }
  }
  
  public void clearVectorField() {
    VectorField field = getVectorField();
    if (field != null) {
      field.clear();
    }
  }
  
  public void initializeField() {
    // Phase 2: Vector field already initialized in LayerRenderer
    VectorField field = getVectorField();
    if (field != null) {
      field.clear();
    }
  }
  
  public void startSimulation() {
    // Phase 5: Start bot simulation
  }
  
  public void stopSimulation() {
    // Phase 5: Stop bot simulation
  }
}
