package render;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;

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
  
  private int canvasWidth;
  private int canvasHeight;
  private boolean showVectorField = false;
  
  public LayerRenderer(int w, int h) {
    this.canvasWidth = w;
    this.canvasHeight = h;
    
    // Initialize all layers as BufferedImages
    backgroundLayer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    vectorFieldLayer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    strokeLayer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    botLayer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    
    // Initialize background with default color
    initializeBackground();
  }
  
  private void initializeBackground() {
    Graphics2D g2d = backgroundLayer.createGraphics();
    g2d.setColor(new Color(245, 245, 245));
    g2d.fillRect(0, 0, canvasWidth, canvasHeight);
    g2d.dispose();
  }
  
  public void resizeLayers(int newWidth, int newHeight) {
    // Resize all layers
    BufferedImage newBg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
    BufferedImage newVF = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    BufferedImage newStroke = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    BufferedImage newBot = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    
    // Copy old content (or reinitialize)
    Graphics2D g2d = newBg.createGraphics();
    g2d.setColor(new Color(245, 245, 245));
    g2d.fillRect(0, 0, newWidth, newHeight);
    g2d.dispose();
    
    backgroundLayer = newBg;
    vectorFieldLayer = newVF;
    strokeLayer = newStroke;
    botLayer = newBot;
    
    canvasWidth = newWidth;
    canvasHeight = newHeight;
  }
  
  public void render(Graphics2D g2d, int w, int h) {
    // Render background layer
    g2d.drawImage(backgroundLayer, 0, 0, null);
    
    // Render vector field layer (optional)
    if (showVectorField) {
      g2d.drawImage(vectorFieldLayer, 0, 0, null);
    }
    
    // Render stroke layer
    g2d.drawImage(strokeLayer, 0, 0, null);
    
    // Render bot layer
    g2d.drawImage(botLayer, 0, 0, null);
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
    g2d.fillRect(0, 0, canvasWidth, canvasHeight);
    g2d.dispose();
  }
  
  public void clearBots() {
    Graphics2D g2d = botLayer.createGraphics();
    g2d.setComposite(java.awt.AlphaComposite.Clear);
    g2d.fillRect(0, 0, canvasWidth, canvasHeight);
    g2d.dispose();
  }
  
  public void clearVectorField() {
    Graphics2D g2d = vectorFieldLayer.createGraphics();
    g2d.setComposite(java.awt.AlphaComposite.Clear);
    g2d.fillRect(0, 0, canvasWidth, canvasHeight);
    g2d.dispose();
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
  
  public int getWidth() {
    return canvasWidth;
  }
  
  public int getHeight() {
    return canvasHeight;
  }
}
