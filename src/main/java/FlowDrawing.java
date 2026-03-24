import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
  private static final int WINDOW_WIDTH = 800;
  private static final int WINDOW_HEIGHT = 600;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new FlowDrawing());
  }

  public FlowDrawing() {
    setTitle("Flow-Guided Generative Drawing Engine");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    setLocationRelativeTo(null);
    setResizable(true);

    // Create canvas panel
    canvasPanel = new CanvasPanel(WINDOW_WIDTH, WINDOW_HEIGHT);
    add(canvasPanel);

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

    public CanvasPanel(int width, int height) {
      setPreferredSize(new Dimension(width, height));
      setBackground(Color.WHITE);
      setFocusable(true);

      // Initialize managers
      cameraController = new render.CameraController(width, height);
      layerRenderer = new render.LayerRenderer(width, height);
      canvasManager = new CanvasManager(width, height, cameraController, layerRenderer);

      // Add mouse listeners
      addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          lastMouseX = e.getX();
          lastMouseY = e.getY();
        }
      });

      addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
          int dx = lastMouseX - e.getX();
          int dy = lastMouseY - e.getY();
          cameraController.pan(dx, dy);
          lastMouseX = e.getX();
          lastMouseY = e.getY();
          repaint();
        }
      });

      addMouseWheelListener(e -> {
        // Reverse: down = zoom out, up = zoom in
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
        repaint();
        try {
          Thread.sleep(16); // ~60 FPS
        } catch (InterruptedException e) {
          break;
        }
      }
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
