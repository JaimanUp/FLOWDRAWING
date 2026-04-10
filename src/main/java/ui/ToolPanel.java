package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ToolPanel - Phase 1 Refactor: Tool-Centric Architecture (Java 11 Compatible)
 *
 * NEW STRUCTURE (3-Level Hierarchy):
 * ─────────────────────────────────
 * 1. Layer Tabs [FIXED]        - Select layer (BKGD, VECFD, BOTFD)
 * 2. Tool Ribbon [FIXED]       - Icon buttons: [☑ Visibility] [Tool] [Tool] ...
 * 3. Settings Panel [SCROLLABLE] - Dynamic content per selected tool
 */

public class ToolPanel extends JPanel {

  // ── Constants ────────────────────────────────────────────────────────
  private static final int PANEL_WIDTH = 230;
  private static final int PANEL_MIN_WIDTH = 200;
  private static final int SECTION_PADDING = 8;
  private static final int CONTROL_SPACING = 4;
  private static final int BUTTON_HEIGHT = 44;

  // ── Simplified Color Palette ─────────────────────────────────────────
  private static final Color COLOR_PANEL_BG = new Color(46, 46, 46);
  private static final Color COLOR_TEXT_PRIMARY = new Color(232, 232, 232);
  private static final Color COLOR_TEXT_SECONDARY = new Color(176, 176, 176);
  private static final Color COLOR_ACCENT_GREEN = new Color(0, 255, 0);
  private static final Color COLOR_BUTTON_DEFAULT = new Color(0, 180, 0);
  private static final Color COLOR_BUTTON_HOVER = new Color(0, 255, 0);

  // ── Layer Definitions ────────────────────────────────────────────────
  private static final int LAYER_BKGD = 0;
  private static final int LAYER_VECFD = 1;
  private static final int LAYER_BOTFD = 2;
  private static final String[] LAYER_NAMES = {"BKGD", "VECFD", "BOTFD"};

  // ── Listener Interface (Preserved from original) ──────────────────────
  public interface ToolListener {
    // Background
    default void onBackgroundToggle(boolean show) {}
    default void onBackgroundTransparencyChanged(float alpha) {}
    default void onBackgroundColorPicker() {}
    default void onLoadBackgroundImage() {}
    default void onClearBackgroundImage() {}

    // Vector Field
    default void onVectorFieldToggle(boolean show) {}
    default void onVectorFieldTransparencyChanged(float alpha) {}
    default void onVisualizationModeChanged(String mode) {}
    default void onResetVectorField() {}
    default void onRandomVectorField() {}

    // Sketch
    default void onSketchToggle(boolean show) {}
    default void onSketchTransparencyChanged(float alpha) {}
    default void onBrushModeChanged(boolean isBrush) {}
    default void onBrushSizeChanged(float size) {}
    default void onBrushHardnessChanged(float hardness) {}
    default void onBrushStrengthChanged(float strength) {}
    default void onClearSketch() {}

    // Botfield
    default void onBotfieldToggle(boolean show) {}
    default void onBotfieldTransparencyChanged(float alpha) {}
    default void onBotTraceFadeToggle(boolean enabled) {}
    default void onBotLifeChanged(float life) {}
    default void onBotRadarChanged(float radar) {}
    default void onBotSpeedChanged(float speed) {}
    default void onBotDriftInfluenceChanged(float influence) {}
    default void onBotFieldInfluenceChanged(float influence) {}
    default void onBotRepulsionInfluenceChanged(float influence) {}
    default void onBotRepulsionRadiusChanged(float radius) {}
    default void onSpawnBot() {}
    default void onAutoSpawnToggle(boolean enabled) {}
    default void onBotNumberChanged(int number) {}
    default void onSimulationToggle() {}
    default void onSimulationReset() {}
    default void onClearBots() {}
    default void onClearTraces() {}
    default void onBotLuminanceDecayToggle(boolean enabled) {}

