package ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * ToolPanel
 * Left-side vertical tabbed tool panel.
 * Four tabs: Background | Vector Field | Sketch | Botfield
 * Replaces the old horizontal UIPanel strip.
 */

/**
 * Custom ButtonUI that prevents the default L&F from overriding colors when pressed/armed
 */
class CustomButtonUI extends BasicButtonUI {
  @Override
  public void paint(Graphics g, JComponent c) {
    // Don't let the L&F apply armed/pressed state colors
    // Just render with the button's currently set background color
    JButton b = (JButton) c;
    
    // Paint background
    if (b.isOpaque()) {
      g.setColor(b.getBackground());
      g.fillRect(0, 0, b.getWidth(), b.getHeight());
    }
    
    // Paint the button's content (text, icon, etc.)
    super.paint(g, c);
  }
}

public class ToolPanel extends JPanel {

  private static final int PANEL_WIDTH = 230;
  private static final int PANEL_MIN_WIDTH = 200;  // Minimum width for responsive design
  private static final int SECTION_PADDING = 8;    // Padding inside sections
  private static final int CONTROL_SPACING = 4;    // Spacing between controls
  private static final int BUTTON_HEIGHT = 44;     // WCAG minimum touch target height
  private static final float CONTROL_WIDTH_RATIO = 0.92f;  // Controls use 92% of available width

  // ── Color Palette ──────────────────────────────────────────────────────
  private static final Color COLOR_PANEL_BG = new Color(46, 46, 46);
  // Main background for panels and tabs. Unified dark background.

  // Removed COLOR_SECTION_BG - now unified with PANEL_BG for simplicity

  // Removed COLOR_SECTION_BORDER - spacing replaces borders for cleaner design

  private static final Color COLOR_TEXT_PRIMARY = new Color(232, 232, 232);
  // Primary text color. Off-white for all labels, buttons, and headers.

  private static final Color COLOR_TEXT_SECONDARY = new Color(176, 176, 176);
  // Secondary text color for muted labels and hints. Rarely used.

  private static final Color COLOR_TEXT_TAB = new Color(232, 232, 232);
  // Tab text color. Off-white for tab labels.

  private static final Color COLOR_ACCENT_GREEN = new Color(0, 255, 0);
  // Primary accent color. Neon green for active controls, interactive elements, and radar aesthetic.

  private static final Color COLOR_BUTTON_DEFAULT = new Color(0, 180, 0);
  // Default button color. Muted green for rest state.

  private static final Color COLOR_BUTTON_HOVER = new Color(0, 255, 0);
  // Button hover glow. Bright neon green when hovering over buttons.

  private static final Color COLOR_ACCENT_CYAN = new Color(0, 255, 209);
  // Secondary accent color. Cyan for hover states, secondary highlights, and alternative selections.

  // ── Listener interface ────────────────────────────────────────────────
  public interface ToolListener {
    // Background
    void onBackgroundToggle(boolean show);
    void onBackgroundTransparencyChanged(float alpha);
    void onBackgroundColorPicker();
    void onLoadBackgroundImage();
    void onClearBackgroundImage();

    // Vector Field
    void onVectorFieldToggle(boolean show);
    void onVectorFieldTransparencyChanged(float alpha);
    void onVisualizationModeChanged(String mode);   // "arrow" | "heatmap"
    void onResetVectorField();
    void onRandomVectorField();

    // Sketch
    void onSketchToggle(boolean show);
    void onSketchTransparencyChanged(float alpha);
    void onBrushModeChanged(boolean isBrush);      // true = brush, false = eraser
    void onBrushSizeChanged(float size);
    void onBrushHardnessChanged(float hardness);
    void onBrushStrengthChanged(float strength);
    void onClearSketch();

    // Botfield
    void onBotfieldToggle(boolean show);
    void onBotfieldTransparencyChanged(float alpha);
    void onBotLifeChanged(float life);
    void onBotRadarChanged(float radar);
    void onBotDriftChanged(float drift);
    void onBotSpeedChanged(float speed);
    void onSpawnBot();
    void onAutoSpawnToggle(boolean enabled);
    void onBotSpawnRateChanged(int rate);
    void onSimulationStart();
    void onSimulationPause();
    void onSimulationReset();
    void onClearBots();
  }

  private final ToolListener listener;
  private JLabel brushSizeLabel, brushHardnessLabel, brushStrengthLabel;
  private JLabel botLifeLabel, botRadarLabel, botDriftLabel, botSpeedLabel, botSpawnRateLabel;

