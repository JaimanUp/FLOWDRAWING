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
  private ui.ToolPanel toolPanel;
  private JLabel statusBar;
  
  // Fixed canvas dimensions (larger than typical window for painting flexibility)
  private static final int CANVAS_WIDTH = 2000;
  private static final int CANVAS_HEIGHT = 2000;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new FlowDrawing());
  }

  public FlowDrawing() {
    setTitle("FlowDrawing");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(true);

    // Create canvas panel
    canvasPanel = new CanvasPanel(CANVAS_WIDTH, CANVAS_HEIGHT);

    // ── Menu bar ───────────────────────────────────────────────────────
    setJMenuBar(new ui.AppMenuBar(new ui.AppMenuBar.MenuListener() {
      @Override public void onNewProject() {
        int confirm = JOptionPane.showConfirmDialog(
          FlowDrawing.this,
          "Start a new project? All unsaved changes will be lost.",
          "New Project", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
          canvasPanel.clearVectorField();
          canvasPanel.clearStrokes();
          canvasPanel.clearBots();
          canvasPanel.resetCanvasToFit();
          setStatus("New project");
        }
      }
      @Override public void onExit() { System.exit(0); }
      @Override public void onResetView() { canvasPanel.resetCanvasToFit(); setStatus("View reset"); }
      @Override public void onZoomIn()   { canvasPanel.zoomBy(1.2f); }
      @Override public void onZoomOut()  { canvasPanel.zoomBy(0.8f); }
      @Override public void onFitCanvas() { canvasPanel.resetCanvasToFit(); setStatus("Fit canvas"); }
      @Override public void onToggleToolPanel() {
        toolPanel.setVisible(!toolPanel.isVisible());
      }
    }));

    // ── Tool panel ─────────────────────────────────────────────────────
    toolPanel = new ui.ToolPanel(new ui.ToolPanel.ToolListener() {

      // Background
      @Override public void onBackgroundToggle(boolean show)         { setStatus(show ? "Background ON" : "Background OFF"); }
      @Override public void onBackgroundTransparencyChanged(float a)  { setStatus("[planned] Background transparency"); }
      @Override public void onBackgroundColorPicker()                 { setStatus("[planned] Background colour picker"); }
      @Override public void onLoadBackgroundImage()                   { setStatus("[planned] Load background image"); }
      @Override public void onClearBackgroundImage()                  { setStatus("[planned] Clear background image"); }

      // Vector Field
      @Override public void onVectorFieldToggle(boolean show) {
        canvasPanel.setShowVectorField(show);
        setStatus(show ? "Vector field ON" : "Vector field OFF");
      }
      @Override public void onVectorFieldTransparencyChanged(float a) { setStatus("[planned] Field transparency"); }
      @Override public void onVisualizationModeChanged(String mode) {
        canvasPanel.setVisualizationMode(mode.equals("heatmap") ? "flowmap" : mode);
        setStatus(mode.equals("heatmap") ? "Heatmap mode" : "Arrow mode");
      }
      @Override public void onResetVectorField() {
        canvasPanel.clearVectorField();
        setStatus("Vector field reset");
      }
      @Override public void onRandomVectorField() {
        canvasPanel.randomizeVectorField();
        setStatus("Vector field randomized");
      }

      // Sketch
      @Override public void onSketchToggle(boolean show) {
        canvasPanel.setShowStrokes(show);
        setStatus(show ? "Strokes ON" : "Strokes OFF");
      }
      @Override public void onSketchTransparencyChanged(float a)      { setStatus("[planned] Sketch transparency"); }
      @Override public void onBrushModeChanged(boolean isBrush) {
        brush.Brush.BrushMode mode = isBrush ? brush.Brush.BrushMode.BRUSH : brush.Brush.BrushMode.ERASER;
        canvasPanel.getBrush().setMode(mode);
        setStatus(isBrush ? "Brush mode" : "Eraser mode");
      }
      @Override public void onBrushSizeChanged(float size)            { canvasPanel.getBrush().setSize(size); }
      @Override public void onBrushHardnessChanged(float hardness)    { canvasPanel.getBrush().setHardness(hardness); }
      @Override public void onBrushStrengthChanged(float strength)    { canvasPanel.getBrush().setStrength(strength); }
      @Override public void onClearSketch() {
        canvasPanel.clearStrokes();
        setStatus("Strokes cleared");
      }

      // Botfield
      @Override public void onBotfieldToggle(boolean show)            { setStatus(show ? "Botfield ON" : "Botfield OFF"); }
      @Override public void onBotfieldTransparencyChanged(float a)    { setStatus("[planned] Botfield transparency"); }
      @Override public void onBotLifeChanged(float life)              { canvasPanel.getBotEngine().setBotMaxLife(life); }
      @Override public void onBotRadarChanged(float radar)            { canvasPanel.getBotEngine().setBotRadar(radar); }
      @Override public void onBotDriftChanged(float drift)            { canvasPanel.getBotEngine().setBotDrift(drift); }
      @Override public void onBotSpeedChanged(float speed)            { canvasPanel.getBotEngine().setBotSpeed(speed); }
      @Override public void onSpawnBot() {
        canvasPanel.getBotEngine().spawnRandomBot();
        setStatus("Bot spawned");
      }
      @Override public void onAutoSpawnToggle(boolean enabled) {
        canvasPanel.getBotEngine().setAutoSpawnEnabled(enabled);
        setStatus(enabled ? "Auto-spawn ON" : "Auto-spawn OFF");
      }
      @Override public void onBotSpawnRateChanged(int rate)           { canvasPanel.getBotEngine().setSpawnRate(rate); }
      @Override public void onClearBots() {
        canvasPanel.clearBots();
        setStatus("Bots cleared");
      }
    });

    // ── Status bar ─────────────────────────────────────────────────────
    statusBar = new JLabel("  Ready");
    statusBar.setFont(new Font("Arial", Font.PLAIN, 11));
    statusBar.setForeground(Color.DARK_GRAY);
    statusBar.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
      BorderFactory.createEmptyBorder(2, 6, 2, 6)
    ));
    statusBar.setPreferredSize(new Dimension(0, 20));

    // ── Layout ─────────────────────────────────────────────────────────
    setLayout(new BorderLayout());
    add(toolPanel,  BorderLayout.WEST);
    add(canvasPanel, BorderLayout.CENTER);
    add(statusBar,   BorderLayout.SOUTH);

    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setVisible(true);
    
    // Fit canvas to screen on startup and request focus
    canvasPanel.resetCanvasToFit();
    canvasPanel.requestFocusInWindow();
  }

  private void setStatus(String msg) {
    if (statusBar != null) statusBar.setText("  " + msg);
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
    
    // Phase 5: Bot simulation
    private bot.BotEngine botEngine;
    // Stroke polyline tracking
    private float lastStrokeX = Float.NaN;
    private float lastStrokeY = Float.NaN;
    private float lastModulatedSize = 0;  // Track previous brush size for interpolation

    private String visualizationMode = "arrow";
    
    // Keyboard state tracking
    private volatile boolean zoomModeActive = false;  // True when spacebar is pressed (for zoom/pan)
    private volatile boolean[] arrowKeysPressed = new boolean[4];  // [up, down, left, right]

    public CanvasPanel(int width, int height) {
      this.canvasWidth = width;
      this.canvasHeight = height;
      setPreferredSize(new Dimension(width, height));
      setOpaque(false);  // Make transparent so super.paintComponent doesn't paint white background
      setFocusable(true);
      setFocusTraversalKeysEnabled(false);  // Allow all key events

      // Initialize managers
      cameraController = new render.CameraController(width, height);
      layerRenderer = new render.LayerRenderer(width, height);
      layerRenderer.setShowVectorField(true);  // Show vector field by default
      canvasManager = new CanvasManager(width, height, cameraController, layerRenderer);
      
      // Phase 4: Initialize brush system
      brush = new brush.Brush();
      brushEngine = new brush.BrushEngine(brush, canvasManager.getVectorField());
      
      // Phase 5: Initialize bot engine
      botEngine = new bot.BotEngine(canvasManager.getVectorField());

      // Request focus when component is clicked
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          requestFocusInWindow();
        }
      });

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
              e.getX(), e.getY(), getWidth(), getHeight()
            );
            brushEngine.startStroke(worldCoords.x, worldCoords.y);
            // Reset stroke tracking for polyline
            lastStrokeX = Float.NaN;
            lastStrokeY = Float.NaN;
            lastModulatedSize = 0;
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
              e.getX(), e.getY(), getWidth(), getHeight()
            );
            brushEngine.paintAt(worldCoords.x, worldCoords.y);
            // Draw visual stroke feedback
            drawBrushStroke(worldCoords.x, worldCoords.y);
            repaint();
          }
          lastMouseX = e.getX();
          lastMouseY = e.getY();
        }
      });

      addMouseWheelListener(e -> {
        // Zoom centered on mouse position: down = zoom out, up = zoom in
        float factor = e.getWheelRotation() > 0 ? 0.9f : 1.1f;
        cameraController.zoomAtMouse(factor, e.getX(), e.getY(), getWidth(), getHeight());
        repaint();
      });

      addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          int keyCode = e.getKeyCode();
          char keyChar = e.getKeyChar();
          
          // Reset view on 'r'
          if (keyChar == 'r' || keyChar == 'R') {
            cameraController.reset();
            repaint();
          }
          
          // Spacebar activates zoom/pan mode
          if (keyCode == KeyEvent.VK_SPACE) {
            zoomModeActive = true;
            e.consume();  // Consume to prevent other handlers
          }
          
          // Zoom in: spacebar + '+'
          if (zoomModeActive && (keyChar == '+' || keyChar == '=')) {
            cameraController.zoomAtMouse(1.2f, getWidth() / 2, getHeight() / 2, getWidth(), getHeight());
            repaint();
            e.consume();
          }
          
          // Zoom out: spacebar + '-'
          if (zoomModeActive && keyChar == '-') {
            cameraController.zoomAtMouse(0.8f, getWidth() / 2, getHeight() / 2, getWidth(), getHeight());
            repaint();
            e.consume();
          }
          
          // Arrow key panning (when spacebar is held)
          if (zoomModeActive) {
            int panDistance = 15;  // Pixels to pan per key press
            switch (keyCode) {
              case KeyEvent.VK_UP:
                arrowKeysPressed[0] = true;
                cameraController.pan(0, panDistance);
                repaint();
                e.consume();
                break;
              case KeyEvent.VK_DOWN:
                arrowKeysPressed[1] = true;
                cameraController.pan(0, -panDistance);
                repaint();
                e.consume();
                break;
              case KeyEvent.VK_LEFT:
                arrowKeysPressed[2] = true;
                cameraController.pan(panDistance, 0);
                repaint();
                e.consume();
                break;
              case KeyEvent.VK_RIGHT:
                arrowKeysPressed[3] = true;
                cameraController.pan(-panDistance, 0);
                repaint();
                e.consume();
                break;
            }
          }
        }
        
        @Override
        public void keyReleased(KeyEvent e) {
          int keyCode = e.getKeyCode();
          
          // Exit zoom/pan mode when spacebar is released
          if (keyCode == KeyEvent.VK_SPACE) {
            zoomModeActive = false;
            arrowKeysPressed[0] = false;
            arrowKeysPressed[1] = false;
            arrowKeysPressed[2] = false;
            arrowKeysPressed[3] = false;
            e.consume();
          }
          
          // Track arrow key release
          if (zoomModeActive) {
            switch (keyCode) {
              case KeyEvent.VK_UP:
                arrowKeysPressed[0] = false;
                e.consume();
                break;
              case KeyEvent.VK_DOWN:
                arrowKeysPressed[1] = false;
                e.consume();
                break;
              case KeyEvent.VK_LEFT:
                arrowKeysPressed[2] = false;
                e.consume();
                break;
              case KeyEvent.VK_RIGHT:
                arrowKeysPressed[3] = false;
                e.consume();
                break;
            }
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
        // Note: Canvas size (1200x800) remains FIXED
        // Only update viewport dimensions for rendering (window may be resized)
        int currentWidth = getWidth();
        int currentHeight = getHeight();
        if (currentWidth > 0 && currentHeight > 0) {
          layerRenderer.resizeViewport(currentWidth, currentHeight);
        }
        
        // Phase 5: Update bots
        botEngine.update();
        
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
    
    /**
     * Reset canvas to best fit current window size.
     * Logic:
     * 1. Determine reference length: if vertical (height > width), use height; if horizontal, use width
     * 2. Zoom so one canvas dimension equals reference length
     * 3. Center canvas both horizontally and vertically on the panel
     */
    public void resetCanvasToFit() {
      int panelWidth = getWidth();
      int panelHeight = getHeight();
      
      // Determine reference length based on screen orientation
      // Vertical: use full height
      // Horizontal: use width (which already excludes sidebar since we're in BorderLayout.CENTER)
      float refLength;
      if (panelHeight >= panelWidth) {
        // Vertical or square screen: use height as reference
        refLength = panelHeight;
      } else {
        // Horizontal screen: use width as reference
        refLength = panelWidth;
      }
      
      // Calculate zoom so canvas dimension equals reference length
      float canvasDim = Math.max(CANVAS_WIDTH, CANVAS_HEIGHT);
      float fitZoom = (refLength / canvasDim) * 0.95f;  // 95% margin
      
      // Calculate centers
      float panelCenterX = panelWidth / 2.0f;
      float panelCenterY = panelHeight / 2.0f;
      
      float canvasCenterX = CANVAS_WIDTH / 2.0f;
      float canvasCenterY = CANVAS_HEIGHT / 2.0f;
      
      // Pan to center the canvas
      float panX = panelCenterX - (canvasCenterX * fitZoom);
      float panY = panelCenterY - (canvasCenterY * fitZoom);
      
      // Apply camera settings
      cameraController.reset();
      cameraController.setZoom(fitZoom);
      cameraController.setPan(panX, panY);
      
      // Clear temporary data but preserve vector field
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
      botEngine.clearBots();
      layerRenderer.clearBots();
      repaint();
    }
    
    public void setShowVectorField(boolean show) {
      layerRenderer.setShowVectorField(show);
      repaint();
    }
    
    public void setShowStrokes(boolean show) {
      layerRenderer.setShowStrokes(show);
      repaint();
    }
    
    public void setVisualizationMode(String mode) {
      this.visualizationMode = mode;
      layerRenderer.setVisualizationMode(mode);
      repaint();
    }
    
    // Phase 4: Brush accessors
    public brush.Brush getBrush() {
      return brush;
    }
    
    public brush.BrushEngine getBrushEngine() {
      return brushEngine;
    }
    
    // Phase 5: Bot accessor
    public bot.BotEngine getBotEngine() {
      return botEngine;
    }
    
    // Zoom by a relative factor (used by menu bar actions)
    public void zoomBy(float factor) {
      cameraController.zoomAtMouse(factor, getWidth() / 2, getHeight() / 2, getWidth(), getHeight());
      repaint();
    }
    
    // Randomize the vector field with noise-like values
    public void randomizeVectorField() {
      field.VectorField vf = canvasManager.getVectorField();
      int cols = vf.getCols();
      int rows = vf.getRows();
      vf.clear();
      for (int x = 0; x < cols; x++) {
        for (int y = 0; y < rows; y++) {
          double angle = Math.random() * 2 * Math.PI;
          float strength = 0.5f + (float)(Math.random() * 2.0f);
          vf.addForce(x, y, (float)(Math.cos(angle) * strength), (float)(Math.sin(angle) * strength));
        }
      }
      repaint();
    }
    
    /**
     * Draw all bot paths and bots to the bot layer.
     * Called each frame to update bot visualization.
     * Traces are accumulated (not cleared each frame) so they persist after bots die.
     */
    private void drawBots() {
      java.awt.Graphics2D g2d = layerRenderer.getBotGraphics();
      
      // Do NOT clear the bot layer - accumulate traces for persistence
      // g2d.setComposite(...);
      // g2d.fillRect(...);
      
      g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
      
      // Create a synchronized copy of the bots list to avoid ConcurrentModificationException
      java.util.ArrayList<bot.Bot> botsCopy;
      synchronized (botEngine.getBots()) {
        botsCopy = new java.util.ArrayList<>(botEngine.getBots());
      }
      
      // Draw all bot paths
      for (bot.Bot bot : botsCopy) {
        java.util.ArrayList<field.Vector2D> path = bot.getPath();
        
        if (path.size() > 1) {
          // Draw path as connected line segments
          float life = bot.getLifeNormalized();  // 0-1, where 1 is just spawned
          
          // Color based on life: bright when alive, fade to darker
          int r = (int) (100 + 155 * life);
          int g_val = (int) (100 + 155 * life);
          int b = (int) (200 + 55 * life);
          int alpha = (int) (180 + 75 * life);  // Brighten path as bot ages
          
          g2d.setColor(new Color(r, g_val, b, alpha));
          g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
          
          // Draw polyline through all path points (with local copy for thread safety)
          synchronized (path) {
            for (int i = 0; i < path.size() - 1; i++) {
              field.Vector2D p1 = path.get(i);
              if (i + 1 < path.size()) {  // Safety check
                field.Vector2D p2 = path.get(i + 1);
                if (p1 != null && p2 != null) {
                  g2d.drawLine((int) p1.vx, (int) p1.vy, (int) p2.vx, (int) p2.vy);
                }
              }
            }
          }
        }
        
        // Draw bot as small circle at current position
        field.Vector2D pos = bot.getPosition();
        float life = bot.getLifeNormalized();
        int radius = 3;
        int r = (int) (150 + 105 * life);
        int g_val = (int) (150 + 105 * life);
        int b = (int) (255);
        int alpha = (int) (200 + 55 * life);
        
        g2d.setColor(new Color(r, g_val, b, alpha));
        g2d.fillOval((int) pos.vx - radius, (int) pos.vy - radius, radius * 2, radius * 2);
        
        // Draw direction indicator (small line from center)
        field.Vector2D vel = bot.getVelocity();
        float velMag = vel.magnitude();
        if (velMag > 0.1f) {
          g2d.setColor(new Color(r, g_val, b, 150));
          float dirScale = 5.0f;
          g2d.drawLine(
            (int) pos.vx, (int) pos.vy,
            (int) (pos.vx + vel.vx / velMag * dirScale), 
            (int) (pos.vy + vel.vy / velMag * dirScale)
          );
        }
      }
      
      g2d.dispose();
    }

    /**
     * Draw brush stroke feedback to the stroke layer.
     * Draws a variable-width polyline that matches the brush circle blueprint.
     * Stroke width changes dynamically based on distance-based size modulation
     * between control points (faster movement = narrower stroke).
     */
    private void drawBrushStroke(float worldX, float worldY) {
      java.awt.Graphics2D g2d = layerRenderer.getStrokeGraphics();
      g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
      
      // Get base brush size
      float baseBrushSize = brush.getSize();
      
      // If this is the first point in the stroke, just record it
      if (Float.isNaN(lastStrokeX)) {
        lastStrokeX = worldX;
        lastStrokeY = worldY;
        lastModulatedSize = baseBrushSize;  // Start with full size
        g2d.dispose();
        return;
      }
      
      // Calculate distance between last point and current point
      float dx = worldX - lastStrokeX;
      float dy = worldY - lastStrokeY;
      float distance = (float) Math.sqrt(dx * dx + dy * dy);
      
      // Modulate brush size based on distance (5% range variation)
      // Max distance (50 units) gives minimum size (95% of base)
      // 0 distance gives maximum size (100% of base)
      float maxDistanceForModulation = 50.0f;
      float distanceFactor = Math.min(distance / maxDistanceForModulation, 1.0f);
      float sizeModulator = 1.0f - (distanceFactor * 0.05f);  // Range from 1.0 to 0.95 (5% variation)
      float currentModulatedSize = baseBrushSize * sizeModulator;
      
      // Map brush strength to alpha (transparency)
      // Strength range: 0.1 to 5.0 -> Alpha range: 1% to 40%
      float brushStrength = brush.getStrength();
      float minStrength = 0.1f;
      float maxStrength = 5.0f;
      float strengthFactor = (brushStrength - minStrength) / (maxStrength - minStrength);  // Normalize to 0-1
      strengthFactor = Math.max(0.0f, Math.min(1.0f, strengthFactor));  // Clamp to 0-1
      
      int minAlpha = (int) (0.01f * 255);  // 1% opacity
      int maxAlpha = (int) (0.40f * 255);  // 40% opacity
      int baseAlpha = (int) (minAlpha + strengthFactor * (maxAlpha - minAlpha));
      baseAlpha = Math.max(minAlpha, Math.min(maxAlpha, baseAlpha));  // Clamp to 1%-40% range
      
      // Apply distance-based transparency modulation: faster = more transparent (5% variation)
      // Faster movement (larger distance) reduces alpha by up to 5%
      float transparencyReduction = distanceFactor * 0.05f;  // Up to 5% reduction
      int alpha = (int) (baseAlpha * (1.0f - transparencyReduction));
      alpha = Math.max(0, Math.min(255, alpha));  // Ensure stays within bounds
      
      // Stroke color with strength-based transparency (light blue)
      g2d.setColor(new Color(100, 150, 255, alpha));
      
      // Draw polyline with varying width by subdividing the segment
      int numSubdivisions = Math.max(3, (int) distance / 2);  // More subdivisions for longer segments
      for (int i = 0; i < numSubdivisions; i++) {
        // Interpolate position along segment
        float t = (float) (i + 1) / numSubdivisions;
        float interpX = lastStrokeX + dx * t;
        float interpY = lastStrokeY + dy * t;
        
        // Interpolate stroke width between last and current size
        float interpWidth = lastModulatedSize + (currentModulatedSize - lastModulatedSize) * t;
        interpWidth = Math.max(1.0f, interpWidth);  // Minimum width of 1.0
        
        // Get the previous interpolated position and width
        float prevT = (float) i / numSubdivisions;
        float prevX = lastStrokeX + dx * prevT;
        float prevY = lastStrokeY + dy * prevT;
        float prevWidth = lastModulatedSize + (currentModulatedSize - lastModulatedSize) * prevT;
        prevWidth = Math.max(1.0f, prevWidth);
        
        // Use average width for this segment for smoother transitions
        float avgWidth = (prevWidth + interpWidth) / 2.0f;
        g2d.setStroke(new BasicStroke(avgWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Draw segment
        g2d.drawLine((int) prevX, (int) prevY, (int) interpX, (int) interpY);
      }
      
      // Update last position and size for next segment
      lastStrokeX = worldX;
      lastStrokeY = worldY;
      lastModulatedSize = currentModulatedSize;
      
      g2d.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      // Draw bots into the bot layer (in world coordinates before transform)
      drawBots();

      // Save original transform
      java.awt.geom.AffineTransform originalTransform = g2d.getTransform();

      // Apply camera transform for zooming and panning
      cameraController.apply(g2d, getWidth(), getHeight());

      // Render all layers (including bot layer) with camera transform applied
      layerRenderer.render(g2d, getWidth(), getHeight(), cameraController);

      // Reset transform and display FPS
      g2d.setTransform(originalTransform);
      g2d.setColor(Color.BLACK);
      g2d.setFont(new Font("Arial", Font.PLAIN, 12));
      long now = System.currentTimeMillis();
      if (lastFrameTime > 0) {
        fps = 1000.0 / (now - lastFrameTime);
      }
      lastFrameTime = now;
      g2d.drawString(String.format("FPS: %.1f | Bots: %d", fps, botEngine.getBotCount()), 10, 20);
    }
  }
}
