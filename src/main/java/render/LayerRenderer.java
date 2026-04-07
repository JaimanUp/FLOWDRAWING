package render;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import field.VectorField;

/**
 * LayerRenderer
 * Manages multi-layer rendering system with BufferedImages.
 * 
 * Layers (in order):
 * 1. Background layer
 * 2. Vector field layer (optional visibility)
 * 3. Stroke layer (user drawing)
 * 4. Bot layer (bot traces)
 */
public class LayerRenderer {
  private BufferedImage backgroundLayer;
  private BufferedImage vectorFieldLayer;
  private BufferedImage strokeLayer;
  private BufferedImage botLayer;
  private BufferedImage backgroundImage;  // User-loaded background image
  
  private int canvasWidth;   // Original canvas/vector field size (fixed)
  private int canvasHeight;  // Original canvas/vector field size (fixed)
  private int viewportWidth;  // Current viewport/window size (may change)
  private int viewportHeight; // Current viewport/window size (may change)
  private boolean showVectorField = false;
  private boolean showBackground = true;  // Toggle background visibility
  private boolean showBots = true;  // Toggle bot layer visibility
  private boolean showStrokes = true;  // Show strokes layer by default
  private FieldRenderer fieldRenderer;
  private VectorField vectorField;
  private CameraController cameraController;
  
  public LayerRenderer(int w, int h) {
    this.canvasWidth = w;    // Fixed canvas size
    this.canvasHeight = h;   // Fixed canvas size
    this.viewportWidth = w;  // Start with canvas size
    this.viewportHeight = h;
    
    // Initialize ALL layers at FIXED canvas size - NEVER CHANGES
    backgroundLayer = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
    vectorFieldLayer = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
    strokeLayer = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
    botLayer = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
    
    // Initialize background with default color
    initializeBackground();
    
    // Explicitly clear bot layer to ensure it's fully transparent
    clearBots();
    
    // Initialize Phase 2 field rendering - FIXED SIZE, NEVER CHANGES
    // cellSize = 20.0f gives us 100x100 grid (doubled from 10.0f)
    this.vectorField = new VectorField(canvasWidth, canvasHeight, 20.0f);
    this.fieldRenderer = new FieldRenderer(vectorField);
  }
  
  private void initializeBackground() {
    Graphics2D g2d = backgroundLayer.createGraphics();
    g2d.setColor(new Color(245, 245, 245));
    g2d.fillRect(0, 0, canvasWidth, canvasHeight);  // Fixed canvas size
    g2d.dispose();
  }
  
  /**
   * Resize viewport layers when window is resized.
   * The actual layer BufferedImages remain at FIXED canvas size.
   * Only viewport dimensions are tracked for rendering purposes.
   */
  public void resizeViewport(int newViewportWidth, int newViewportHeight) {
    // Update viewport dimensions only - do NOT resize the actual layers
    viewportWidth = newViewportWidth;
    viewportHeight = newViewportHeight;
    // Layers stay at their fixed canvas size
  }
  
  /**
   * DEPRECATED: Use resizeViewport() instead.
   * This old method was resizing the vector field, which we don't want.
   */
  public void resizeLayers(int newWidth, int newHeight) {
    // Just resize viewport, don't touch vector field
    resizeViewport(newWidth, newHeight);
  }
  
  public void render(Graphics2D g2d, int w, int h, CameraController camera) {
    // Store camera for later use
    this.cameraController = camera;
    
    // Render background (only if showBackground is true)
    if (showBackground) {
      if (backgroundImage != null) {
        g2d.drawImage(backgroundImage, 0, 0, null);
      } else {
        g2d.drawImage(backgroundLayer, 0, 0, null);
      }
    } else {
      // Draw default background when visibility is off
      g2d.drawImage(backgroundLayer, 0, 0, null);
    }
    
    // Render vector field layer (optional, Phase 3)
    if (showVectorField) {
      // Render field onto the vector field layer (canvas-sized, fixed)
      Graphics2D fieldG2d = vectorFieldLayer.createGraphics();
      fieldG2d.setComposite(java.awt.AlphaComposite.Clear);
      fieldG2d.fillRect(0, 0, canvasWidth, canvasHeight);  // Clear canvas-sized layer
      fieldG2d.setComposite(java.awt.AlphaComposite.SrcOver);
      fieldRenderer.render(fieldG2d, camera);  // Pass CameraController for zoom-aware rendering
      fieldG2d.dispose();
      
      g2d.drawImage(vectorFieldLayer, 0, 0, null);
    }
    
    // Render stroke layer at canvas size
    if (showStrokes) {
      g2d.drawImage(strokeLayer, 0, 0, null);
    }
    
    // Render bot layer at canvas size (only if showBots is true)
    if (showBots) {
      g2d.drawImage(botLayer, 0, 0, null);
    }
  }
  