    // Radar sampling
    default void onRadarSamplingFalloffChanged(String falloffType) {}
    default void onRadarSamplesChanged(int numSamples) {}
    default void onRadarSampleDistanceChanged(float distance) {}
    default void onCenterSampleWeightChanged(float weight) {}
  }

  // ── State ────────────────────────────────────────────────────────────
  private final ToolListener listener;
  private int currentLayer = LAYER_BKGD;
  private String currentToolId = null;
  private final boolean[] layerVisibility = {true, true, true};

  // UI Components
  private JPanel layerTabsPanel;
  private JPanel toolRibbonPanel;
  private JPanel settingsPanelContainer;
  private JScrollPane settingsScroll;
  private JButton[] layerButtons = new JButton[3];

  // Public API
  public JLabel botCountLabel;
  public JButton simToggleBtn;

  public ToolPanel(ToolListener listener) {
    this.listener = listener;
    setLayout(new BorderLayout(0, 0));
    setPreferredSize(new Dimension(PANEL_WIDTH, 0));
    setMinimumSize(new Dimension(PANEL_MIN_WIDTH, 0));
    setBackground(COLOR_PANEL_BG);
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    buildLayerTabs();
    buildToolRibbon();
    buildSettingsPanel();

    // Create wrapper for NORTH section (layer tabs + tool ribbon)
    JPanel northWrapper = new JPanel();
    northWrapper.setLayout(new BoxLayout(northWrapper, BoxLayout.Y_AXIS));
    northWrapper.setBackground(COLOR_PANEL_BG);
    northWrapper.add(layerTabsPanel);
    northWrapper.add(toolRibbonPanel);

    add(northWrapper, BorderLayout.NORTH);
    add(settingsScroll, BorderLayout.CENTER);

    switchToTool(LAYER_BKGD, "color");
  }

  // ────────────────────────────────────────────────────────────────────
  // Level 1: Layer Tabs
  // ────────────────────────────────────────────────────────────────────

  private void buildLayerTabs() {
    layerTabsPanel = new JPanel();
    layerTabsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
    layerTabsPanel.setBackground(COLOR_PANEL_BG);
    layerTabsPanel.setPreferredSize(new Dimension(PANEL_WIDTH, 40));
    layerTabsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    for (int i = 0; i < LAYER_NAMES.length; i++) {
      final int layerIndex = i;
      JButton layerBtn = new JButton(LAYER_NAMES[i]);
      layerBtn.setFont(new Font("Arial", Font.BOLD, 10));
      layerBtn.setPreferredSize(new Dimension(68, 32));
      layerBtn.setBackground(i == 0 ? COLOR_ACCENT_GREEN : COLOR_BUTTON_DEFAULT);
      layerBtn.setForeground(COLOR_PANEL_BG);
      layerBtn.setFocusPainted(false);
      layerBtn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
      layerBtn.setOpaque(true);

      layerBtn.addActionListener(e -> switchLayer(layerIndex));
      layerBtn.addMouseListener(makeHoverListener(layerBtn));
      layerButtons[i] = layerBtn;
      layerTabsPanel.add(layerBtn);
    }
  }

  private void switchLayer(int layer) {
    currentLayer = layer;
    updateLayerTabsDisplay();
    rebuildToolRibbon();
    switchToTool(layer, getDefaultToolForLayer(layer));
  }

  private void updateLayerTabsDisplay() {
    for (int i = 0; i < layerButtons.length; i++) {
      if (i == currentLayer) {
        layerButtons[i].setBackground(COLOR_ACCENT_GREEN);
      } else {
        layerButtons[i].setBackground(COLOR_BUTTON_DEFAULT);
      }
    }
    layerTabsPanel.repaint();
  }

  private String getDefaultToolForLayer(int layer) {
    if (layer == LAYER_BKGD) return "color";
    if (layer == LAYER_VECFD) return "paint";
    if (layer == LAYER_BOTFD) return "place";
    return "color";
  }