  public ToolPanel(ToolListener listener) {
    this.listener = listener;
    setLayout(new BorderLayout(0, 0));  // No gaps between components
    // Set preferred and minimum sizes for responsive layout
    // Preferred: 230px, Minimum: 200px, Maximum: flexible based on parent
    setPreferredSize(new Dimension(PANEL_WIDTH, 0));
    setMinimumSize(new Dimension(PANEL_MIN_WIDTH, 0));
    setBackground(COLOR_PANEL_BG);
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));  // No padding around edges

    // Configure UI defaults for all components once (performance optimization)
    configureUIDefaults();

    JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
    tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);  // Wrap to multiple rows if needed, don't use scroll buttons
    tabs.setBackground(COLOR_PANEL_BG);
    tabs.setForeground(COLOR_TEXT_TAB);
    tabs.setBorder(BorderFactory.createEmptyBorder());  // Remove border to maximize space
    
    // Ensure tabs fill available space
    tabs.setMinimumSize(new Dimension(PANEL_MIN_WIDTH, 0));
    tabs.setPreferredSize(new Dimension(PANEL_WIDTH, 0));
    tabs.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));  // Cap height but allow width expansion
    
    // Apply custom rounded tab UI (do NOT call updateUI() after setUI())
    tabs.setUI(new RoundedTabbedPaneUI());

    tabs.addTab("BG",     wrapScroll(buildBackgroundTab()));
    tabs.addTab("Field",  wrapScroll(buildVectorFieldTab()));
    tabs.addTab("Sketch", wrapScroll(buildSketchTab()));
    tabs.addTab("Bots",   wrapScroll(buildBotfieldTab()));

    // Full tab tooltips
    tabs.setToolTipTextAt(0, "Background");
    tabs.setToolTipTextAt(1, "Vector Field");
    tabs.setToolTipTextAt(2, "Sketch");
    tabs.setToolTipTextAt(3, "Botfield");

    add(tabs, BorderLayout.CENTER);
    
    // Register keyboard shortcuts for tab navigation (Ctrl+1-4)
    registerKeyboardShortcuts(tabs);
  }

  // ── Scroll wrapper ────────────────────────────────────────────────────
  private JScrollPane wrapScroll(JPanel content) {
    JScrollPane sp = new JScrollPane(content,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setBorder(BorderFactory.createEmptyBorder());
    sp.getVerticalScrollBar().setUnitIncrement(12);
    return sp;
  }
  // ── Configure UI defaults for all components (performance optimization) ──
  private void configureUIDefaults() {
    // All UI styling is explicitly handled by custom UI classes:
    // - RoundedTabbedPaneUI for tabs (overrides all TabbedPane rendering)
    // - CustomSliderUI for sliders (overrides all Slider rendering)
    // - CustomButtonUI for buttons (overrides all Button rendering)
    // No UIManager defaults are needed since we control all painting
  }

  // ── Helper: Calculate responsive component width ──
  private int getComponentWidth() {
    int panelWidth = getWidth();
    if (panelWidth <= 0) {
      panelWidth = PANEL_WIDTH;  // Use default if width not yet set
    }
    return Math.max((int)(panelWidth * CONTROL_WIDTH_RATIO), PANEL_MIN_WIDTH - SECTION_PADDING * 2);
  }

  // ── Helper: Show confirmation dialog for destructive actions ──
  private boolean confirmAction(String title, String message) {
    int result = JOptionPane.showConfirmDialog(
      this,
      message,
      title,
      JOptionPane.YES_NO_OPTION,
      JOptionPane.WARNING_MESSAGE
    );
    return result == JOptionPane.YES_OPTION;
  }

  // ── Helper: Show status feedback message (brief toast-like message) ──
  private void showStatus(String message) {
    // Create a temporary status popup
    JLabel statusLabel = new JLabel(message);
    statusLabel.setFont(new Font("Arial", Font.BOLD, 11));
    statusLabel.setForeground(COLOR_ACCENT_GREEN);
    statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    
    // Show in a popup that auto-closes after 2 seconds
    JWindow popup = new JWindow();
    popup.setContentPane(statusLabel);
    popup.setBackground(new Color(46, 46, 46, 240));
    popup.setAlwaysOnTop(true);
    popup.pack();
    
    // Position near the tool panel
    Component parent = SwingUtilities.getWindowAncestor(this);
    if (parent != null) {
      popup.setLocation(parent.getX() + 30, parent.getY() + 100);
    }
    popup.setVisible(true);
    
    // Auto-close after 2 seconds
    Timer timer = new Timer(2000, e -> popup.dispose());
    timer.setRepeats(false);
    timer.start();
  }

  // ── Helper: Show error feedback message (toast-like with warning styling) ──
  private void showError(String message) {
    // Create a temporary error popup
    JLabel errorLabel = new JLabel(message);
    errorLabel.setFont(new Font("Arial", Font.BOLD, 11));
    errorLabel.setForeground(new Color(255, 100, 100));  // Light red
    errorLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    
    // Show in a popup that auto-closes after 3 seconds
    JWindow popup = new JWindow();
    popup.setContentPane(errorLabel);
    popup.setBackground(new Color(80, 30, 30, 240));  // Dark red tint
    popup.setAlwaysOnTop(true);
    popup.pack();
    
    // Position near the tool panel
    Component parent = SwingUtilities.getWindowAncestor(this);
    if (parent != null) {
      popup.setLocation(parent.getX() + 30, parent.getY() + 100);
    }
    popup.setVisible(true);
    
    // Auto-close after 3 seconds
    Timer timer = new Timer(3000, e -> popup.dispose());
    timer.setRepeats(false);
    timer.start();
  }

  // ── Helper: Add hover effects to buttons ──
  private void addButtonHoverEffect(JButton button) {
    final Color normalBg = COLOR_BUTTON_DEFAULT;
    final Color normalFg = COLOR_PANEL_BG;
    final Color hoverBg = COLOR_BUTTON_HOVER;  // Glow green on hover
    final Color pressBg = Color.WHITE;  // White on press

    // Apply custom UI to prevent default L&F from overriding colors
    button.setUI(new CustomButtonUI());
    button.setFocusPainted(false);
    button.setContentAreaFilled(true);
    button.setOpaque(true);

    button.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == 0) {
          // Only change to glow green if not currently pressed
          button.setBackground(hoverBg);
          button.setForeground(COLOR_PANEL_BG);  // Dark text on glow green
        }
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        // Return to normal when mouse exits
        button.setBackground(normalBg);
        button.setForeground(normalFg);
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }

      @Override
      public void mousePressed(MouseEvent e) {
        button.setBackground(pressBg);
        button.setForeground(Color.BLACK);  // Black text on white
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        // Keep glow green until mouse exits
        button.setBackground(hoverBg);
        button.setForeground(COLOR_PANEL_BG);
      }
    });
  }

  // ── Helper: Add hover effects to sliders ──
  private void addSliderHoverEffect(JSlider slider) {
    slider.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        slider.setToolTipText(slider.getToolTipText() + " (Value: " + slider.getValue() + ")");
        slider.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        slider.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    });
  }

  // ── Helper: Add hover effects to checkboxes ──
  private void addCheckBoxHoverEffect(JCheckBox checkbox) {
    checkbox.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        checkbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        checkbox.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    });
  }

  // ── Helper: Add hover effects to radio buttons ──
  private void addRadioButtonHoverEffect(JRadioButton radio) {
    radio.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        radio.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        radio.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    });
  }

  // ── Helper: Set button disabled state with visual styling ──
  private void setButtonDisabled(JButton button, String reason) {
    button.setEnabled(false);
    button.setForeground(new Color(150, 150, 150));  // Grayed text
    button.setBackground(new Color(60, 60, 60));    // Darker button background
    button.setToolTipText(reason);
    button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  // ── Helper: Set button enabled state ──
  private void setButtonEnabled(JButton button) {
    button.setEnabled(true);
    button.setForeground(COLOR_PANEL_BG);
    button.setBackground(COLOR_BUTTON_DEFAULT);
  }

  // ── Helper: Set slider disabled state with visual styling ──
  private void setSliderDisabled(JSlider slider, String reason) {
    slider.setEnabled(false);
    slider.setForeground(new Color(100, 100, 100));  // Grayed green
    slider.setToolTipText(reason);
    slider.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  // ── Helper: Set slider enabled state ──
  private void setSliderEnabled(JSlider slider) {
    slider.setEnabled(true);
    slider.setForeground(COLOR_ACCENT_GREEN);
  }

  // ── Helper: Set checkbox disabled state with visual styling ──
  private void setCheckBoxDisabled(JCheckBox checkbox, String reason) {
    checkbox.setEnabled(false);
    checkbox.setForeground(new Color(100, 100, 100));  // Grayed out
    checkbox.setToolTipText(reason);
    checkbox.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  // ── Helper: Set checkbox enabled state ──
  private void setCheckBoxEnabled(JCheckBox checkbox) {
    checkbox.setEnabled(true);
    checkbox.setForeground(COLOR_ACCENT_GREEN);
  }

  // ── Helper: Animate button press (no visual change needed, hover handles it) ──
  private void animateButtonPress(JButton button) {
    // Button already shows white on hover via addButtonHoverEffect
    // No additional animation needed
  }

  // ── Helper: Animate tab switch with fade effect ──
  private void animateTabSwitch(JTabbedPane tabs, int newTab) {
    // Save current opacity
    Component currentComponent = tabs.getSelectedComponent();
    
    // Fade out current tab
    if (currentComponent != null) {
      currentComponent.setVisible(false);
    }
    
    // Switch tab
    tabs.setSelectedIndex(newTab);
    Component newComponent = tabs.getSelectedComponent();
    
    // Fade in new tab
    if (newComponent != null) {
      newComponent.setVisible(true);
    }
  }

  // ── Helper: Show brief success flash animation ──
  private void showSuccessFlash() {
    // Create a brief bright flash by showing the status with special styling
    JLabel flashLabel = new JLabel("✓");
    flashLabel.setFont(new Font("Arial", Font.BOLD, 20));
    flashLabel.setForeground(COLOR_ACCENT_GREEN);
    flashLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    
    JWindow flash = new JWindow();
    flash.setContentPane(flashLabel);
    flash.setBackground(new Color(46, 46, 46, 200));
    flash.setAlwaysOnTop(true);
    flash.pack();
    
    Component parent = SwingUtilities.getWindowAncestor(this);
    if (parent != null) {
      flash.setLocation(parent.getX() + 50, parent.getY() + 150);
    }
    flash.setVisible(true);
    
    // Auto-close after 500ms
    Timer timer = new Timer(500, e -> flash.dispose());
    timer.setRepeats(false);
    timer.start();
  }

  // ── Helper: Add keyboard shortcuts for tab navigation ──
  private void registerKeyboardShortcuts(JTabbedPane tabs) {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
      if (e.getID() == KeyEvent.KEY_PRESSED && (e.isControlDown())) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_1:
            tabs.setSelectedIndex(0);
            e.consume();
            return true;
          case KeyEvent.VK_2:
            tabs.setSelectedIndex(1);
            e.consume();
            return true;
          case KeyEvent.VK_3:
            tabs.setSelectedIndex(2);
            e.consume();
            return true;
          case KeyEvent.VK_4:
            tabs.setSelectedIndex(3);
            e.consume();
            return true;
        }
      }
      return false;
    });
  }

  // ── Helper: section separator label ──────────────────────────────────
  private JPanel section(String title) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    // Section header - clean bold text, no borders or decorations
    JLabel header = new JLabel(title);
    header.setFont(new Font("Arial", Font.BOLD, 11));
    header.setForeground(COLOR_TEXT_PRIMARY);
    header.setAlignmentX(Component.LEFT_ALIGNMENT);
    header.setBorder(BorderFactory.createEmptyBorder(SECTION_PADDING, 0, CONTROL_SPACING, 0));
    panel.add(header);
    
    panel.setBackground(COLOR_PANEL_BG);
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 0, CONTROL_SPACING, 0));
    return panel;
  }

  private JLabel rowLabel(String text) {
    JLabel l = new JLabel(text);
    l.setForeground(COLOR_TEXT_PRIMARY);
    l.setFont(new Font("Arial", Font.PLAIN, 11));
    l.setAlignmentX(Component.LEFT_ALIGNMENT);
    return l;
  }

  private JSlider rowSlider(int min, int max, int value) {
    JSlider s = new JSlider(min, max, value);
    int width = getComponentWidth();
    s.setPreferredSize(new Dimension(width, 28));
    s.setMaximumSize(new Dimension(width, 28));
    s.setBackground(COLOR_PANEL_BG);
    s.setForeground(COLOR_ACCENT_GREEN);
    s.setAlignmentX(Component.LEFT_ALIGNMENT);
    s.setFocusable(true);
    
    // Apply custom slider UI to this instance
    s.setUI(new CustomSliderUI(s));
    
    // Add hover effect
    addSliderHoverEffect(s);
    
    return s;
  }

  private JButton rowButton(String text) {
    JButton b = new JButton(text);
    int width = getComponentWidth();
    b.setAlignmentX(Component.LEFT_ALIGNMENT);
    b.setMaximumSize(new Dimension(width, BUTTON_HEIGHT));
    b.setBackground(COLOR_BUTTON_DEFAULT);
    b.setForeground(COLOR_PANEL_BG);
    b.setFocusPainted(false);  // No Swing focus painting
    b.setFont(new Font("Arial", Font.BOLD, 11));
    b.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    
    // Add hover effect
    addButtonHoverEffect(b);
    
    return b;
  }

  private JCheckBox rowCheckBox(String text, boolean selected) {
    JCheckBox cb = new JCheckBox(text, selected);
    cb.setBackground(COLOR_PANEL_BG);
    cb.setForeground(COLOR_ACCENT_GREEN);
    cb.setFont(new Font("Arial", Font.PLAIN, 11));
    cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    cb.setFocusable(true);
    cb.setFocusPainted(false);  // No Swing focus painting
    
    // Add hover effect
    addCheckBoxHoverEffect(cb);
    
    return cb;
  }

  private void pad(JPanel p) {
    p.add(Box.createRigidArea(new Dimension(0, CONTROL_SPACING)));
  }

  // ────────────────────────────────────────────────────────────────────
  // TAB 1 — Background
  // ────────────────────────────────────────────────────────────────────
  private JPanel buildBackgroundTab() {
    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
    root.setBackground(COLOR_PANEL_BG);
    root.setBorder(BorderFactory.createEmptyBorder(SECTION_PADDING, SECTION_PADDING, SECTION_PADDING, SECTION_PADDING));

    // Visibility
    JPanel visSection = section("Visibility");
    JCheckBox showBg = rowCheckBox("Show Background", true);
    showBg.setToolTipText("Toggle whether the background is visible on the canvas (checked = visible)");
    showBg.getAccessibleContext().setAccessibleName("Show Background");
    showBg.getAccessibleContext().setAccessibleDescription("Toggle background visibility on canvas");
    showBg.addActionListener(e -> { if (listener != null) listener.onBackgroundToggle(showBg.isSelected()); });
    visSection.add(showBg);
    root.add(visSection);
    pad(root);

    // Appearance
    JPanel appearSection = section("Appearance");
    JLabel alphaLabel = rowLabel("Transparency: 100%");
    appearSection.add(alphaLabel);
    JSlider alphaSlider = rowSlider(0, 100, 100);
    alphaSlider.setToolTipText("Adjust background transparency from fully transparent (0%) to fully opaque (100%)");
    alphaSlider.getAccessibleContext().setAccessibleName("Background Transparency");
    alphaSlider.getAccessibleContext().setAccessibleDescription("Adjust background transparency from 0 to 100 percent");
    alphaSlider.addChangeListener(e -> {
      float alpha = alphaSlider.getValue() / 100.0f;
      alphaLabel.setText(String.format("Transparency: %d%%", alphaSlider.getValue()));
      if (listener != null) listener.onBackgroundTransparencyChanged(alpha);
    });
    appearSection.add(alphaSlider);
    pad(appearSection);
    JButton colorBtn = rowButton("Colour...");
    colorBtn.setToolTipText("Click to open the color picker and choose a background color");
    colorBtn.getAccessibleContext().setAccessibleName("Background Color Picker");
    colorBtn.getAccessibleContext().setAccessibleDescription("Opens color picker to change background color");
    colorBtn.addActionListener(e -> {
      animateButtonPress(colorBtn);
      if (listener != null) {
        listener.onBackgroundColorPicker();
      }
    });
    appearSection.add(colorBtn);
    root.add(appearSection);
    pad(root);

    // Image
    JPanel imgSection = section("Image");
    JButton loadBtn = rowButton("Load Image...");
    loadBtn.setToolTipText("Click to browse your computer and select an image file as the background (PNG, JPG, etc.)");
    loadBtn.getAccessibleContext().setAccessibleName("Load Background Image");
    loadBtn.getAccessibleContext().setAccessibleDescription("Opens file browser to load a background image");
    loadBtn.addActionListener(e -> {
      animateButtonPress(loadBtn);
      if (listener != null) {
        listener.onLoadBackgroundImage();
      }
    });
    JButton clearImgBtn = rowButton("Clear Image");
    clearImgBtn.setToolTipText("Remove the currently loaded background image from the canvas");
    clearImgBtn.getAccessibleContext().setAccessibleName("Clear Background Image");
    clearImgBtn.getAccessibleContext().setAccessibleDescription("Removes the currently loaded background image");
    clearImgBtn.addActionListener(e -> {
      animateButtonPress(clearImgBtn);
      if (confirmAction("Clear Background Image", "Are you sure? This will remove the current background image.")) {
        if (listener != null) {
          listener.onClearBackgroundImage();
        }
      }
    });
    imgSection.add(loadBtn);
    pad(imgSection);
    imgSection.add(clearImgBtn);
    root.add(imgSection);

    root.add(Box.createVerticalGlue());
    return root;
  }

  // ────────────────────────────────────────────────────────────────────
  // TAB 2 — Vector Field
  // ────────────────────────────────────────────────────────────────────
  private JPanel buildVectorFieldTab() {
    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
    root.setBackground(COLOR_PANEL_BG);
    root.setBorder(BorderFactory.createEmptyBorder(SECTION_PADDING, SECTION_PADDING, SECTION_PADDING, SECTION_PADDING));

    // Visibility
    JPanel visSection = section("Visibility");
    JCheckBox showField = rowCheckBox("Show Vector Field", true);
    showField.setToolTipText("Toggle whether the vector field visualization is visible on the canvas");
    showField.getAccessibleContext().setAccessibleName("Show Vector Field");
    showField.getAccessibleContext().setAccessibleDescription("Toggle vector field visualization visibility");
    showField.addActionListener(e -> { if (listener != null) listener.onVectorFieldToggle(showField.isSelected()); });
    visSection.add(showField);
    root.add(visSection);
    pad(root);

    // Appearance
    JPanel appearSection = section("Appearance");
    JLabel alphaLabel = rowLabel("Transparency: 100%");
    appearSection.add(alphaLabel);
    JSlider alphaSlider = rowSlider(0, 100, 100);
    alphaSlider.setToolTipText("Control how transparent the vector field visualization appears (0% = invisible, 100% = opaque)");
    alphaSlider.getAccessibleContext().setAccessibleName("Vector Field Transparency");
    alphaSlider.getAccessibleContext().setAccessibleDescription("Adjust vector field transparency from 0 to 100 percent");
    alphaSlider.addChangeListener(e -> {
      alphaLabel.setText(String.format("Transparency: %d%%", alphaSlider.getValue()));
      if (listener != null) listener.onVectorFieldTransparencyChanged(alphaSlider.getValue() / 100.0f);
    });
    appearSection.add(alphaSlider);
    pad(appearSection);

    JLabel modeLabel = rowLabel("Visualization Mode");
    appearSection.add(modeLabel);
    JRadioButton arrowBtn = new JRadioButton("Arrow");
    arrowBtn.setToolTipText("Display vectors as arrows showing direction and magnitude");
    arrowBtn.getAccessibleContext().setAccessibleName("Arrow Visualization Mode");
    arrowBtn.getAccessibleContext().setAccessibleDescription("Display vector field as arrows");
    JRadioButton heatmapBtn = new JRadioButton("Heatmap");
    heatmapBtn.setToolTipText("Display vectors as color-coded heatmap showing magnitude intensity");
    heatmapBtn.getAccessibleContext().setAccessibleName("Heatmap Visualization Mode");
    heatmapBtn.getAccessibleContext().setAccessibleDescription("Display vector field as heatmap");
    styleRadio(arrowBtn); styleRadio(heatmapBtn);
    ButtonGroup modeGroup = new ButtonGroup();
    modeGroup.add(arrowBtn); modeGroup.add(heatmapBtn);
    arrowBtn.setSelected(true);
    arrowBtn.addActionListener(e -> { if (listener != null) listener.onVisualizationModeChanged("arrow"); });
    heatmapBtn.addActionListener(e -> { if (listener != null) listener.onVisualizationModeChanged("heatmap"); });
    appearSection.add(arrowBtn);
    appearSection.add(heatmapBtn);
    root.add(appearSection);
    pad(root);

    // Actions
    JPanel actSection = section("Actions");
    JButton resetBtn = rowButton("Reset Field");
    resetBtn.setToolTipText("Restore vector field to its default initial state");
    resetBtn.getAccessibleContext().setAccessibleName("Reset Vector Field");
    resetBtn.getAccessibleContext().setAccessibleDescription("Resets the vector field to its default state");
    resetBtn.addActionListener(e -> {
      animateButtonPress(resetBtn);
      if (listener != null) {
        listener.onResetVectorField();
      }
    });
    JButton randomBtn = rowButton("Randomize Field");
    randomBtn.setToolTipText("Generate a completely random vector field with new direction and magnitude values");
    randomBtn.getAccessibleContext().setAccessibleName("Randomize Vector Field");
    randomBtn.getAccessibleContext().setAccessibleDescription("Generates random vector field values");
    randomBtn.addActionListener(e -> {
      animateButtonPress(randomBtn);
      if (listener != null) {
        listener.onRandomVectorField();
      }
    });
    actSection.add(resetBtn);
    pad(actSection);
    actSection.add(randomBtn);
    root.add(actSection);

    root.add(Box.createVerticalGlue());
    return root;
  }

  // ────────────────────────────────────────────────────────────────────
  // TAB 3 — Sketch
  // ────────────────────────────────────────────────────────────────────
  private JPanel buildSketchTab() {
    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
    root.setBackground(COLOR_PANEL_BG);
    root.setBorder(BorderFactory.createEmptyBorder(SECTION_PADDING, SECTION_PADDING, SECTION_PADDING, SECTION_PADDING));

    // Visibility
    JPanel visSection = section("Visibility");
    JCheckBox showStrokes = rowCheckBox("Show Strokes", false);
    showStrokes.setToolTipText("Toggle whether your sketch strokes are visible on the canvas");
    showStrokes.getAccessibleContext().setAccessibleName("Show Strokes");
    showStrokes.getAccessibleContext().setAccessibleDescription("Toggle sketch strokes visibility on canvas");
    showStrokes.addActionListener(e -> { if (listener != null) listener.onSketchToggle(showStrokes.isSelected()); });
    visSection.add(showStrokes);
    root.add(visSection);
    pad(root);

    // Tool Mode (Brush vs Eraser)
    JPanel modeSection = section("Tool Mode");
    JRadioButton brushRadio = new JRadioButton("Brush", true);
    brushRadio.setForeground(COLOR_ACCENT_GREEN);
    brushRadio.setBackground(COLOR_PANEL_BG);
    brushRadio.setToolTipText("Paint mode: adds forces to guide the vector field");
    brushRadio.getAccessibleContext().setAccessibleName("Brush Mode");
    brushRadio.getAccessibleContext().setAccessibleDescription("Select brush mode to paint forces");
    addRadioButtonHoverEffect(brushRadio);
    
    JRadioButton eraserRadio = new JRadioButton("Eraser", false);
    eraserRadio.setForeground(COLOR_ACCENT_GREEN);
    eraserRadio.setBackground(COLOR_PANEL_BG);
    eraserRadio.setToolTipText("Erase mode: reduces field vectors toward zero");
    eraserRadio.getAccessibleContext().setAccessibleName("Eraser Mode");
    eraserRadio.getAccessibleContext().setAccessibleDescription("Select eraser mode to reduce vector field");
    addRadioButtonHoverEffect(eraserRadio);
    
    ButtonGroup modeGroup = new ButtonGroup();
    modeGroup.add(brushRadio);
    modeGroup.add(eraserRadio);
    
    brushRadio.addActionListener(e -> { if (listener != null) listener.onBrushModeChanged(true); });
    eraserRadio.addActionListener(e -> { if (listener != null) listener.onBrushModeChanged(false); });
    
    modeSection.add(brushRadio);
    modeSection.add(eraserRadio);
    root.add(modeSection);
    pad(root);

    // Appearance
    JPanel appearSection = section("Appearance");
    JLabel alphaLabel = rowLabel("Transparency: 100%");
    appearSection.add(alphaLabel);
    JSlider alphaSlider = rowSlider(0, 100, 100);
    alphaSlider.setToolTipText("Control the opacity of sketch strokes (0% = invisible, 100% = fully opaque)");
    alphaSlider.getAccessibleContext().setAccessibleName("Sketch Transparency");
    alphaSlider.getAccessibleContext().setAccessibleDescription("Adjust sketch transparency from 0 to 100 percent");
    alphaSlider.addChangeListener(e -> {
      alphaLabel.setText(String.format("Transparency: %d%%", alphaSlider.getValue()));
      if (listener != null) listener.onSketchTransparencyChanged(alphaSlider.getValue() / 100.0f);
    });
    appearSection.add(alphaSlider);
    root.add(appearSection);
    pad(root);

    // Brush settings
    JPanel brushSection = section("Brush Settings");

    brushSizeLabel = rowLabel("Size: 30");
    brushSection.add(brushSizeLabel);
    JSlider sizeSlider = rowSlider(5, 200, 30);
    sizeSlider.setToolTipText("Brush diameter in pixels: 5 (tiny) to 200 (huge). Affects stroke width");
    sizeSlider.getAccessibleContext().setAccessibleName("Brush Size");
    sizeSlider.getAccessibleContext().setAccessibleDescription("Adjust brush size from 5 to 200 pixels");
    sizeSlider.addChangeListener(e -> {
      float v = sizeSlider.getValue();
      brushSizeLabel.setText(String.format("Size: %.0f", v));
      if (listener != null) listener.onBrushSizeChanged(v);
    });
    brushSection.add(sizeSlider);
    pad(brushSection);

    brushHardnessLabel = rowLabel("Hardness: 0.50");
    brushSection.add(brushHardnessLabel);
    JSlider hardSlider = rowSlider(0, 100, 50);
    hardSlider.setToolTipText("Edge softness: 0 (soft blur) to 1.0 (hard edges). Controls stroke falloff");
    hardSlider.getAccessibleContext().setAccessibleName("Brush Hardness");
    hardSlider.getAccessibleContext().setAccessibleDescription("Adjust brush hardness from 0.00 to 1.00");
    hardSlider.addChangeListener(e -> {
      float v = hardSlider.getValue() / 100.0f;
      brushHardnessLabel.setText(String.format("Hardness: %.2f", v));
      if (listener != null) listener.onBrushHardnessChanged(v);
    });
    brushSection.add(hardSlider);
    pad(brushSection);

    brushStrengthLabel = rowLabel("Strength: 2.0");
    brushSection.add(brushStrengthLabel);
    JSlider strengthSlider = rowSlider(1, 50, 20);
    strengthSlider.setToolTipText("Opacity intensity per brush stroke: 0.1 (faint) to 5.0 (intense). Controls color intensity");
    strengthSlider.getAccessibleContext().setAccessibleName("Brush Strength");
    strengthSlider.getAccessibleContext().setAccessibleDescription("Adjust brush strength from 0.1 to 5.0");
    strengthSlider.addChangeListener(e -> {
      float v = strengthSlider.getValue() / 10.0f;
      brushStrengthLabel.setText(String.format("Strength: %.1f", v));
      if (listener != null) listener.onBrushStrengthChanged(v);
    });
    brushSection.add(strengthSlider);
    root.add(brushSection);
    pad(root);

    // Actions
    JPanel actSection = section("Actions");
    JButton clearBtn = rowButton("Clear Strokes");
    clearBtn.setToolTipText("Permanently erase all sketch strokes from the canvas (cannot be undone)");
    clearBtn.getAccessibleContext().setAccessibleName("Clear Sketch Strokes");
    clearBtn.getAccessibleContext().setAccessibleDescription("Removes all sketch strokes from the canvas");
    clearBtn.addActionListener(e -> {
      animateButtonPress(clearBtn);
      if (confirmAction("Clear Sketch Strokes", "Are you sure? All sketch strokes will be removed. This cannot be undone.")) {
        if (listener != null) {
          listener.onClearSketch();
        }
      }
    });
    actSection.add(clearBtn);
    root.add(actSection);

    root.add(Box.createVerticalGlue());
    return root;
  }

  // ────────────────────────────────────────────────────────────────────
  // TAB 4 — Botfield
  // ────────────────────────────────────────────────────────────────────
  private JPanel buildBotfieldTab() {
    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
    root.setBackground(COLOR_PANEL_BG);
    root.setBorder(BorderFactory.createEmptyBorder(SECTION_PADDING, SECTION_PADDING, SECTION_PADDING, SECTION_PADDING));

    // Visibility
    JPanel visSection = section("Visibility");
    JCheckBox showBots = rowCheckBox("Show Botfield", true);
    showBots.setToolTipText("Toggle whether bots and their visualizations are visible on the canvas");
    showBots.getAccessibleContext().setAccessibleName("Show Botfield");
    showBots.getAccessibleContext().setAccessibleDescription("Toggle botfield visualization visibility");
    showBots.addActionListener(e -> { if (listener != null) listener.onBotfieldToggle(showBots.isSelected()); });
    visSection.add(showBots);
    root.add(visSection);
    pad(root);

    // Appearance
    JPanel appearSection = section("Appearance");
    JLabel alphaLabel = rowLabel("Transparency: 100%");
    appearSection.add(alphaLabel);
    JSlider alphaSlider = rowSlider(0, 100, 100);
    alphaSlider.setToolTipText("Control how visible bots and their trails appear (0% = invisible, 100% = fully opaque)");
    alphaSlider.getAccessibleContext().setAccessibleName("Botfield Transparency");
    alphaSlider.getAccessibleContext().setAccessibleDescription("Adjust botfield transparency from 0 to 100 percent");
    alphaSlider.addChangeListener(e -> {
      alphaLabel.setText(String.format("Transparency: %d%%", alphaSlider.getValue()));
      if (listener != null) listener.onBotfieldTransparencyChanged(alphaSlider.getValue() / 100.0f);
    });
    appearSection.add(alphaSlider);
    root.add(appearSection);
    pad(root);

    // Bot settings
    JPanel botSection = section("Bot Settings");

    botLifeLabel = rowLabel("Life: 500");
    botSection.add(botLifeLabel);
    JSlider lifeSlider = rowSlider(100, 2000, 500);
    lifeSlider.setToolTipText("How long bots survive before disappearing (in simulation ticks): 100 (short) to 2000 (long)");
    lifeSlider.getAccessibleContext().setAccessibleName("Bot Life");
    lifeSlider.getAccessibleContext().setAccessibleDescription("Adjust bot life span from 100 to 2000 ticks");
    lifeSlider.addChangeListener(e -> {
      botLifeLabel.setText(String.format("Life: %d", lifeSlider.getValue()));
      if (listener != null) listener.onBotLifeChanged(lifeSlider.getValue());
    });
    botSection.add(lifeSlider);
    pad(botSection);

    botRadarLabel = rowLabel("Radar: 50");
    botSection.add(botRadarLabel);
    JSlider radarSlider = rowSlider(10, 200, 50);
    radarSlider.setToolTipText("How far bots can sense their environment (detection range in pixels): 10 (short) to 200 (far)");
    radarSlider.getAccessibleContext().setAccessibleName("Bot Radar Range");
    radarSlider.getAccessibleContext().setAccessibleDescription("Adjust bot radar sensing range from 10 to 200 pixels");
    radarSlider.addChangeListener(e -> {
      botRadarLabel.setText(String.format("Radar: %d", radarSlider.getValue()));
      if (listener != null) listener.onBotRadarChanged(radarSlider.getValue());
    });
    botSection.add(radarSlider);
    pad(botSection);

    botDriftLabel = rowLabel("Drift: 0.30");
    botSection.add(botDriftLabel);
    JSlider driftSlider = rowSlider(0, 100, 30);
    driftSlider.setToolTipText("Random movement variation: 0 (predictable) to 1.0 (chaotic). Affects path randomness");
    driftSlider.getAccessibleContext().setAccessibleName("Bot Drift");
    driftSlider.getAccessibleContext().setAccessibleDescription("Adjust bot drift randomness from 0.00 to 1.00");
    driftSlider.addChangeListener(e -> {
      float v = driftSlider.getValue() / 100.0f;
      botDriftLabel.setText(String.format("Drift: %.2f", v));
      if (listener != null) listener.onBotDriftChanged(v);
    });
    botSection.add(driftSlider);
    pad(botSection);

    botSpeedLabel = rowLabel("Speed: 2.0");
    botSection.add(botSpeedLabel);
    JSlider speedSlider = rowSlider(1, 50, 20);
    speedSlider.setToolTipText("How fast bots move across the canvas (pixels per tick): 0.1 (crawl) to 5.0 (sprint)");
    speedSlider.getAccessibleContext().setAccessibleName("Bot Speed");
    speedSlider.getAccessibleContext().setAccessibleDescription("Adjust bot movement speed from 0.1 to 5.0 pixels per tick");
    speedSlider.addChangeListener(e -> {
      float v = speedSlider.getValue() / 10.0f;
      botSpeedLabel.setText(String.format("Speed: %.1f", v));
      if (listener != null) listener.onBotSpeedChanged(v);
    });
    botSection.add(speedSlider);
    root.add(botSection);
    pad(root);

    // Simulation settings
    JPanel simSection = section("Simulation");

    // Simulation controls (Start/Pause/Reset)
    JPanel ctrlPanel = new JPanel();
    ctrlPanel.setLayout(new BoxLayout(ctrlPanel, BoxLayout.X_AXIS));
    ctrlPanel.setBackground(COLOR_PANEL_BG);
    ctrlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    JButton startBtn = rowButton("Start");
    startBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
    startBtn.setToolTipText("Start/resume the bot simulation");
    startBtn.getAccessibleContext().setAccessibleName("Start Simulation");
    startBtn.getAccessibleContext().setAccessibleDescription("Start or resume the bot field simulation");
    startBtn.addActionListener(e -> {
      animateButtonPress(startBtn);
      if (listener != null) listener.onSimulationStart();
    });
    ctrlPanel.add(startBtn);
    ctrlPanel.add(Box.createHorizontalStrut(4));
    
    JButton pauseBtn = rowButton("Pause");
    pauseBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
    pauseBtn.setToolTipText("Pause the bot simulation");
    pauseBtn.getAccessibleContext().setAccessibleName("Pause Simulation");
    pauseBtn.getAccessibleContext().setAccessibleDescription("Pause the bot field simulation");
    pauseBtn.addActionListener(e -> {
      animateButtonPress(pauseBtn);
      if (listener != null) listener.onSimulationPause();
    });
    ctrlPanel.add(pauseBtn);
    ctrlPanel.add(Box.createHorizontalStrut(4));
    
    JButton resetBtn = rowButton("Reset");
    resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
    resetBtn.setToolTipText("Reset the bot simulation (clears all bots and restarts)");
    resetBtn.getAccessibleContext().setAccessibleName("Reset Simulation");
    resetBtn.getAccessibleContext().setAccessibleDescription("Reset the bot field simulation to initial state");
    resetBtn.addActionListener(e -> {
      animateButtonPress(resetBtn);
      if (confirmAction("Reset Simulation", "Clear all bots and reset simulation? This cannot be undone.")) {
        if (listener != null) listener.onSimulationReset();
      }
    });
    ctrlPanel.add(resetBtn);
    
    simSection.add(ctrlPanel);
    pad(simSection);

    botSpawnRateLabel = rowLabel("Spawn Rate: 5");
    simSection.add(botSpawnRateLabel);
    JSlider rateSlider = rowSlider(1, 20, 5);
    rateSlider.setToolTipText("When auto-spawn is on, how many bots spawn per cycle: 1 (rare) to 20 (frequent)");
    rateSlider.getAccessibleContext().setAccessibleName("Bot Spawn Rate");
    rateSlider.getAccessibleContext().setAccessibleDescription("Adjust bot auto-spawn rate from 1 to 20 bots per cycle");
    rateSlider.addChangeListener(e -> {
      botSpawnRateLabel.setText(String.format("Spawn Rate: %d", rateSlider.getValue()));
      if (listener != null) listener.onBotSpawnRateChanged(rateSlider.getValue());
    });
    simSection.add(rateSlider);
    pad(simSection);

    JCheckBox autoSpawn = rowCheckBox("Auto Spawn", false);
    autoSpawn.setToolTipText("Enable automatic spawning of bots at the rate specified above");
    autoSpawn.getAccessibleContext().setAccessibleName("Auto Spawn Bots");
    autoSpawn.getAccessibleContext().setAccessibleDescription("Enable automatic bot spawning at configured rate");
    autoSpawn.addActionListener(e -> { if (listener != null) listener.onAutoSpawnToggle(autoSpawn.isSelected()); });
    simSection.add(autoSpawn);
    pad(simSection);

    JButton spawnBtn = rowButton("Spawn Bot");
    spawnBtn.setToolTipText("Immediately create and add one new bot to the botfield (regardless of auto-spawn setting)");
    spawnBtn.getAccessibleContext().setAccessibleName("Spawn New Bot");
    spawnBtn.getAccessibleContext().setAccessibleDescription("Creates and adds a new bot to the botfield immediately");
    spawnBtn.addActionListener(e -> {
      animateButtonPress(spawnBtn);
      if (listener != null) {
        listener.onSpawnBot();
      }
    });
    simSection.add(spawnBtn);
    pad(simSection);

    JButton clearBotsBtn = rowButton("Clear Bots");
    clearBotsBtn.setToolTipText("Remove all currently active bots from the botfield (cannot be undone)");
    clearBotsBtn.getAccessibleContext().setAccessibleName("Clear All Bots");
    clearBotsBtn.getAccessibleContext().setAccessibleDescription("Removes all active bots from the botfield");
    clearBotsBtn.addActionListener(e -> {
      animateButtonPress(clearBotsBtn);
      if (confirmAction("Clear All Bots", "Are you sure? All active bots will be removed. This cannot be undone.")) {
        if (listener != null) {
          listener.onClearBots();
        }
      }
    });
    simSection.add(clearBotsBtn);
    root.add(simSection);

    root.add(Box.createVerticalGlue());
    return root;
  }

  // ── Radio button styling helper ────────────────────────────────────
  private void styleRadio(JRadioButton rb) {
    rb.setBackground(COLOR_PANEL_BG);
    rb.setFont(new Font("Arial", Font.PLAIN, 11));
    rb.setAlignmentX(Component.LEFT_ALIGNMENT);
    rb.setFocusable(true);  // Ensure radio button is reachable via Tab
    rb.setFocusPainted(false);  // No Swing focus painting
    
    // Update text color based on selection state
    updateRadioTextColor(rb);
    
    // Add listener to update text color when selection changes
    rb.addChangeListener(e -> updateRadioTextColor(rb));
    
    // Add hover effect
    addRadioButtonHoverEffect(rb);
  }
  
  // Helper: Update radio button text color based on selection
  private void updateRadioTextColor(JRadioButton rb) {
    if (rb.isSelected()) {
      rb.setForeground(COLOR_BUTTON_HOVER);  // Glow green when selected
    } else {
      rb.setForeground(COLOR_BUTTON_DEFAULT);  // Normal green when not selected
    }
  }
  
  // ════════════════════════════════════════════════════════════════════════════════
  // CustomTabbedPaneUI - Rounded corners on tabs + fill container width
  // ════════════════════════════════════════════════════════════════════════════════
  private static class RoundedTabbedPaneUI extends javax.swing.plaf.basic.BasicTabbedPaneUI {
    private static final int TAB_RADIUS = 10;
    private static final int TAB_HEIGHT = 40;
    
    @Override
    public void paint(Graphics g, JComponent c) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      // Recalculate tab positions before painting to fill width
      int tabCount = tabPane.getTabCount();
      if (tabCount > 0) {
        int containerWidth = tabPane.getWidth();
        int tabWidth = containerWidth / tabCount;
        int tabHeight = TAB_HEIGHT;
        
        // Redistribute all tab rects to fill the full width
        for (int i = 0; i < tabCount && i < rects.length; i++) {
          rects[i].x = i * tabWidth;
          rects[i].y = 0;
          rects[i].width = (i == tabCount - 1) ? containerWidth - (i * tabWidth) : tabWidth;
          rects[i].height = tabHeight;
        }
      }
      
      super.paint(g2, c);
    }
    
    @Override
    protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
      Rectangle bounds = super.getTabBounds(tabIndex, dest);
      if (bounds != null && tabIndex < rects.length) {
        bounds.width = rects[tabIndex].width;
      }
      return bounds;
    }
    
    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
      return TAB_HEIGHT;
    }
    
    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {
      // Remove any insets that could crop the content
      return new Insets(0, 0, 0, 0);
    }
    
    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
      return 0;  // No shift
    }
    
    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
      return 0;  // No shift
    }
    
    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
      // Distribute width evenly across all tabs
      int containerWidth = tabPane.getWidth();
      int tabCount = tabPane.getTabCount();
      
      if (containerWidth <= 0 || tabCount <= 0) {
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics);
      }
      
      return containerWidth / tabCount;
    }
    
    @Override
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
      if (tabIndex >= rects.length) return;
      
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      
      Rectangle rect = rects[tabIndex];
      boolean isSelected = (tabIndex == tabPane.getSelectedIndex());
      
      // Draw tab background with rounded corners
      RoundRectangle2D roundRect = new RoundRectangle2D.Double(rect.x, rect.y, rect.width, rect.height, TAB_RADIUS, TAB_RADIUS);
      
      if (isSelected) {
        g2.setColor(COLOR_PANEL_BG);
      } else {
        g2.setColor(new Color(50, 50, 50));
      }
      g2.fill(roundRect);
      
      // Draw inset border inside the tab
      int inset = 2;
      RoundRectangle2D insetRect = new RoundRectangle2D.Double(
        rect.x + inset, rect.y + inset, 
        rect.width - (inset * 2), rect.height - inset, 
        TAB_RADIUS - 2, TAB_RADIUS - 2
      );
      
      if (isSelected) {
        g2.setColor(COLOR_ACCENT_GREEN);
        g2.setStroke(new BasicStroke(2));
      } else {
        g2.setColor(new Color(70, 70, 70));
        g2.setStroke(new BasicStroke(1));
      }
      g2.draw(insetRect);
      
      // Draw bottom separator line only for unselected tabs
      if (!isSelected) {
        g2.setColor(COLOR_ACCENT_GREEN);
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width, rect.y + rect.height - 1);
      }
      
      // Draw text
      String title = tabPane.getTitleAt(tabIndex);
      Font font = tabPane.getFont().deriveFont(Font.BOLD, 12f);
      g2.setFont(font);
      g2.setColor(COLOR_TEXT_TAB);
      
      FontMetrics fm = g2.getFontMetrics();
      int textWidth = fm.stringWidth(title);
      int textHeight = fm.getAscent();
      
      int centerX = rect.x + (rect.width - textWidth) / 2;
      int centerY = rect.y + ((rect.height - textHeight) / 2) + fm.getAscent();
      
      g2.drawString(title, centerX, centerY);
    }
  }
  
  
  // ════════════════════════════════════════════════════════════════════════════════
  // CustomSliderUI - Green thumb on dark track with clean styling
  // ════════════════════════════════════════════════════════════════════════════════
  private static class CustomSliderUI extends javax.swing.plaf.basic.BasicSliderUI {
    private static final int THUMB_WIDTH = 14;
    private static final int THUMB_HEIGHT = 24;
    private static final int TRACK_HEIGHT = 6;
    
    private boolean thumbHovered = false;
    private boolean thumbPressed = false;
    
    public CustomSliderUI(JSlider slider) {
      super(slider);
      
      // Add mouse listener for hover and press tracking
      slider.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          updateThumbHover(e);
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
          updateThumbHover(e);
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
          thumbHovered = false;
          slider.repaint();
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
          thumbPressed = true;
          slider.repaint();
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
          thumbPressed = false;
          updateThumbHover(e);
        }
        
        private void updateThumbHover(MouseEvent e) {
          Rectangle thumbArea = new Rectangle(thumbRect);
          thumbArea.grow(4, 4); // Expand hover area slightly
          thumbHovered = thumbArea.contains(e.getPoint());
          slider.repaint();
        }
      });
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      recalculateIfInsetsChanged();
      recalculateIfOrientationChanged();
      
      Rectangle clip = g2.getClipBounds();
      
      // Paint track
      if (clip.intersects(trackRect)) {
        paintTrack(g2);
      }
      
      // Paint ticks
      if (slider.getPaintTicks() && clip.intersects(tickRect)) {
        paintTicks(g2);
      }
      
      // Paint labels
      if (slider.getPaintLabels() && clip.intersects(labelRect)) {
        paintLabels(g2);
      }
      
      // Paint thumb
      if (clip.intersects(thumbRect)) {
        paintThumb(g2);
      }
      
      // Paint focus ring
      if (slider.hasFocus() && clip.intersects(focusRect)) {
        paintFocus(g2);
      }
    }
    
    public void paintTrack(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      int trackY = trackRect.y + (trackRect.height - TRACK_HEIGHT) / 2;
      
      if (slider.getOrientation() == JSlider.HORIZONTAL) {
        int thumbX = thumbRect.x + thumbRect.width / 2;
        
        // Left side (filled): Normal green
        g2.setColor(COLOR_BUTTON_DEFAULT);
        g2.fillRect(trackRect.x, trackY, thumbX - trackRect.x, TRACK_HEIGHT);
        
        // Right side (unfilled): Dark grey
        g2.setColor(new Color(80, 80, 80));
        g2.fillRect(thumbX, trackY, trackRect.x + trackRect.width - thumbX, TRACK_HEIGHT);
      }
    }
    
    public void paintThumb(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      // Determine thumb color based on state
      Color thumbColor;
      Color borderColor;
      
      if (thumbPressed) {
        // White on press
        thumbColor = Color.WHITE;
        borderColor = Color.LIGHT_GRAY;
      } else if (thumbHovered) {
        // Glow green on hover
        thumbColor = COLOR_BUTTON_HOVER;
        borderColor = COLOR_BUTTON_HOVER;
      } else {
        // Normal green (rest state)
        thumbColor = COLOR_BUTTON_DEFAULT;
        borderColor = new Color(0, 150, 0);
      }
      
      RoundRectangle2D thumb = new RoundRectangle2D.Double(
        thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, 4, 4
      );
      
      g2.setColor(thumbColor);
      g2.fill(thumb);
      
      // Border for definition
      g2.setColor(borderColor);
      g2.setStroke(new BasicStroke(1));
      g2.draw(thumb);
    }
    
    public void paintFocus(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      g2.setColor(COLOR_TEXT_PRIMARY);
      g2.setStroke(new BasicStroke(2));
      int margin = 2;
      g2.drawRect(
        focusRect.x - margin, focusRect.y - margin,
        focusRect.width + margin * 2, focusRect.height + margin * 2
      );
    }
  }
}