  // Backward compatibility: render without camera
  public void render(Graphics2D g2d, int w, int h) {
    render(g2d, w, h, null);
  }
  
  // Accessors for drawing into layers
  public Graphics2D getBackgroundGraphics() {
    return backgroundLayer.createGraphics();
  }
  
  public Graphics2D getVectorFieldGraphics() {
    return vectorFieldLayer.createGraphics();
  }
  
  public Graphics2D getStrokeGraphics() {
    return strokeLayer.createGraphics();
  }
  
  public Graphics2D getBotGraphics() {
    return botLayer.createGraphics();
  }
  
  // Clear functions
  public void clearStrokes() {
    Graphics2D g2d = strokeLayer.createGraphics();
    g2d.setComposite(java.awt.AlphaComposite.Clear);
    g2d.fillRect(0, 0, canvasWidth, canvasHeight);  // Clear full canvas-sized layer
    g2d.dispose();
  }
  
  public void clearBots() {
    Graphics2D g2d = botLayer.createGraphics();
    g2d.setComposite(java.awt.AlphaComposite.Clear);
    g2d.fillRect(0, 0, canvasWidth, canvasHeight);  // Clear full canvas-sized layer
    g2d.dispose();
  }
  
  public void clearVectorField() {
    Graphics2D g2d = vectorFieldLayer.createGraphics();
    g2d.setComposite(java.awt.AlphaComposite.Clear);
    g2d.fillRect(0, 0, canvasWidth, canvasHeight);  // Canvas-sized layer
    g2d.dispose();
    
    // Phase 2: Also clear the underlying vector field data
    if (vectorField != null) {
      vectorField.clear();
    }
  }
  
  public void resetBackground() {
    Graphics2D g2d = backgroundLayer.createGraphics();
    g2d.setColor(new Color(245, 245, 245));
    g2d.fillRect(0, 0, canvasWidth, canvasHeight);
    g2d.dispose();
  }
  
  public void setShowVectorField(boolean show) {
    this.showVectorField = show;
  }
  
  public boolean isShowingVectorField() {
    return showVectorField;
  }
  
  public void setShowStrokes(boolean show) {
    this.showStrokes = show;
  }
  
  public boolean isShowingStrokes() {
    return showStrokes;
  }
  
  public int getWidth() {
    return canvasWidth;  // Canvas size for vector field reference
  }
  
  public int getHeight() {
    return canvasHeight;  // Canvas size for vector field reference
  }
  
  public int getViewportWidth() {
    return viewportWidth;
  }
  
  public int getViewportHeight() {
    return viewportHeight;
  }
  
  // Phase 2: Getters for field access
  public VectorField getVectorField() {
    return vectorField;
  }
  
  public FieldRenderer getFieldRenderer() {
    return fieldRenderer;
  }
  
  public void setVisualizationMode(String mode) {
    if (fieldRenderer != null) {
      fieldRenderer.setArrowMode("arrow".equals(mode));
    }
  }
  
  // Background image management
  public void setBackgroundImage(BufferedImage img) {
    if (img != null) {
      // Scale image to fit canvas while preserving aspect ratio
      BufferedImage scaled = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
      java.awt.Graphics2D g2d = scaled.createGraphics();
      
      // Fill with background color first
      g2d.setColor(new Color(245, 245, 245));
      g2d.fillRect(0, 0, canvasWidth, canvasHeight);
      
      // Calculate aspect-ratio-preserving dimensions
      float imgAspect = (float) img.getWidth() / img.getHeight();
      float canvasAspect = (float) canvasWidth / canvasHeight;
      int drawWidth, drawHeight, drawX, drawY;
      
      if (imgAspect > canvasAspect) {
        // Image is wider: fit to width
        drawWidth = canvasWidth;
        drawHeight = (int) (canvasWidth / imgAspect);
      } else {
        // Image is taller: fit to height
        drawHeight = canvasHeight;
        drawWidth = (int) (canvasHeight * imgAspect);
      }
      
      // Center the image
      drawX = (canvasWidth - drawWidth) / 2;
      drawY = (canvasHeight - drawHeight) / 2;
      
      g2d.drawImage(img, drawX, drawY, drawWidth, drawHeight, null);
      g2d.dispose();
      this.backgroundImage = scaled;
    } else {
      this.backgroundImage = null;
    }
  }
  
  public BufferedImage getBackgroundImage() {
    return backgroundImage;
  }
  
  public void clearBackgroundImage() {
    this.backgroundImage = null;
  }
  
  public void setShowBackground(boolean show) {
    this.showBackground = show;
  }
  
  public boolean isShowingBackground() {
    return showBackground;
  }
  
  public void setShowBots(boolean show) {
    this.showBots = show;
  }
  
  public boolean isShowingBots() {
    return showBots;
  }
}