  // ────────────────────────────────────────────────────────────────────
  // Level 2: Tool Ribbon
  // ────────────────────────────────────────────────────────────────────

  private void buildToolRibbon() {
    toolRibbonPanel = new JPanel();
    toolRibbonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
    toolRibbonPanel.setBackground(COLOR_PANEL_BG);
    toolRibbonPanel.setPreferredSize(new Dimension(PANEL_WIDTH, 50));
    toolRibbonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
  }

  private void rebuildToolRibbon() {
    toolRibbonPanel.removeAll();

    // Visibility icon
    boolean isVisible = layerVisibility[currentLayer];
    JButton visibilityBtn = new JButton(isVisible ? "☑" : "☐");
    visibilityBtn.setFont(new Font("Arial", Font.BOLD, 14));
    visibilityBtn.setPreferredSize(new Dimension(36, 36));
    visibilityBtn.setBackground(isVisible ? COLOR_ACCENT_GREEN : COLOR_BUTTON_DEFAULT);
    visibilityBtn.setForeground(COLOR_PANEL_BG);
    visibilityBtn.setFocusPainted(false);
    visibilityBtn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    visibilityBtn.setOpaque(true);
    visibilityBtn.addMouseListener(makeHoverListener(visibilityBtn));
    visibilityBtn.setToolTipText(isVisible ? "Hide layer" : "Show layer");
    visibilityBtn.addActionListener(e -> toggleLayerVisibility());
    toolRibbonPanel.add(visibilityBtn);

    // Tool icons
    String[] tools = getToolsForLayer(currentLayer);
    for (String toolId : tools) {
      JButton toolBtn = new JButton(getToolIcon(toolId));
      toolBtn.setFont(new Font("Arial", Font.BOLD, 12));
      toolBtn.setPreferredSize(new Dimension(36, 36));
      boolean isSelected = toolId.equals(currentToolId);
      toolBtn.setBackground(isSelected ? COLOR_ACCENT_GREEN : COLOR_BUTTON_DEFAULT);
      toolBtn.setForeground(COLOR_PANEL_BG);
      toolBtn.setFocusPainted(false);
      toolBtn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      toolBtn.setOpaque(true);
      toolBtn.addMouseListener(makeHoverListener(toolBtn));
      toolBtn.setToolTipText(getToolName(toolId));
      final String fToolId = toolId;
      toolBtn.addActionListener(e -> switchToTool(currentLayer, fToolId));
      toolRibbonPanel.add(toolBtn);
    }

    toolRibbonPanel.revalidate();
    toolRibbonPanel.repaint();
  }

  private String[] getToolsForLayer(int layer) {
    if (layer == LAYER_BKGD) return new String[]{"color", "image", "clear"};
    if (layer == LAYER_VECFD) return new String[]{"paint", "erase", "overlay", "clean", "random", "reset"};
    if (layer == LAYER_BOTFD) return new String[]{"place", "random", "auto", "clear", "export"};
    return new String[]{};
  }

  private String getToolIcon(String toolId) {
    if ("color".equals(toolId)) return "●";
    if ("image".equals(toolId)) return "⬜";
    if ("clear".equals(toolId)) return "✕";
    if ("paint".equals(toolId)) return "🖌";
    if ("erase".equals(toolId)) return "🗑";
    if ("overlay".equals(toolId)) return "🪣";
    if ("clean".equals(toolId)) return "🧹";
    if ("random".equals(toolId)) return "🎲";
    if ("reset".equals(toolId)) return "🔄";
    if ("place".equals(toolId)) return "📍";
    if ("auto".equals(toolId)) return "▶";
    if ("export".equals(toolId)) return "📤";
    return "?";
  }

