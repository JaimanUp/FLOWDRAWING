import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import field.VectorField;

/**
 * Flow-Guided Generative Drawing Engine
 * Main application entry point
 * 
 * Phase 1: Core Canvas System
 * - Resizable canvas
 * - Camera controls (zoom, pan)
 * - Multi-layer rendering (background, vector field, strokes, bots)
 */
public class FlowDrawing extends JFrame {
  private CanvasPanel canvasPanel;
  private ui.UIPanel uiPanel;
  private static final int WINDOW_WIDTH = 1000;
  private static final int WINDOW_HEIGHT = 700;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new FlowDrawing());
  }

  public FlowDrawing() {
    setTitle("Flow-Guided Generative Drawing Engine - Phase 4 | MiddleClick+Drag=Pan | Scroll=Zoom | R=Reset | LeftClick=Paint");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    setLocationRelativeTo(null);
    setResizable(true);

    // Create canvas panel
    canvasPanel = new CanvasPanel(WINDOW_WIDTH - 20, WINDOW_HEIGHT - 100);
    
    // Create UI panel with listener
    uiPanel = new ui.UIPanel(new ui.UIPanel.UIListener() {
      @Override
      public void onResetCanvas() {
        canvasPanel.resetCanvas();
        uiPanel.setStatus("Canvas reset");
      }

      @Override
      public void onClearStrokes() {
        canvasPanel.clearStrokes();
        uiPanel.setStatus("Strokes cleared");
      }

      @Override
      public void onClearBots() {
        canvasPanel.clearBots();
        uiPanel.setStatus("Bots cleared");
      }

      @Override
      public void onVectorFieldToggle(boolean show) {
        canvasPanel.setShowVectorField(show);
        uiPanel.setStatus(show ? "Vector field ON" : "Vector field OFF");
      }
      
      @Override
      public void onClearVectorField() {
        canvasPanel.clearVectorField();
        uiPanel.setStatus("Vector field cleared");
      }
      
      @Override
      public void onBrushSizeChanged(float size) {
        canvasPanel.getBrush().setSize(size);
      }
      
      @Override
      public void onBrushHardnessChanged(float hardness) {
        canvasPanel.getBrush().setHardness(hardness);
      }
      
      @Override
      public void onBrushStrengthChanged(float strength) {
        canvasPanel.getBrush().setStrength(strength);
      }
    });

    // Layout
    setLayout(new BorderLayout());
    add(uiPanel, BorderLayout.NORTH);
    add(canvasPanel, BorderLayout.CENTER);

    setVisible(true);
  }

  // Inner class for canvas rendering
  private static class CanvasPanel extends JPanel implements Runnable {
    private render.CameraController cameraController;
    private render.LayerRenderer layerRenderer;
    private CanvasManager canvasManager;
    private Thread renderThread;
    private volatile boolean running = true;
    private long lastFrameTime;
    private double fps = 0;
    private int lastMouseX, lastMouseY;
    private int canvasWidth;
    private int canvasHeight;
    private volatile boolean middleMousePressed = false;
    
    // Phase 4: Brush system
    private brush.Brush brush;
    private brush.BrushEngine brushEngine;
    private volatile boolean leftMousePressed = false;

    public CanvasPanel(int width, int height) {
      this.canvasWidth = width;
      this.canvasHeight = height;
      setPreferredSize(new Dimension(width, height));
      setBackground(Color.WHITE);
      setFocusable(true);

      // Initialize managers
      cameraController = new render.CameraController(width, height);
      layerRenderer = new render.LayerRenderer(width, height);
      canvasManager = new CanvasManager(width, height, cameraController, layerRenderer);
      
      // Phase 4: Initialize brush system
      brush = new brush.Brush();
      brushEngine = new brush.BrushEngine(brush, canvasManager.getVectorField());

      // Add mouse listeners
      addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          lastMouseX = e.getX();
          lastMouseY = e.getY();
          
          // Detect middle mouse button (scroll button) for panning
          if (e.getButton() == MouseEvent.BUTTON2) {
            middleMousePressed = true;
          }
          // Detect left mouse button for painting
          else if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = true;
            // Start brush stroke with world coordinates
            java.awt.geom.Point2D.Float worldCoords = cameraController.screenToWorld(
              e.getX(), e.getY(), canvasWidth, canvasHeight
            );
            brushEngine.startStroke(worldCoords.x, worldCoords.y);
          }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          if (e.getButton() == MouseEvent.BUTTON2) {
            middleMousePressed = false;
          }
          // End brush stroke on left mouse release
          else if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = false;
            brushEngine.endStroke();
          }
        }
      });

      addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
          // Pan when middle mouse is pressed
          if (middleMousePressed) {
            int dx = lastMouseX - e.getX();
            int dy = lastMouseY - e.getY();
            cameraController.pan(dx, dy);
            repaint();
          }
          // Paint when left mouse is pressed
          else if (leftMousePressed) {
            // Convert screen coordinates to world coordinates for brush
            java.awt.geom.Point2D.Float worldCoords = cameraController.screenToWorld(
              e.getX(), e.getY(), canvasWidth, canvasHeight
            );
            brushEngine.paintAt(worldCoords.x, worldCoords.y);
            repaint();
          }
          lastMouseX = e.getX();
          lastMouseY = e.getY();
        }
      });

      addMouseWheelListener(e -> {
        // Zoom: down = zoom out, up = zoom in
        float factor = e.getWheelRotation() > 0 ? 0.9f : 1.1f;
        cameraController.zoom(factor);
        repaint();
      });

      addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getKeyChar() == 'r' || e.getKeyChar() == 'R') {
            cameraController.reset();
            repaint();
          }
        }
      });

      // Start render thread
      renderThread = new Thread(this);
      renderThread.setDaemon(true);
      renderThread.start();
    }

    @Override
    public void run() {
      while (running) {
        // Handle resizing (skip if component not yet laid out)
        int currentWidth = getWidth();
        int currentHeight = getHeight();
        if (currentWidth > 0 && currentHeight > 0 && 
            (currentWidth != canvasWidth || currentHeight != canvasHeight)) {
          canvasWidth = currentWidth;
          canvasHeight = currentHeight;
          layerRenderer.resizeLayers(canvasWidth, canvasHeight);
          // Update brush engine with new field reference
          VectorField newField = layerRenderer.getVectorField();
          if (newField != null && brushEngine != null) {
            brushEngine = new brush.BrushEngine(brush, newField);
          }
        }
        repaint();
        try {
          Thread.sleep(16); // ~60 FPS
        } catch (InterruptedException e) {
          break;
        }
      }
    }
    
    public void resetCanvas() {
      // Reset only camera transforms, NOT the vector field data
      cameraController.reset();
      layerRenderer.resetBackground();
      layerRenderer.clearStrokes();
      layerRenderer.clearBots();
      repaint();
    }
    
    public void clearVectorField() {
      // Explicitly clear only the vector field
      layerRenderer.clearVectorField();
      repaint();
    }
    
    public void clearStrokes() {
      layerRenderer.clearStrokes();
      repaint();
    }
    
    public void clearBots() {
      layerRenderer.clearBots();
      repaint();
    }
    
    public void setShowVectorField(boolean show) {
      layerRenderer.setShowVectorField(show);
      repaint();
    }
    
    // Phase 4: Brush accessors
    public brush.Brush getBrush() {
      return brush;
    }
    
    public brush.BrushEngine getBrushEngine() {
      return brushEngine;
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Save original transform
      java.awt.geom.AffineTransform originalTransform = g2d.getTransform();

      // Apply camera transform
      cameraController.apply(g2d, getWidth(), getHeight());

      // Render layers
      layerRenderer.render(g2d, getWidth(), getHeight());

      // Reset transform and display FPS
      g2d.setTransform(originalTransform);
      g2d.setColor(Color.BLACK);
      g2d.setFont(new Font("Arial", Font.PLAIN, 12));
      long now = System.currentTimeMillis();
      if (lastFrameTime > 0) {
        fps = 1000.0 / (now - lastFrameTime);
      }
      lastFrameTime = now;
      g2d.drawString(String.format("FPS: %.1f", fps), 10, 20);
    }
  }
}
