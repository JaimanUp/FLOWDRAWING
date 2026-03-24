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
  private VectorField vectorField;
  private FieldRenderer fieldRenderer;
  
  public CanvasManager(int w, int h, CameraController cam, LayerRenderer renderer) {
    this.canvasWidth = w;
    this.canvasHeight = h;
    this.cameraController = cam;
    this.layerRenderer = renderer;
    
    // Initialize Phase 2: Vector Field Engine
    this.vectorField = new VectorField(w, h, Config.CELL_SIZE);
    this.fieldRenderer = new FieldRenderer(vectorField);
  }
  
  public void update() {
    // Main update loop placeholder
    // Phase 2: Field normalization on demand
    vectorField.normalizeFieldIfNeeded();
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
    return vectorField;
  }
  
  public FieldRenderer getFieldRenderer() {
    return fieldRenderer;
  }
  
  // Canvas operations (stubs for future phases)
  public void clearAll() {
    // Will clear both stroke and bot layers in Phase 4+
  }
  
  public void resetCanvas() {
    cameraController.reset();
    layerRenderer.resetBackground();
    vectorField.clear();
  }
  
  public void clearVectorField() {
    vectorField.clear();
  }
  
  public void initializeField() {
    // Phase 2: Vector field already initialized in constructor
    vectorField.clear();
  }
  
  public void startSimulation() {
    // Phase 5: Start bot simulation
  }
  
  public void stopSimulation() {
    // Phase 5: Stop bot simulation
  }
}