  private String getToolName(String toolId) {
    if ("color".equals(toolId)) return "Color";
    if ("image".equals(toolId)) return "Image";
    if ("clear".equals(toolId)) return "Clear";
    if ("paint".equals(toolId)) return "Paint";
    if ("erase".equals(toolId)) return "Erase";
    if ("overlay".equals(toolId)) return "Overlay";
    if ("clean".equals(toolId)) return "Clean";
    if ("random".equals(toolId)) return "Random";
    if ("reset".equals(toolId)) return "Reset";
    if ("place".equals(toolId)) return "Place";
    if ("auto".equals(toolId)) return "Auto";
    if ("export".equals(toolId)) return "Export";
    return toolId;
  }

  private void toggleLayerVisibility() {
    layerVisibility[currentLayer] = !layerVisibility[currentLayer];

    if (currentLayer == LAYER_BKGD) {
      if (listener != null) listener.onBackgroundToggle(layerVisibility[currentLayer]);
    } else if (currentLayer == LAYER_VECFD) {
      if (listener != null) listener.onVectorFieldToggle(layerVisibility[currentLayer]);
    } else if (currentLayer == LAYER_BOTFD) {
      if (listener != null) listener.onBotfieldToggle(layerVisibility[currentLayer]);
    }

    rebuildToolRibbon();
  }

  private void switchToTool(int layer, String toolId) {
    currentLayer = layer;
    currentToolId = toolId;
    rebuildToolRibbon();
    updateSettingsPanel();
  }

  // ────────────────────────────────────────────────────────────────────
  // Level 3: Settings Panel
  // ────────────────────────────────────────────────────────────────────

  private void buildSettingsPanel() {
    settingsPanelContainer = new JPanel();
    settingsPanelContainer.setLayout(new BoxLayout(settingsPanelContainer, BoxLayout.Y_AXIS));
    settingsPanelContainer.setBackground(COLOR_PANEL_BG);

    settingsScroll = new JScrollPane(settingsPanelContainer,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    settingsScroll.setBorder(BorderFactory.createEmptyBorder());
    settingsScroll.getVerticalScrollBar().setUnitIncrement(12);
  }

  private void updateSettingsPanel() {
    settingsPanelContainer.removeAll();

    // Transparency slider
    final JLabel[] transparencyLabel = new JLabel[1];
    transparencyLabel[0] = new JLabel("Transparency: 100%");
    transparencyLabel[0].setForeground(COLOR_TEXT_PRIMARY);
    transparencyLabel[0].setFont(new Font("Arial", Font.BOLD, 10));
    transparencyLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
    
    JSlider transparencySlider = new JSlider(0, 100, 100);
    transparencySlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
    transparencySlider.setBackground(COLOR_PANEL_BG);
    transparencySlider.setForeground(COLOR_ACCENT_GREEN);
    transparencySlider.addChangeListener(e -> {
      transparencyLabel[0].setText(String.format("Transparency: %d%%", transparencySlider.getValue()));
      onTransparencyChanged(transparencySlider.getValue() / 100.0f);
    });
    
    settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
    settingsPanelContainer.add(transparencyLabel[0]);
    settingsPanelContainer.add(transparencySlider);
    settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

    buildToolSpecificSettings();

    settingsPanelContainer.add(Box.createVerticalGlue());
    settingsPanelContainer.revalidate();
    settingsPanelContainer.repaint();
  }

  private void buildToolSpecificSettings() {
    if (currentLayer == LAYER_BKGD) {
      buildBackgroundToolSettings(currentToolId);
    } else if (currentLayer == LAYER_VECFD) {
      buildVectorFieldToolSettings(currentToolId);
    } else if (currentLayer == LAYER_BOTFD) {
      buildBotFieldToolSettings(currentToolId);
    }
  }

  private void buildBackgroundToolSettings(String toolId) {
    if ("color".equals(toolId)) {
      JLabel header = new JLabel("Color Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton colorPickerBtn = createButton("Color Picker...", 140);
      colorPickerBtn.addActionListener(e -> {
        if (listener != null) listener.onBackgroundColorPicker();
      });
      settingsPanelContainer.add(colorPickerBtn);
    } else if ("image".equals(toolId)) {
      JLabel header = new JLabel("Image Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton loadBtn = createButton("Load Image...", 140);
      loadBtn.addActionListener(e -> {
        if (listener != null) listener.onLoadBackgroundImage();
      });
      settingsPanelContainer.add(loadBtn);
    } else if ("clear".equals(toolId)) {
      JLabel header = new JLabel("Clear Background");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton clearBtn = createButton("Clear Image", 140);
      clearBtn.addActionListener(e -> {
        if (listener != null) listener.onClearBackgroundImage();
      });
      settingsPanelContainer.add(clearBtn);
    }
  }

  private void buildVectorFieldToolSettings(String toolId) {
    if ("paint".equals(toolId)) {
      JLabel header = new JLabel("Paint Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] sizeLabel = new JLabel[1];
      sizeLabel[0] = new JLabel("Size: 30");
      sizeLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      sizeLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider sizeSlider = createSlider(5, 200, 30);
      sizeSlider.addChangeListener(e -> {
        float v = sizeSlider.getValue();
        sizeLabel[0].setText(String.format("Size: %.0f", v));
        if (listener != null) listener.onBrushSizeChanged(v);
      });
      settingsPanelContainer.add(sizeLabel[0]);
      settingsPanelContainer.add(sizeSlider);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] hardnessLabel = new JLabel[1];
      hardnessLabel[0] = new JLabel("Hardness: 0.50");
      hardnessLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      hardnessLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider hardnessSlider = createSlider(0, 100, 50);
      hardnessSlider.addChangeListener(e -> {
        float v = hardnessSlider.getValue() / 100.0f;
        hardnessLabel[0].setText(String.format("Hardness: %.2f", v));
        if (listener != null) listener.onBrushHardnessChanged(v);
      });
      settingsPanelContainer.add(hardnessLabel[0]);
      settingsPanelContainer.add(hardnessSlider);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] strengthLabel = new JLabel[1];
      strengthLabel[0] = new JLabel("Strength: 2.0");
      strengthLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      strengthLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider strengthSlider = createSlider(1, 50, 20);
      strengthSlider.addChangeListener(e -> {
        float v = strengthSlider.getValue() / 10.0f;
        strengthLabel[0].setText(String.format("Strength: %.1f", v));
        if (listener != null) listener.onBrushStrengthChanged(v);
      });
      settingsPanelContainer.add(strengthLabel[0]);
      settingsPanelContainer.add(strengthSlider);
    } else if ("erase".equals(toolId)) {
      JLabel header = new JLabel("Erase Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] sizeLabel = new JLabel[1];
      sizeLabel[0] = new JLabel("Size: 30");
      sizeLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      sizeLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider sizeSlider = createSlider(5, 200, 30);
      sizeSlider.addChangeListener(e -> {
        float v = sizeSlider.getValue();
        sizeLabel[0].setText(String.format("Size: %.0f", v));
        if (listener != null) listener.onBrushSizeChanged(v);
      });
      settingsPanelContainer.add(sizeLabel[0]);
      settingsPanelContainer.add(sizeSlider);
    } else if ("overlay".equals(toolId)) {
      JLabel header = new JLabel("Overlay Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      // Falloff buttons
      JLabel falloffLabel = new JLabel("Falloff:");
      falloffLabel.setForeground(COLOR_TEXT_PRIMARY);
      falloffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(falloffLabel);
      JPanel falloffPanel = new JPanel();
      falloffPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
      falloffPanel.setBackground(COLOR_PANEL_BG);
      for (String falloff : new String[]{"Linear", "Gaussian", "Hard"}) {
        JButton btn = createButton(falloff, 45);
        falloffPanel.add(btn);
      }
      settingsPanelContainer.add(falloffPanel);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      // Size, Hardness, Strength
      final JLabel[] sizeLabel = new JLabel[1];
      sizeLabel[0] = new JLabel("Size: 30");
      sizeLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      sizeLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider sizeSlider = createSlider(5, 200, 30);
      sizeSlider.addChangeListener(e -> {
        float v = sizeSlider.getValue();
        sizeLabel[0].setText(String.format("Size: %.0f", v));
        if (listener != null) listener.onBrushSizeChanged(v);
      });
      settingsPanelContainer.add(sizeLabel[0]);
      settingsPanelContainer.add(sizeSlider);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] hardnessLabel = new JLabel[1];
      hardnessLabel[0] = new JLabel("Hardness: 0.50");
      hardnessLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      hardnessLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider hardnessSlider = createSlider(0, 100, 50);
      hardnessSlider.addChangeListener(e -> {
        float v = hardnessSlider.getValue() / 100.0f;
        hardnessLabel[0].setText(String.format("Hardness: %.2f", v));
        if (listener != null) listener.onBrushHardnessChanged(v);
      });
      settingsPanelContainer.add(hardnessLabel[0]);
      settingsPanelContainer.add(hardnessSlider);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] strengthLabel = new JLabel[1];
      strengthLabel[0] = new JLabel("Strength: 2.0");
      strengthLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      strengthLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider strengthSlider = createSlider(1, 50, 20);
      strengthSlider.addChangeListener(e -> {
        float v = strengthSlider.getValue() / 10.0f;
        strengthLabel[0].setText(String.format("Strength: %.1f", v));
        if (listener != null) listener.onBrushStrengthChanged(v);
      });
      settingsPanelContainer.add(strengthLabel[0]);
      settingsPanelContainer.add(strengthSlider);
    } else if ("clean".equals(toolId)) {
      JLabel header = new JLabel("Clean Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      // Falloff buttons
      JLabel falloffLabel = new JLabel("Falloff:");
      falloffLabel.setForeground(COLOR_TEXT_PRIMARY);
      falloffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(falloffLabel);
      JPanel falloffPanel = new JPanel();
      falloffPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
      falloffPanel.setBackground(COLOR_PANEL_BG);
      for (String falloff : new String[]{"Linear", "Gaussian", "Hard"}) {
        JButton btn = createButton(falloff, 45);
        falloffPanel.add(btn);
      }
      settingsPanelContainer.add(falloffPanel);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      // Size, Hardness, Strength
      final JLabel[] sizeLabel = new JLabel[1];
      sizeLabel[0] = new JLabel("Size: 30");
      sizeLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      sizeLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider sizeSlider = createSlider(5, 200, 30);
      sizeSlider.addChangeListener(e -> {
        float v = sizeSlider.getValue();
        sizeLabel[0].setText(String.format("Size: %.0f", v));
        if (listener != null) listener.onBrushSizeChanged(v);
      });
      settingsPanelContainer.add(sizeLabel[0]);
      settingsPanelContainer.add(sizeSlider);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] hardnessLabel = new JLabel[1];
      hardnessLabel[0] = new JLabel("Hardness: 0.50");
      hardnessLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      hardnessLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider hardnessSlider = createSlider(0, 100, 50);
      hardnessSlider.addChangeListener(e -> {
        float v = hardnessSlider.getValue() / 100.0f;
        hardnessLabel[0].setText(String.format("Hardness: %.2f", v));
        if (listener != null) listener.onBrushHardnessChanged(v);
      });
      settingsPanelContainer.add(hardnessLabel[0]);
      settingsPanelContainer.add(hardnessSlider);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] strengthLabel = new JLabel[1];
      strengthLabel[0] = new JLabel("Strength: 2.0");
      strengthLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      strengthLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider strengthSlider = createSlider(1, 50, 20);
      strengthSlider.addChangeListener(e -> {
        float v = strengthSlider.getValue() / 10.0f;
        strengthLabel[0].setText(String.format("Strength: %.1f", v));
        if (listener != null) listener.onBrushStrengthChanged(v);
      });
      settingsPanelContainer.add(strengthLabel[0]);
      settingsPanelContainer.add(strengthSlider);
    } else if ("random".equals(toolId)) {
      JLabel header = new JLabel("Random Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      // Falloff buttons
      JLabel falloffLabel = new JLabel("Falloff:");
      falloffLabel.setForeground(COLOR_TEXT_PRIMARY);
      falloffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(falloffLabel);
      JPanel falloffPanel = new JPanel();
      falloffPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
      falloffPanel.setBackground(COLOR_PANEL_BG);
      for (String falloff : new String[]{"Linear", "Gaussian", "Hard"}) {
        JButton btn = createButton(falloff, 45);
        falloffPanel.add(btn);
      }
      settingsPanelContainer.add(falloffPanel);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      // Size, Hardness, Strength
      final JLabel[] sizeLabel = new JLabel[1];
      sizeLabel[0] = new JLabel("Size: 30");
      sizeLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      sizeLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider sizeSlider = createSlider(5, 200, 30);
      sizeSlider.addChangeListener(e -> {
        float v = sizeSlider.getValue();
        sizeLabel[0].setText(String.format("Size: %.0f", v));
        if (listener != null) listener.onBrushSizeChanged(v);
      });
      settingsPanelContainer.add(sizeLabel[0]);
      settingsPanelContainer.add(sizeSlider);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] hardnessLabel = new JLabel[1];
      hardnessLabel[0] = new JLabel("Hardness: 0.50");
      hardnessLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      hardnessLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider hardnessSlider = createSlider(0, 100, 50);
      hardnessSlider.addChangeListener(e -> {
        float v = hardnessSlider.getValue() / 100.0f;
        hardnessLabel[0].setText(String.format("Hardness: %.2f", v));
        if (listener != null) listener.onBrushHardnessChanged(v);
      });
      settingsPanelContainer.add(hardnessLabel[0]);
      settingsPanelContainer.add(hardnessSlider);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] strengthLabel = new JLabel[1];
      strengthLabel[0] = new JLabel("Strength: 2.0");
      strengthLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      strengthLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider strengthSlider = createSlider(1, 50, 20);
      strengthSlider.addChangeListener(e -> {
        float v = strengthSlider.getValue() / 10.0f;
        strengthLabel[0].setText(String.format("Strength: %.1f", v));
        if (listener != null) listener.onBrushStrengthChanged(v);
      });
      settingsPanelContainer.add(strengthLabel[0]);
      settingsPanelContainer.add(strengthSlider);
    } else if ("reset".equals(toolId)) {
      JLabel header = new JLabel("Reset Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton actionBtn = createButton("Reset Field", 140);
      actionBtn.addActionListener(e -> {
        if (listener != null) listener.onResetVectorField();
      });
      settingsPanelContainer.add(actionBtn);
    }
  }

  private void buildBotFieldToolSettings(String toolId) {
    if ("place".equals(toolId)) {
      JLabel header = new JLabel("Place Bot Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton placeBtn = createButton("Place One Bot", 140);
      placeBtn.addActionListener(e -> {
        if (listener != null) listener.onSpawnBot();
      });
      settingsPanelContainer.add(placeBtn);
    } else if ("random".equals(toolId)) {
      JLabel header = new JLabel("Randomize Bots");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      final JLabel[] numLabel = new JLabel[1];
      numLabel[0] = new JLabel("Count: 5");
      numLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      numLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider numSlider = createSlider(1, 100, 5);
      numSlider.addChangeListener(e -> {
        numLabel[0].setText("Count: " + numSlider.getValue());
        if (listener != null) listener.onBotNumberChanged(numSlider.getValue());
      });
      settingsPanelContainer.add(numLabel[0]);
      settingsPanelContainer.add(numSlider);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton randomBtn = createButton("Spawn Random", 140);
      randomBtn.addActionListener(e -> {
        if (listener != null) listener.onAutoSpawnToggle(true);
      });
      settingsPanelContainer.add(randomBtn);
    } else if ("auto".equals(toolId)) {
      JLabel header = new JLabel("Auto Spawn");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      simToggleBtn = createButton("▶ Start", 140);
      simToggleBtn.addActionListener(e -> {
        if (listener != null) listener.onSimulationToggle();
      });
      settingsPanelContainer.add(simToggleBtn);

      botCountLabel = new JLabel("Active: 0  |  Traces: 0");
      botCountLabel.setForeground(COLOR_TEXT_SECONDARY);
      botCountLabel.setFont(new Font("Arial", Font.PLAIN, 10));
      botCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
      settingsPanelContainer.add(botCountLabel);
    } else if ("clear".equals(toolId)) {
      JLabel header = new JLabel("Clear Control");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton clearBotsBtn = createButton("Clear Bots", 140);
      clearBotsBtn.addActionListener(e -> {
        if (listener != null) listener.onClearBots();
      });
      settingsPanelContainer.add(clearBotsBtn);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton clearTracesBtn = createButton("Clear Traces", 140);
      clearTracesBtn.addActionListener(e -> {
        if (listener != null) listener.onClearTraces();
      });
      settingsPanelContainer.add(clearTracesBtn);
    } else if ("export".equals(toolId)) {
      JLabel header = new JLabel("Export Data");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton exportBtn = createButton("Export State", 140);
      settingsPanelContainer.add(exportBtn);
    }
  }

  private void onTransparencyChanged(float alpha) {
    if (currentLayer == LAYER_BKGD) {
      if (listener != null) listener.onBackgroundTransparencyChanged(alpha);
    } else if (currentLayer == LAYER_VECFD) {
      if (listener != null) listener.onVectorFieldTransparencyChanged(alpha);
    } else if (currentLayer == LAYER_BOTFD) {
      if (listener != null) listener.onBotfieldTransparencyChanged(alpha);
    }
  }

  private JButton createButton(String text, int width) {
    JButton btn = new JButton(text);
    btn.setMaximumSize(new Dimension(width, 36));
    btn.setBackground(COLOR_BUTTON_DEFAULT);
    btn.setForeground(COLOR_PANEL_BG);
    btn.setFocusPainted(false);
    btn.setFont(new Font("Arial", Font.BOLD, 11));
    btn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    btn.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        btn.setBackground(COLOR_BUTTON_HOVER);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        btn.setBackground(COLOR_BUTTON_DEFAULT);
      }
    });
    return btn;
  }

  private JSlider createSlider(int min, int max, int value) {
    JSlider slider = new JSlider(min, max, value);
    slider.setMaximumSize(new Dimension(190, 28));
    slider.setBackground(COLOR_PANEL_BG);
    slider.setForeground(COLOR_ACCENT_GREEN);
    slider.setAlignmentX(Component.LEFT_ALIGNMENT);
    slider.setFocusable(false);
    return slider;
  }

  private MouseAdapter makeHoverListener(JButton btn) {
    return new MouseAdapter() {
      private final Color originalBg = btn.getBackground();

      @Override
      public void mouseEntered(MouseEvent e) {
        btn.setBackground(COLOR_BUTTON_HOVER);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        btn.setBackground(originalBg);
        btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    };
  }

  public void updateSimulationState(boolean running) {
    if (simToggleBtn != null) {
      SwingUtilities.invokeLater(() -> {
        simToggleBtn.setText(running ? "\u23F8 Pause" : "\u25B6 Play");
        simToggleBtn.setToolTipText(running ? "Pause simulation" : "Start simulation");
      });
    }
  }

  public void updateBotCount(int active, int dead) {
    if (botCountLabel != null) {
      SwingUtilities.invokeLater(() -> {
        botCountLabel.setText(String.format("Active: %d  |  Traces: %d", active, dead));
      });
    }
  }
}
