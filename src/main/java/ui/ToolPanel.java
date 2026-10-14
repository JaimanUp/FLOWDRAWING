package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.swing.plaf.basic.BasicSliderUI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
  private static final int LAYER_TAB_HEIGHT = 48;
  private static final int TOOL_BUTTON_GAP = 8;
  private static final int TOOL_RIBBON_INSET = 8;
  private static final int TOOL_RIBBON_COLUMNS = 4;
  private static final int TOOL_RIBBON_ROWS = 2;
  private static final int TOOL_RIBBON_HEIGHT = TOOL_RIBBON_ROWS * BUTTON_HEIGHT
    + TOOL_BUTTON_GAP + 2 * TOOL_RIBBON_INSET;
  private static final Map<String, Icon> TOOLBAR_ICONS = new HashMap<>();

  // ── Simplified Color Palette ─────────────────────────────────────────
  private static final Color COLOR_PANEL_BG = new Color(37, 43, 40);
  private static final Color COLOR_TEXT_PRIMARY = new Color(231, 238, 233);
  private static final Color COLOR_TEXT_SECONDARY = new Color(175, 189, 179);
  private static final Color COLOR_ACCENT_GREEN = new Color(188, 220, 166);
  private static final Color COLOR_TOOL_SELECTED = new Color(157, 191, 139);
  private static final Color COLOR_LAYER_DEFAULT = new Color(70, 86, 75);
  private static final Color COLOR_LAYER_HOVER = new Color(91, 111, 97);
  private static final Color COLOR_BUTTON_DEFAULT = new Color(133, 164, 139);
  private static final Color COLOR_BUTTON_HOVER = new Color(207, 231, 188);
  private static final Color COLOR_INPUT_BG = new Color(53, 64, 57);
  private static final Color COLOR_INPUT_BORDER = new Color(102, 126, 109);
  private static final Color COLOR_SLIDER_TRACK = new Color(76, 91, 81);

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
    default void onFieldMagnitudeScaleChanged(float scale) {}
    default void onVisualizationModeChanged(String mode) {}
    default void onResetVectorField() {}
    default void onRandomVectorField() {}
    default void onCleanVectorField() {}
    default void onFieldDrawingEnabled(boolean enabled) {}
    default void onBotPlacementEnabled(boolean enabled) {}

    // Sketch
    default void onSketchToggle(boolean show) {}
    default void onSketchTransparencyChanged(float alpha) {}
    default void onBrushModeChanged(boolean isBrush) {}
    default void onBrushSizeChanged(float size) {}
    default void onBrushHardnessChanged(float hardness) {}
    default void onBrushStrengthChanged(float strength) {}
    default void onBrushFalloffChanged(String falloffType) {}
    default void onClearSketch() {}

    // Botfield
    default void onBotfieldToggle(boolean show) {}
    default void onBotfieldTransparencyChanged(float alpha) {}
    default void onBotTraceFadeToggle(boolean enabled) {}
    default void onBotLifeChanged(float life) {}
    default void onBotRadarChanged(float radar) {}
    default void onBotSpeedChanged(float speed) {}
    default void onBotDriftInfluenceChanged(float influence) {}
      default void onBotDriftModeChanged(String mode) {}
    default void onBotFieldInfluenceChanged(float influence) {}
    default void onBotRepulsionInfluenceChanged(float influence) {}
    default void onBotRepulsionRadiusChanged(float radius) {}
    default void onSpawnBot() {}
    default void onSpawnRandomBots() {}
    default void onAutoSpawnToggle(boolean enabled) {}
    default void onBotNumberChanged(int number) {}
    default void onBotSpawnRateChanged(int botsPerSecond) {}
    default void onSimulationToggle() {}
    default void onSimulationReset() {}
    default void onClearBots() {}
    default void onClearTraces() {}
    default void onBotLuminanceDecayToggle(boolean enabled) {}
    default void onBotRepresentationChanged(String representation) {}
    default void onBotColorModeChanged(String mode) {}
        default void onBotColorPicker() {}
    default void onExportRequested(String format) {}
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
  private final float[] layerTransparency = {1.0f, 1.0f, 1.0f};
  private final Map<String, Integer> sharedSliderValues = new HashMap<>();
  private final Map<String, Object> sharedControlValues = new HashMap<>();
  private float fieldMagnitudeScale = 1.0f;

  // UI Components
  private JPanel layerTabsPanel;
  private JPanel layerGeneralControlsPanel;
  private JPanel layerActionButtonsPanel;
  private JPanel toolRibbonPanel;
  private JPanel settingsPanelContainer;
  private JScrollPane settingsScroll;
  private final JToggleButton[] layerButtons = new JToggleButton[3];
  private final ButtonGroup layerButtonGroup = new ButtonGroup();
  private ButtonGroup toolButtonGroup = new ButtonGroup();
  private JToggleButton layerVisibilityButton;
  private JSlider layerTransparencySlider;
  private JButton backgroundClearButton;
  private JButton fieldResetButton;
  private JButton botsClearButton;
  private JButton tracesClearButton;
  private boolean updatingLayerControls;

  // Public API
  public JLabel botCountLabel;
  public JButton simToggleBtn;

  public ToolPanel(ToolListener listener) {
    this.listener = listener;
    setLayout(new BorderLayout(0, 0));
    setPreferredSize(new Dimension(PANEL_WIDTH, 0));
    setMinimumSize(new Dimension(PANEL_MIN_WIDTH, 0));
    setMaximumSize(new Dimension(PANEL_WIDTH, Integer.MAX_VALUE));
    setBackground(COLOR_PANEL_BG);
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    buildLayerTabs();
    buildLayerGeneralControls();
    buildToolRibbon();
    buildSettingsPanel();

    JPanel northWrapper = new JPanel();
    northWrapper.setLayout(new BoxLayout(northWrapper, BoxLayout.Y_AXIS));
    northWrapper.setBackground(COLOR_PANEL_BG);
    northWrapper.add(layerTabsPanel);
    northWrapper.add(layerGeneralControlsPanel);
    northWrapper.add(toolRibbonPanel);

    add(northWrapper, BorderLayout.NORTH);
    add(settingsScroll, BorderLayout.CENTER);

    rebuildToolRibbon();
    switchToTool(LAYER_BKGD, "color");
  }

  // ────────────────────────────────────────────────────────────────────
  // Level 1: Layer Tabs
  // ────────────────────────────────────────────────────────────────────

  private void buildLayerTabs() {
    layerTabsPanel = new JPanel(new GridLayout(1, LAYER_NAMES.length, 8, 0));
    layerTabsPanel.setBackground(COLOR_PANEL_BG);
    layerTabsPanel.setPreferredSize(new Dimension(PANEL_WIDTH, LAYER_TAB_HEIGHT + 16));
    layerTabsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    for (int index = 0; index < LAYER_NAMES.length; index++) {
      final int layerIndex = index;
      JToggleButton layerButton = new JToggleButton(LAYER_NAMES[index]);
      layerButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
      layerButton.setPreferredSize(new Dimension(64, LAYER_TAB_HEIGHT));
      layerButton.setSelected(index == currentLayer);
      layerButton.setBackground(layerButton.isSelected()
        ? COLOR_ACCENT_GREEN : COLOR_LAYER_DEFAULT);
      layerButton.setForeground(layerButton.isSelected()
        ? COLOR_PANEL_BG : COLOR_TEXT_PRIMARY);
      layerButton.setFocusPainted(true);
      layerButton.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
      layerButton.setOpaque(true);
      layerButton.getAccessibleContext().setAccessibleName(getLayerAccessibleName(index));
      layerButton.getAccessibleContext().setAccessibleDescription(
        layerButton.isSelected() ? "Selected layer" : "Select this layer");
      layerButton.setToolTipText(getLayerAccessibleName(index));
      layerButton.addActionListener(event -> switchLayer(layerIndex));
      layerButton.addMouseListener(makeLayerTabHoverListener(layerButton));
      layerButtonGroup.add(layerButton);
      layerButtons[index] = layerButton;
      layerTabsPanel.add(layerButton);
    }
  }

  private String getLayerAccessibleName(int layer) {
    if (layer == LAYER_BKGD) return "Background layer";
    if (layer == LAYER_VECFD) return "Vector field layer";
    return "Bot field layer";
  }

  private void switchLayer(int layer) {
    currentLayer = layer;
    currentToolId = getDefaultToolForLayer(layer);
    updateLayerTabsDisplay();
    updateLayerGeneralControls();
    rebuildToolRibbon();
    switchToTool(layer, currentToolId);
  }

  private void updateLayerTabsDisplay() {
    for (int index = 0; index < layerButtons.length; index++) {
      boolean isSelected = index == currentLayer;
      layerButtons[index].setSelected(isSelected);
      layerButtons[index].setBackground(isSelected ? COLOR_ACCENT_GREEN : COLOR_LAYER_DEFAULT);
      layerButtons[index].setForeground(isSelected ? COLOR_PANEL_BG : COLOR_TEXT_PRIMARY);
      layerButtons[index].getAccessibleContext().setAccessibleDescription(
        isSelected ? "Selected layer" : "Select this layer");
    }
    layerTabsPanel.repaint();
  }

  private String getDefaultToolForLayer(int layer) {
    if (layer == LAYER_BKGD) return "color";
    if (layer == LAYER_VECFD) return "paint";
    if (layer == LAYER_BOTFD) return "place";
    return "color";
  }

  private void buildToolRibbon() {
    toolRibbonPanel = new JPanel(new GridLayout(
      TOOL_RIBBON_ROWS, TOOL_RIBBON_COLUMNS, TOOL_BUTTON_GAP, TOOL_BUTTON_GAP));
    toolRibbonPanel.setBackground(COLOR_PANEL_BG);
    toolRibbonPanel.setBorder(BorderFactory.createEmptyBorder(
      TOOL_RIBBON_INSET, TOOL_RIBBON_INSET, TOOL_RIBBON_INSET, TOOL_RIBBON_INSET));
    toolRibbonPanel.setPreferredSize(new Dimension(PANEL_WIDTH, TOOL_RIBBON_HEIGHT));
    toolRibbonPanel.setMaximumSize(new Dimension(PANEL_WIDTH, TOOL_RIBBON_HEIGHT));
  }

  private void buildLayerGeneralControls() {
    layerGeneralControlsPanel = new JPanel(new GridBagLayout());
    layerGeneralControlsPanel.setBackground(COLOR_PANEL_BG);
    layerGeneralControlsPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
    layerGeneralControlsPanel.setPreferredSize(new Dimension(PANEL_WIDTH, 44));

    layerVisibilityButton = new JToggleButton();
    layerVisibilityButton.setPreferredSize(new Dimension(32, 30));
    layerVisibilityButton.setMinimumSize(new Dimension(32, 30));
    layerVisibilityButton.setForeground(COLOR_PANEL_BG);
    layerVisibilityButton.setFocusPainted(true);
    layerVisibilityButton.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    layerVisibilityButton.setOpaque(true);
    layerVisibilityButton.addMouseListener(makeHoverListener(layerVisibilityButton));
    layerVisibilityButton.addActionListener(event -> toggleLayerVisibility());

    layerTransparencySlider = createSlider(0, 100,
      Math.round(layerTransparency[currentLayer] * 100.0f));
    layerTransparencySlider.setPreferredSize(new Dimension(70, 30));
    layerTransparencySlider.getAccessibleContext().setAccessibleName("Layer transparency");
    layerTransparencySlider.addChangeListener(event -> {
      int value = layerTransparencySlider.getValue();
      if (updatingLayerControls) return;
      layerTransparency[currentLayer] = value / 100.0f;
      onTransparencyChanged(layerTransparency[currentLayer]);
    });

    backgroundClearButton = createGeneralActionButton("clear", "Clear background or image",
      () -> { if (listener != null) listener.onClearBackgroundImage(); });
    fieldResetButton = createGeneralActionButton("reset", "Reset vector field to zero",
      () -> { if (listener != null) listener.onResetVectorField(); });
    botsClearButton = createGeneralActionButton("clear", "Kill all bots (keep traces)",
      () -> { if (listener != null) listener.onClearBots(); });
    tracesClearButton = createGeneralActionButton("clean", "Clear all bot traces",
      () -> { if (listener != null) listener.onClearTraces(); });
    layerActionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
    layerActionButtonsPanel.setBackground(COLOR_PANEL_BG);
    layerActionButtonsPanel.setOpaque(true);
    layerActionButtonsPanel.setPreferredSize(new Dimension(68, 32));

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.insets = new Insets(0, 2, 0, 2);
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridx = 0;
    layerGeneralControlsPanel.add(layerVisibilityButton, constraints);
    constraints.gridx = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    layerGeneralControlsPanel.add(layerTransparencySlider, constraints);
    constraints.gridx = 2;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    layerGeneralControlsPanel.add(layerActionButtonsPanel, constraints);

    updateLayerGeneralControls();
  }

  private JButton createGeneralActionButton(String iconName, String accessibleName,
                                            Runnable action) {
    JButton button = new JButton(loadToolbarIcon(iconName));
    button.setPreferredSize(new Dimension(30, 30));
    button.setMinimumSize(new Dimension(30, 30));
    button.setBackground(COLOR_BUTTON_DEFAULT);
    button.setForeground(COLOR_PANEL_BG);
    button.setFocusPainted(true);
    button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    button.setOpaque(true);
    button.setToolTipText(accessibleName);
    button.getAccessibleContext().setAccessibleName(accessibleName);
    button.addMouseListener(makeHoverListener(button));
    button.addActionListener(event -> action.run());
    return button;
  }

  private void updateLayerGeneralControls() {
    if (layerVisibilityButton == null || layerTransparencySlider == null) return;
    updatingLayerControls = true;
    boolean isVisible = layerVisibility[currentLayer];
    layerVisibilityButton.setSelected(isVisible);
    layerVisibilityButton.setIcon(loadToolbarIcon(isVisible ? "visibility" : "visibility-off"));
    layerVisibilityButton.setBackground(getToolButtonBackground(isVisible));
    layerVisibilityButton.setToolTipText((isVisible ? "Hide " : "Show ")
      + getLayerAccessibleName(currentLayer));
    layerVisibilityButton.getAccessibleContext().setAccessibleName(
      getLayerAccessibleName(currentLayer) + " visibility");
    layerVisibilityButton.getAccessibleContext().setAccessibleDescription(
      isVisible ? "Layer is visible" : "Layer is hidden");
    layerTransparencySlider.getAccessibleContext().setAccessibleName(
      getLayerAccessibleName(currentLayer) + " transparency");
    layerTransparencySlider.setToolTipText("Adjust "
      + getLayerAccessibleName(currentLayer).toLowerCase() + " transparency");
    layerTransparencySlider.setValue(Math.round(layerTransparency[currentLayer] * 100.0f));
    updatingLayerControls = false;
    layerActionButtonsPanel.removeAll();
    if (currentLayer == LAYER_BKGD) {
      layerActionButtonsPanel.add(backgroundClearButton);
    } else if (currentLayer == LAYER_VECFD) {
      layerActionButtonsPanel.add(fieldResetButton);
    } else if (currentLayer == LAYER_BOTFD) {
      layerActionButtonsPanel.add(botsClearButton);
      layerActionButtonsPanel.add(tracesClearButton);
    }
    layerActionButtonsPanel.revalidate();
    layerActionButtonsPanel.repaint();
    layerGeneralControlsPanel.revalidate();
  }

  private void rebuildToolRibbon() {
    toolRibbonPanel.removeAll();
    toolButtonGroup = new ButtonGroup();

    String[] tools = getToolsForLayer(currentLayer);
    for (String toolId : tools) {
      JToggleButton toolButton = new JToggleButton(getToolIcon(toolId));
      boolean isSelected = toolId.equals(currentToolId);
      toolButton.setFont(new Font("Arial", Font.BOLD, 12));
      toolButton.setPreferredSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
      toolButton.setSelected(isSelected);
      toolButton.setBackground(getToolButtonBackground(isSelected));
      toolButton.setForeground(COLOR_PANEL_BG);
      toolButton.setFocusPainted(true);
      toolButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      toolButton.setOpaque(true);
      toolButton.putClientProperty("toolId", toolId);
      toolButton.getAccessibleContext().setAccessibleName(getToolName(toolId));
      toolButton.getAccessibleContext().setAccessibleDescription(
        isSelected ? "Selected tool" : "Select this tool");
      toolButton.addMouseListener(makeHoverListener(toolButton));
      toolButton.setToolTipText(getToolName(toolId));
      toolButton.addActionListener(event -> switchToTool(currentLayer, toolId));
      toolButtonGroup.add(toolButton);
      toolRibbonPanel.add(toolButton);
    }

    while (toolRibbonPanel.getComponentCount() < TOOL_RIBBON_ROWS * TOOL_RIBBON_COLUMNS) {
      JPanel emptyCell = new JPanel();
      emptyCell.setOpaque(false);
      emptyCell.setFocusable(false);
      toolRibbonPanel.add(emptyCell);
    }
    toolRibbonPanel.revalidate();
    toolRibbonPanel.repaint();
  }

  private String[] getToolsForLayer(int layer) {
    if (layer == LAYER_BKGD) return new String[]{"color", "image"};
    if (layer == LAYER_VECFD) return new String[]{"paint", "erase", "actions"};
    if (layer == LAYER_BOTFD) return new String[]{"place", "random", "auto", "export"};
    return new String[]{};
  }

  private Icon getToolIcon(String toolId) {
    if ("color".equals(toolId)) return loadToolbarIcon("color");
    if ("image".equals(toolId)) return loadToolbarIcon("image");
    if ("paint".equals(toolId)) return loadToolbarIcon("paint");
    if ("erase".equals(toolId)) return loadToolbarIcon("erase");
    if ("actions".equals(toolId)) return loadToolbarIcon("clean");
    if ("random".equals(toolId)) return loadToolbarIcon("random");
    if ("place".equals(toolId)) return loadToolbarIcon("place");
    if ("auto".equals(toolId)) return loadToolbarIcon("auto");
    if ("export".equals(toolId)) return loadToolbarIcon("export");
    return loadToolbarIcon("generic");
  }

  private static synchronized Icon loadToolbarIcon(String assetName) {
    Icon cached = TOOLBAR_ICONS.get(assetName);
    if (cached != null) return cached;
    Icon icon = readToolbarIcon(assetName);
    if (icon == null && !"generic".equals(assetName)) icon = readToolbarIcon("generic");
    if (icon == null) icon = new SvgToolIcon();
    TOOLBAR_ICONS.put(assetName, icon);
    return icon;
  }

  private static Icon readToolbarIcon(String assetName) {
    String resource = "/icons/" + assetName + ".svg";
    try (InputStream stream = ToolPanel.class.getResourceAsStream(resource)) {
      if (stream != null) return new SvgToolIcon(stream);
    } catch (Exception ignored) {
      // Invalid or missing assets use the generic vector icon.
    }
    return null;
  }

  private static final class SvgToolIcon implements Icon {
    private static final int ICON_SIZE = 20;
    private static final Color ICON_COLOR = new Color(25, 61, 45);
    private final List<Shape> shapes = new ArrayList<>();

    SvgToolIcon() {}

    SvgToolIcon(InputStream stream) throws Exception {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setExpandEntityReferences(false);
      NodeList elements = factory.newDocumentBuilder().parse(stream).getElementsByTagName("*");
      for (int index = 0; index < elements.getLength(); index++) {
        Node node = elements.item(index);
        if (node instanceof Element) {
          Shape shape = createShape((Element) node);
          if (shape != null) shapes.add(shape);
        }
      }
      if (shapes.isEmpty()) throw new IllegalArgumentException("SVG has no supported shapes");
    }

    private Shape createShape(Element element) {
      String tag = element.getTagName();
      if ("circle".equals(tag)) {
        double radius = number(element, "r");
        return new Ellipse2D.Double(number(element, "cx") - radius,
          number(element, "cy") - radius, radius * 2, radius * 2);
      }
      if ("ellipse".equals(tag)) {
        double radiusX = number(element, "rx");
        double radiusY = number(element, "ry");
        return new Ellipse2D.Double(number(element, "cx") - radiusX,
          number(element, "cy") - radiusY, radiusX * 2, radiusY * 2);
      }
      if ("rect".equals(tag)) {
        return new Rectangle2D.Double(number(element, "x"), number(element, "y"),
          number(element, "width"), number(element, "height"));
      }
      if ("line".equals(tag)) {
        return new Line2D.Double(number(element, "x1"), number(element, "y1"),
          number(element, "x2"), number(element, "y2"));
      }
      if ("polyline".equals(tag) || "polygon".equals(tag)) {
        String[] coordinates = element.getAttribute("points").trim().split("[ ,]+");
        if (coordinates.length < 4) return null;
        Path2D path = new Path2D.Double();
        path.moveTo(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        for (int index = 2; index + 1 < coordinates.length; index += 2) {
          path.lineTo(Double.parseDouble(coordinates[index]),
            Double.parseDouble(coordinates[index + 1]));
        }
        if ("polygon".equals(tag)) path.closePath();
        return path;
      }
      return null;
    }

    private static double number(Element element, String attribute) {
      String value = element.getAttribute(attribute);
      return value.isEmpty() ? 0.0 : Double.parseDouble(value);
    }

    @Override
    public int getIconWidth() {
      return ICON_SIZE;
    }

    @Override
    public int getIconHeight() {
      return ICON_SIZE;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      Graphics2D graphics2D = (Graphics2D) graphics.create();
      graphics2D.translate(x, y);
      graphics2D.scale(ICON_SIZE / 24.0, ICON_SIZE / 24.0);
      graphics2D.setColor(ICON_COLOR);
      graphics2D.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      if (shapes.isEmpty()) {
        graphics2D.draw(new Ellipse2D.Double(4, 4, 16, 16));
        graphics2D.draw(new Line2D.Double(12, 8, 12, 12));
        graphics2D.fill(new Ellipse2D.Double(11.2, 15, 1.6, 1.6));
      } else {
        for (Shape shape : shapes) graphics2D.draw(shape);
      }
      graphics2D.dispose();
    }
  }

  private String getToolName(String toolId) {
    if ("color".equals(toolId)) return "Color";
    if ("image".equals(toolId)) return "Image";
    if ("paint".equals(toolId)) return "Paint";
    if ("erase".equals(toolId)) return "Erase";
    if ("actions".equals(toolId)) return "Field Actions";
    if ("random".equals(toolId)) return "Random";
    if ("place".equals(toolId)) return "Place";
    if ("auto".equals(toolId)) return "Auto";
    if ("export".equals(toolId)) return "Export";
    return toolId;
  }

  private void toggleLayerVisibility() {
    layerVisibility[currentLayer] = layerVisibilityButton.isSelected();
    if (currentLayer == LAYER_BKGD) {
      if (listener != null) listener.onBackgroundToggle(layerVisibility[currentLayer]);
    } else if (currentLayer == LAYER_VECFD) {
      if (listener != null) listener.onVectorFieldToggle(layerVisibility[currentLayer]);
    } else if (currentLayer == LAYER_BOTFD) {
      if (listener != null) listener.onBotfieldToggle(layerVisibility[currentLayer]);
    }
    updateLayerGeneralControls();
  }

  private void switchToTool(int layer, String toolId) {
    currentLayer = layer;
    currentToolId = toolId;
    updateLayerTabsDisplay();
    for (Component component : toolRibbonPanel.getComponents()) {
      if (component instanceof JToggleButton) {
        JToggleButton toolButton = (JToggleButton) component;
        String candidateToolId = (String) toolButton.getClientProperty("toolId");
        boolean isSelected = toolId.equals(candidateToolId);
        toolButton.setSelected(isSelected);
        toolButton.setBackground(getToolButtonBackground(isSelected));
        toolButton.getAccessibleContext().setAccessibleDescription(
          isSelected ? "Selected tool" : "Select this tool");
      }
    }
    if (listener != null) {
      boolean drawingTool = layer == LAYER_VECFD
        && ("paint".equals(toolId) || "erase".equals(toolId));
      listener.onFieldDrawingEnabled(drawingTool);
      listener.onBotPlacementEnabled(layer == LAYER_BOTFD && "place".equals(toolId));
    }
    if (layer == LAYER_VECFD && listener != null
      && ("paint".equals(toolId) || "erase".equals(toolId))) {
      listener.onBrushModeChanged(!"erase".equals(toolId));
    }
    updateSettingsPanel();
  }

  // ────────────────────────────────────────────────────────────────────
  // Level 3: Settings Panel
  // ────────────────────────────────────────────────────────────────────

  private void buildSettingsPanel() {
    settingsPanelContainer = new SettingsPanel();
    settingsPanelContainer.setLayout(new BoxLayout(settingsPanelContainer, BoxLayout.Y_AXIS));
    settingsPanelContainer.setBackground(COLOR_PANEL_BG);
    settingsPanelContainer.setBorder(BorderFactory.createEmptyBorder(
      SECTION_PADDING, TOOL_RIBBON_INSET, SECTION_PADDING, TOOL_RIBBON_INSET));

    settingsScroll = new JScrollPane(settingsPanelContainer,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    settingsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    settingsScroll.setWheelScrollingEnabled(true);
    settingsScroll.setBorder(BorderFactory.createEmptyBorder());
    settingsScroll.getVerticalScrollBar().setUnitIncrement(12);
  }

  private static final class SettingsPanel extends JPanel implements Scrollable {
    @Override
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      return BUTTON_HEIGHT / 2;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      return Math.max(BUTTON_HEIGHT, visibleRect.height - BUTTON_HEIGHT);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
      return false;
    }
  }

  private void updateSettingsPanel() {
    saveSharedControlValues(settingsPanelContainer);
    settingsPanelContainer.removeAll();

    if (currentLayer == LAYER_VECFD) {
      buildFieldMagnitudeScaleControl();
    }

    buildToolSpecificSettings();
    restoreSharedControlValues(settingsPanelContainer);
    normalizeSettingsMargins(settingsPanelContainer);

    settingsPanelContainer.add(Box.createVerticalGlue());
    settingsPanelContainer.revalidate();
    settingsPanelContainer.repaint();
  }

  private void saveSharedControlValues(Container container) {
    Component[] components = container.getComponents();
    for (int index = 0; index < components.length; index++) {
      Component component = components[index];
      if (component instanceof JSlider) {
        JSlider slider = (JSlider) component;
        String key = sliderSettingKey(slider, components, index);
        if (key != null) sharedSliderValues.put(key, slider.getValue());
      } else if (component instanceof JComboBox) {
        JComboBox<?> combo = (JComboBox<?>) component;
        String key = accessibleSettingKey(combo);
        if (key != null) sharedControlValues.put(key, combo.getSelectedItem());
      } else if (component instanceof JCheckBox) {
        JCheckBox checkbox = (JCheckBox) component;
        String key = accessibleSettingKey(checkbox);
        if (key != null) sharedControlValues.put(key, checkbox.isSelected());
      } else if (component instanceof JToggleButton
          && ((JToggleButton) component).getClientProperty("sharedSettingValue") != null) {
        JToggleButton button = (JToggleButton) component;
        if (button.isSelected()) {
          sharedControlValues.put(accessibleSettingKey(button),
            button.getClientProperty("sharedSettingValue"));
        }
      }
      if (component instanceof Container) saveSharedControlValues((Container) component);
    }
  }

  private void restoreSharedControlValues(Container container) {
    Component[] components = container.getComponents();
    for (int index = 0; index < components.length; index++) {
      Component component = components[index];
      if (component instanceof JSlider) {
        JSlider slider = (JSlider) component;
        String key = sliderSettingKey(slider, components, index);
        Integer value = key == null ? null : sharedSliderValues.get(key);
        if (value != null) slider.setValue(Math.max(slider.getMinimum(),
          Math.min(slider.getMaximum(), value)));
      } else if (component instanceof JComboBox) {
        JComboBox<?> combo = (JComboBox<?>) component;
        Object value = sharedControlValues.get(accessibleSettingKey(combo));
        if (value != null) combo.setSelectedItem(value);
      } else if (component instanceof JCheckBox) {
        JCheckBox checkbox = (JCheckBox) component;
        Object value = sharedControlValues.get(accessibleSettingKey(checkbox));
        if (value instanceof Boolean) checkbox.setSelected((Boolean) value);
      } else if (component instanceof JToggleButton
          && ((JToggleButton) component).getClientProperty("sharedSettingValue") != null) {
        JToggleButton button = (JToggleButton) component;
        Object selectedValue = sharedControlValues.get(accessibleSettingKey(button));
        boolean selected = button.getClientProperty("sharedSettingValue").equals(selectedValue)
          || (selectedValue == null && "Gaussian".equals(
            button.getClientProperty("sharedSettingValue")));
        button.setSelected(selected);
        button.setBackground(selected ? COLOR_TOOL_SELECTED : COLOR_BUTTON_DEFAULT);
      }
      if (component instanceof Container) restoreSharedControlValues((Container) component);
    }
  }

  private String sliderSettingKey(JSlider slider, Component[] siblings, int index) {
    for (int previous = index - 1; previous >= 0; previous--) {
      if (siblings[previous] instanceof JLabel) {
        String label = ((JLabel) siblings[previous]).getText();
        int separator = label.indexOf(':');
        if (separator >= 0) {
          String key = canonicalSettingKey(label.substring(0, separator));
          if ("transparency".equals(key)) return null;
          if (slider.getAccessibleContext().getAccessibleName() == null) {
            slider.getAccessibleContext().setAccessibleName(key);
          }
          return key;
        }
        break;
      }
    }
    return accessibleSettingKey(slider);
  }

  private String accessibleSettingKey(JComponent component) {
    String name = component.getAccessibleContext().getAccessibleName();
    if (name == null || name.trim().isEmpty()) {
      if (component instanceof AbstractButton) name = ((AbstractButton) component).getText();
      else return null;
    }
    return canonicalSettingKey(name);
  }

  private String canonicalSettingKey(String name) {
    String key = name.trim().toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();
    if ("count".equals(key) || "target bots".equals(key) || "random bot count".equals(key)) {
      return "bot count";
    }
    if ("sample distance".equals(key)) return "radar sample distance";
    return key;
  }

  private void normalizeSettingsMargins(Container container) {
    if (!(container.getLayout() instanceof BoxLayout)) return;
    for (Component component : container.getComponents()) {
      if (component instanceof JComponent) {
        JComponent control = (JComponent) component;
        control.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension maximum = control.getMaximumSize();
        control.setMaximumSize(new Dimension(Integer.MAX_VALUE, maximum.height));
      }
      if (component instanceof Container) normalizeSettingsMargins((Container) component);
    }
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

  private void buildFieldMagnitudeScaleControl() {
    JLabel scaleLabel = new JLabel(String.format("Field strength: %d%%", Math.round(fieldMagnitudeScale * 100.0f)));
    scaleLabel.setForeground(COLOR_TEXT_PRIMARY);
    scaleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JSlider scaleSlider = createSlider(0, 200, Math.round(fieldMagnitudeScale * 100.0f));
    scaleSlider.getAccessibleContext().setAccessibleName("Field strength");
    scaleSlider.setToolTipText("Scale vector-field influence from 0% to 200%");
    scaleLabel.setLabelFor(scaleSlider);
    scaleSlider.addChangeListener(event -> {
      fieldMagnitudeScale = scaleSlider.getValue() / 100.0f;
      scaleLabel.setText(String.format("Field strength: %d%%", scaleSlider.getValue()));
      if (listener != null) {
        listener.onFieldMagnitudeScaleChanged(fieldMagnitudeScale);
      }
    });

    settingsPanelContainer.add(scaleLabel);
    settingsPanelContainer.add(scaleSlider);
    settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
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
    }
  }

  private void addFalloffControls() {
    JLabel falloffLabel = new JLabel("Falloff:");
    falloffLabel.setForeground(COLOR_TEXT_PRIMARY);
    falloffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    settingsPanelContainer.add(falloffLabel);

    JPanel falloffPanel = new JPanel(new GridLayout(0, 2, TOOL_BUTTON_GAP, TOOL_BUTTON_GAP));
    falloffPanel.setBackground(COLOR_PANEL_BG);
    falloffPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    falloffPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
      BUTTON_HEIGHT * 2 + TOOL_BUTTON_GAP));
    ButtonGroup falloffGroup = new ButtonGroup();
    String selectedFalloff = (String) sharedControlValues.getOrDefault(
      "brush falloff", "Gaussian");
    String[] falloffs = {"Linear", "Gaussian", "Hard Edge"};
    for (int index = 0; index < falloffs.length; index++) {
      String falloff = falloffs[index];
      JToggleButton button = new JToggleButton(falloff);
      button.setPreferredSize(new Dimension(140, BUTTON_HEIGHT));
      button.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
      button.setSelected(falloff.equals(selectedFalloff));
      button.setBackground(button.isSelected() ? COLOR_TOOL_SELECTED : COLOR_BUTTON_DEFAULT);
      button.setForeground(COLOR_PANEL_BG);
      button.setFocusPainted(true);
      button.setOpaque(true);
      button.setAlignmentX(Component.LEFT_ALIGNMENT);
      button.getAccessibleContext().setAccessibleName("Brush falloff");
      button.putClientProperty("sharedSettingValue", falloff);
      falloffGroup.add(button);
      button.addActionListener(e -> {
        sharedControlValues.put("brush falloff", falloff);
        for (Component component : falloffPanel.getComponents()) {
          if (component instanceof JToggleButton) {
            component.setBackground(((JToggleButton) component).isSelected()
              ? COLOR_TOOL_SELECTED : COLOR_BUTTON_DEFAULT);
          }
        }
        if (listener != null) listener.onBrushFalloffChanged(falloff);
      });
      falloffPanel.add(button);
    }
    settingsPanelContainer.add(falloffPanel);
    settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
  }

  private void buildVectorFieldToolSettings(String toolId) {
    if ("actions".equals(toolId)) {
      JLabel header = new JLabel("Field Actions");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton randomizeButton = createButton("Randomize Whole Field", 180);
      randomizeButton.addActionListener(event -> {
        if (listener != null) listener.onRandomVectorField();
      });
      settingsPanelContainer.add(randomizeButton);

      JButton normalizeButton = createButton("Normalize Whole Field", 180);
      normalizeButton.addActionListener(event -> {
        if (listener != null) listener.onCleanVectorField();
      });
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
      settingsPanelContainer.add(normalizeButton);

      return;
    }

    if ("paint".equals(toolId)) {
      JLabel header = new JLabel("Paint Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      addFalloffControls();

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
      strengthLabel[0] = new JLabel("Strength: 1.0");
      strengthLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      strengthLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider strengthSlider = createSlider(5, 25, 10);
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

      addFalloffControls();

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
      strengthLabel[0] = new JLabel("Strength: 1.0");
      strengthLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      strengthLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider strengthSlider = createSlider(5, 25, 10);
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

      addFalloffControls();

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
      strengthLabel[0] = new JLabel("Strength: 1.0");
      strengthLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      strengthLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider strengthSlider = createSlider(5, 25, 10);
      strengthSlider.addChangeListener(e -> {
        float v = strengthSlider.getValue() / 10.0f;
        strengthLabel[0].setText(String.format("Strength: %.1f", v));
        if (listener != null) listener.onBrushStrengthChanged(v);
      });
      settingsPanelContainer.add(strengthLabel[0]);
      settingsPanelContainer.add(strengthSlider);

      JButton actionBtn = createButton("Normalize Field", 140);
      actionBtn.addActionListener(e -> {
        if (listener != null) listener.onCleanVectorField();
      });
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
      settingsPanelContainer.add(actionBtn);
    } else if ("random".equals(toolId)) {
      JLabel header = new JLabel("Random Tool");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      addFalloffControls();

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
      strengthLabel[0] = new JLabel("Strength: 1.0");
      strengthLabel[0].setForeground(COLOR_TEXT_PRIMARY);
      strengthLabel[0].setAlignmentX(Component.LEFT_ALIGNMENT);
      JSlider strengthSlider = createSlider(5, 25, 10);
      strengthSlider.addChangeListener(e -> {
        float v = strengthSlider.getValue() / 10.0f;
        strengthLabel[0].setText(String.format("Strength: %.1f", v));
        if (listener != null) listener.onBrushStrengthChanged(v);
      });
      settingsPanelContainer.add(strengthLabel[0]);
      settingsPanelContainer.add(strengthSlider);

      JButton actionBtn = createButton("Randomize Field", 140);
      actionBtn.addActionListener(e -> {
        if (listener != null) listener.onRandomVectorField();
      });
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
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

      JButton placeBtn = createButton("Spawn at Canvas Center", 180);
      placeBtn.addActionListener(e -> {
        if (listener != null) listener.onSpawnBot();
      });
      settingsPanelContainer.add(placeBtn);
      buildSharedBotSettings();
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
      JSlider numSlider = createSlider(1, 500, 50);
      numSlider.getAccessibleContext().setAccessibleName("Random bot count");
      numLabel[0].setLabelFor(numSlider);
      numSlider.addChangeListener(e -> {
        numLabel[0].setText("Count: " + numSlider.getValue());
        if (listener != null) listener.onBotNumberChanged(numSlider.getValue());
      });
      settingsPanelContainer.add(numLabel[0]);
      settingsPanelContainer.add(numSlider);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JButton randomBtn = createButton("Spawn Random", 140);
      randomBtn.addActionListener(e -> {
        if (listener != null) listener.onSpawnRandomBots();
      });
      settingsPanelContainer.add(randomBtn);
      buildSharedBotSettings();
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

      addBotSlider("Target bots", 1, 500, 50, 1.0,
        value -> { if (listener != null) listener.onBotNumberChanged((int) value); });
      addBotSlider("Spawn rate (bots/s)", 1, 60, 6, 1.0,
        value -> { if (listener != null) listener.onBotSpawnRateChanged((int) value); });
      addBotSlider("Life", 50, 1000, 300, 1.0,
        value -> { if (listener != null) listener.onBotLifeChanged((float) value); });
      addBotSlider("Radar radius", 1, 200, 50, 1.0,
        value -> { if (listener != null) listener.onBotRadarChanged((float) value); });
      addBotSlider("Speed", 1, 100, 20, 10.0,
        value -> { if (listener != null) listener.onBotSpeedChanged((float) value); });
      addBotSlider("Drift influence", 0, 100, 10, 100.0,
        value -> { if (listener != null) listener.onBotDriftInfluenceChanged((float) value); });
      addBotSlider("Field influence", 0, 100, 100, 100.0,
        value -> { if (listener != null) listener.onBotFieldInfluenceChanged((float) value); });
      addBotSlider("Repulsion influence", 0, 100, 0, 100.0,
        value -> { if (listener != null) listener.onBotRepulsionInfluenceChanged((float) value); });
      addBotSlider("Repulsion radius", 0, 200, 50, 1.0,
        value -> { if (listener != null) listener.onBotRepulsionRadiusChanged((float) value); });
      addBotSlider("Radar samples", 4, 32, 16, 1.0,
        value -> { if (listener != null) listener.onRadarSamplesChanged((int) value); });
      addBotSlider("Radar sample distance", 10, 100, 70, 100.0,
        value -> { if (listener != null) listener.onRadarSampleDistanceChanged((float) value); });
      addBotSlider("Center sample weight", 1, 50, 15, 10.0,
        value -> { if (listener != null) listener.onCenterSampleWeightChanged((float) value); });

      JLabel radarFalloffLabel = new JLabel("Radar falloff");
      radarFalloffLabel.setForeground(COLOR_TEXT_PRIMARY);
      radarFalloffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      JComboBox<String> radarFalloff = new JComboBox<>(new String[]{"Linear", "Gaussian", "Hard Edge"});
      styleComboBox(radarFalloff);
      radarFalloff.setSelectedItem("Gaussian");
      radarFalloff.setAlignmentX(Component.LEFT_ALIGNMENT);
      radarFalloff.getAccessibleContext().setAccessibleName("Radar falloff");
      radarFalloffLabel.setLabelFor(radarFalloff);
      radarFalloff.addActionListener(e -> {
        if (listener != null) {
          listener.onRadarSamplingFalloffChanged(((String) radarFalloff.getSelectedItem()).toLowerCase());
        }
      });
      settingsPanelContainer.add(radarFalloffLabel);
      settingsPanelContainer.add(radarFalloff);

      JCheckBox autoSpawn = new JCheckBox("Maintain target bot count");
      autoSpawn.setSelected(false);
      autoSpawn.setForeground(COLOR_TEXT_PRIMARY);
      autoSpawn.setBackground(COLOR_PANEL_BG);
      autoSpawn.setAlignmentX(Component.LEFT_ALIGNMENT);
      autoSpawn.setPreferredSize(new Dimension(190, BUTTON_HEIGHT));
      autoSpawn.getAccessibleContext().setAccessibleName("Maintain target bot count");
      autoSpawn.addActionListener(e -> {
        if (listener != null) listener.onAutoSpawnToggle(autoSpawn.isSelected());
      });
      settingsPanelContainer.add(autoSpawn);

      JCheckBox luminanceDecay = new JCheckBox("Life decay from background luminance");
      luminanceDecay.setText("<html>Life decay from background<br>luminance</html>");
      luminanceDecay.setForeground(COLOR_TEXT_PRIMARY);
      luminanceDecay.setBackground(COLOR_PANEL_BG);
      luminanceDecay.setAlignmentX(Component.LEFT_ALIGNMENT);
      luminanceDecay.setPreferredSize(new Dimension(190, BUTTON_HEIGHT));
      luminanceDecay.getAccessibleContext().setAccessibleName(
        "Life decay from background luminance");
      luminanceDecay.addActionListener(e -> {
        if (listener != null) listener.onBotLuminanceDecayToggle(luminanceDecay.isSelected());
      });
      settingsPanelContainer.add(luminanceDecay);

      JCheckBox traceFade = new JCheckBox("Fade traces");
      traceFade.setSelected(true);
      traceFade.setForeground(COLOR_TEXT_PRIMARY);
      traceFade.setBackground(COLOR_PANEL_BG);
      traceFade.setAlignmentX(Component.LEFT_ALIGNMENT);
      traceFade.setPreferredSize(new Dimension(190, BUTTON_HEIGHT));
      traceFade.getAccessibleContext().setAccessibleName("Fade traces");
      traceFade.addActionListener(e -> {
        if (listener != null) listener.onBotTraceFadeToggle(traceFade.isSelected());
      });
      settingsPanelContainer.add(traceFade);

      JLabel representationLabel = new JLabel("Bot representation");
      representationLabel.setForeground(COLOR_TEXT_PRIMARY);
      representationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      JComboBox<String> representationSelector = new JComboBox<>(
        new String[]{"Point", "Circle", "Triangle"});
      styleComboBox(representationSelector);
      representationSelector.setSelectedItem("Circle");
      representationSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
      representationSelector.getAccessibleContext().setAccessibleName("Bot representation");
      representationLabel.setLabelFor(representationSelector);
      representationSelector.addActionListener(e -> {
        if (listener != null) {
          listener.onBotRepresentationChanged(
            ((String) representationSelector.getSelectedItem()).toLowerCase());
        }
      });
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
      settingsPanelContainer.add(representationLabel);
      settingsPanelContainer.add(representationSelector);
      JLabel colorModeLabel = new JLabel("Trace color mode");
      colorModeLabel.setForeground(COLOR_TEXT_PRIMARY);
      colorModeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      JComboBox<String> colorModeSelector = new JComboBox<>(
        new String[]{"Constant", "Life opacity", "Cyclic palette"});
      styleComboBox(colorModeSelector);
      colorModeSelector.setSelectedItem("Cyclic palette");
      colorModeSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
      colorModeSelector.getAccessibleContext().setAccessibleName("Trace color mode");
      colorModeLabel.setLabelFor(colorModeSelector);
      colorModeSelector.addActionListener(e -> {
        if (listener != null) {
          String selected = (String) colorModeSelector.getSelectedItem();
          listener.onBotColorModeChanged("Life opacity".equals(selected)
            ? "life" : "Cyclic palette".equals(selected) ? "palette" : "constant");
        }
      });
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
      settingsPanelContainer.add(colorModeLabel);
      settingsPanelContainer.add(colorModeSelector);
    } else if ("export".equals(toolId)) {
      JLabel header = new JLabel("Export Data");
      header.setForeground(COLOR_TEXT_PRIMARY);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanelContainer.add(header);
      settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));

      JComboBox<String> formatSelector = new JComboBox<>(new String[]{"SVG", "DXF", "PNG", "JPG"});
      styleComboBox(formatSelector);
      formatSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
      formatSelector.getAccessibleContext().setAccessibleName("Export format");
      JLabel formatLabel = new JLabel("Format");
      formatLabel.setForeground(COLOR_TEXT_PRIMARY);
      formatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      formatLabel.setLabelFor(formatSelector);
      settingsPanelContainer.add(formatLabel);
      settingsPanelContainer.add(formatSelector);

      JButton exportBtn = createButton("Export", 140);
      exportBtn.addActionListener(e -> {
        if (listener != null) {
          listener.onExportRequested(((String) formatSelector.getSelectedItem()).toLowerCase());
        }
      });
      settingsPanelContainer.add(exportBtn);
    }
  }

  private void buildSharedBotSettings() {
    addBotSettingsHeading("Movement and life");
    addBotSlider("Life", 50, 1000, 300, 1.0,
      value -> { if (listener != null) listener.onBotLifeChanged((float) value); });
    addBotSlider("Speed", 1, 100, 20, 10.0,
      value -> { if (listener != null) listener.onBotSpeedChanged((float) value); });
    addBotSlider("Drift influence", 0, 100, 10, 100.0,
      value -> { if (listener != null) listener.onBotDriftInfluenceChanged((float) value); });
    addBotSlider("Field influence", 0, 100, 100, 100.0,
      value -> { if (listener != null) listener.onBotFieldInfluenceChanged((float) value); });
    addBotSlider("Repulsion influence", 0, 100, 0, 100.0,
      value -> { if (listener != null) listener.onBotRepulsionInfluenceChanged((float) value); });
    addBotSlider("Repulsion radius", 0, 200, 50, 1.0,
      value -> { if (listener != null) listener.onBotRepulsionRadiusChanged((float) value); });

    JComboBox<String> driftMode = new JComboBox<>(new String[]{"Unique", "Collective", "Canvas"});
    styleComboBox(driftMode);
    driftMode.setSelectedItem("Unique");
    driftMode.setAlignmentX(Component.LEFT_ALIGNMENT);
    driftMode.getAccessibleContext().setAccessibleName("Drift mode");
    driftMode.setToolTipText("Choose per-bot, shared, or canvas-position drift");
    driftMode.addActionListener(event -> {
      if (listener != null) {
        listener.onBotDriftModeChanged(((String) driftMode.getSelectedItem()).toLowerCase());
      }
    });
    addBotControlLabel("Drift mode");
    settingsPanelContainer.add(driftMode);

    addBotSettingsHeading("Field radar");
    addBotSlider("Radar radius", 1, 200, 50, 1.0,
      value -> { if (listener != null) listener.onBotRadarChanged((float) value); });
    addBotSlider("Radar samples", 4, 32, 16, 1.0,
      value -> { if (listener != null) listener.onRadarSamplesChanged((int) value); });
    addBotSlider("Sample distance", 10, 100, 70, 100.0,
      value -> { if (listener != null) listener.onRadarSampleDistanceChanged((float) value); });
    addBotSlider("Center sample weight", 1, 50, 15, 10.0,
      value -> { if (listener != null) listener.onCenterSampleWeightChanged((float) value); });

    JComboBox<String> radarFalloff = new JComboBox<>(new String[]{"Linear", "Gaussian", "Hard Edge"});
    styleComboBox(radarFalloff);
    radarFalloff.setSelectedItem("Gaussian");
    radarFalloff.setAlignmentX(Component.LEFT_ALIGNMENT);
    radarFalloff.getAccessibleContext().setAccessibleName("Radar falloff");
      radarFalloff.setToolTipText("Choose how field radar samples are weighted by distance");
    radarFalloff.addActionListener(event -> {
      if (listener != null) {
        listener.onRadarSamplingFalloffChanged(((String) radarFalloff.getSelectedItem()).toLowerCase());
      }
    });
    addBotControlLabel("Radar falloff");
    settingsPanelContainer.add(radarFalloff);

    addBotSettingsHeading("Rendering and life decay");
    addBotCheckbox("Life decay from background luminance", false,
      selected -> { if (listener != null) listener.onBotLuminanceDecayToggle(selected); });
    addBotCheckbox("Fade traces", true,
      selected -> { if (listener != null) listener.onBotTraceFadeToggle(selected); });

    JComboBox<String> representation = new JComboBox<>(new String[]{"Point", "Circle", "Triangle"});
    styleComboBox(representation);
    representation.setSelectedItem("Circle");
    representation.setAlignmentX(Component.LEFT_ALIGNMENT);
    representation.getAccessibleContext().setAccessibleName("Bot representation");
      representation.setToolTipText("Choose how bots are drawn on the canvas");
    representation.addActionListener(event -> {
      if (listener != null) {
        listener.onBotRepresentationChanged(((String) representation.getSelectedItem()).toLowerCase());
      }
    });
    addBotControlLabel("Bot representation");
    settingsPanelContainer.add(representation);

    JComboBox<String> colorMode = new JComboBox<>(
      new String[]{"Constant", "Life opacity", "Cyclic palette"});
    styleComboBox(colorMode);
    colorMode.setSelectedItem("Cyclic palette");
    colorMode.setAlignmentX(Component.LEFT_ALIGNMENT);
    colorMode.getAccessibleContext().setAccessibleName("Trace color mode");
      colorMode.setToolTipText("Choose the color behavior for bot traces");
    colorMode.addActionListener(event -> {
      if (listener != null) {
        String selected = (String) colorMode.getSelectedItem();
        listener.onBotColorModeChanged("Life opacity".equals(selected)
          ? "life" : "Cyclic palette".equals(selected) ? "palette" : "constant");
      }
    });
    addBotControlLabel("Trace color mode");
    settingsPanelContainer.add(colorMode);

    JButton colorPicker = createButton("Trace base color...", 180);
    colorPicker.getAccessibleContext().setAccessibleName("Choose trace base color");
    colorPicker.setToolTipText("Set the color used by Constant and Life opacity modes");
    colorPicker.addActionListener(event -> {
      if (listener != null) listener.onBotColorPicker();
    });
    settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
    settingsPanelContainer.add(colorPicker);
  }

  private void addBotSettingsHeading(String text) {
    JLabel heading = new JLabel(text);
    heading.setForeground(COLOR_TEXT_PRIMARY);
    heading.setFont(new Font("Arial", Font.BOLD, 12));
    heading.setAlignmentX(Component.LEFT_ALIGNMENT);
    settingsPanelContainer.add(Box.createRigidArea(new Dimension(0, 8)));
    settingsPanelContainer.add(heading);
  }

  private void addBotControlLabel(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(COLOR_TEXT_PRIMARY);
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    settingsPanelContainer.add(label);
  }

  private void addBotCheckbox(String text, boolean selected,
      java.util.function.Consumer<Boolean> action) {
    JCheckBox checkbox = new JCheckBox(text);
    checkbox.setSelected(selected);
    checkbox.setForeground(COLOR_TEXT_PRIMARY);
    checkbox.setBackground(COLOR_PANEL_BG);
    checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
    checkbox.setPreferredSize(new Dimension(190, BUTTON_HEIGHT));
    checkbox.getAccessibleContext().setAccessibleName(text);
    checkbox.setToolTipText(text);
    checkbox.addActionListener(event -> action.accept(checkbox.isSelected()));
    settingsPanelContainer.add(checkbox);
  }

  private void addBotSlider(String title, int minimum, int maximum, int initial,
                            double divisor, java.util.function.DoubleConsumer listenerAction) {
    double initialValue = initial / divisor;
    JLabel valueLabel = new JLabel(String.format("%s: %s", title,
      divisor == 1.0 ? String.format("%.0f", initialValue) : String.format("%.2f", initialValue)));
    valueLabel.setForeground(COLOR_TEXT_PRIMARY);
    valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    JSlider slider = createSlider(minimum, maximum, initial);
    slider.getAccessibleContext().setAccessibleName(title);
    slider.setToolTipText("Adjust " + title.toLowerCase());
    valueLabel.setLabelFor(slider);
    slider.addChangeListener(event -> {
      double value = slider.getValue() / divisor;
      valueLabel.setText(String.format("%s: %s", title,
        divisor == 1.0 ? String.format("%.0f", value) : String.format("%.2f", value)));
      listenerAction.accept(value);
    });
    settingsPanelContainer.add(valueLabel);
    settingsPanelContainer.add(slider);
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
    btn.setPreferredSize(new Dimension(width, BUTTON_HEIGHT));
    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
    btn.setBackground(COLOR_BUTTON_DEFAULT);
    btn.setForeground(COLOR_PANEL_BG);
    btn.setFocusPainted(true);
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
    styleSlider(slider);
    return slider;
  }

  private void styleSlider(JSlider slider) {
    slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
    slider.setPreferredSize(new Dimension(190, BUTTON_HEIGHT));
    slider.setBackground(COLOR_PANEL_BG);
    slider.setForeground(COLOR_ACCENT_GREEN);
    slider.setOpaque(false);
    slider.setUI(new FlowSliderUI(slider));
    slider.setAlignmentX(Component.LEFT_ALIGNMENT);
    slider.setFocusable(true);
  }

  private void styleComboBox(JComboBox<?> comboBox) {
    comboBox.setBackground(COLOR_INPUT_BG);
    comboBox.setForeground(COLOR_TEXT_PRIMARY);
    comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    comboBox.setPreferredSize(new Dimension(190, 36));
    comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    comboBox.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
      BorderFactory.createEmptyBorder(3, 8, 3, 4)));
    comboBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
        label.setFont(comboBox.getFont());
        label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        label.setBackground(isSelected ? COLOR_ACCENT_GREEN : COLOR_INPUT_BG);
        label.setForeground(isSelected ? COLOR_PANEL_BG : COLOR_TEXT_PRIMARY);
        return label;
      }
    });
    if (comboBox.getSelectedIndex() < 0 && comboBox.getItemCount() > 0) {
      comboBox.setSelectedIndex(0);
    }
  }

  private static final class FlowSliderUI extends BasicSliderUI {
    FlowSliderUI(JSlider slider) {
      super(slider);
    }

    @Override
    protected Dimension getThumbSize() {
      return new Dimension(16, 16);
    }

    @Override
    public void paintTrack(Graphics graphics) {
      Graphics2D graphics2D = (Graphics2D) graphics.create();
      graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
      int trackHeight = 4;
      int trackY = trackRect.y + (trackRect.height - trackHeight) / 2;
      int left = trackRect.x;
      int right = trackRect.x + trackRect.width;
      int thumbCenter = thumbRect.x + thumbRect.width / 2;
      graphics2D.setColor(COLOR_SLIDER_TRACK);
      graphics2D.fill(new RoundRectangle2D.Double(left, trackY,
        Math.max(0, right - left), trackHeight, trackHeight, trackHeight));
      graphics2D.setColor(COLOR_ACCENT_GREEN);
      graphics2D.fill(new RoundRectangle2D.Double(left, trackY,
        Math.max(0, thumbCenter - left), trackHeight, trackHeight, trackHeight));
      graphics2D.dispose();
    }

    @Override
    public void paintThumb(Graphics graphics) {
      Graphics2D graphics2D = (Graphics2D) graphics.create();
      graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
      graphics2D.setColor(COLOR_ACCENT_GREEN);
      graphics2D.fill(new Ellipse2D.Double(thumbRect.x, thumbRect.y,
        thumbRect.width - 1, thumbRect.height - 1));
      graphics2D.setColor(slider.hasFocus() ? COLOR_TEXT_PRIMARY : COLOR_PANEL_BG);
      graphics2D.setStroke(new BasicStroke(slider.hasFocus() ? 2f : 1f));
      graphics2D.draw(new Ellipse2D.Double(thumbRect.x + 1, thumbRect.y + 1,
        thumbRect.width - 3, thumbRect.height - 3));
      graphics2D.dispose();
    }
  }

  private MouseAdapter makeHoverListener(AbstractButton btn) {
    return new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        btn.setBackground(COLOR_BUTTON_HOVER);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        btn.setBackground(getToolButtonBackground(btn.isSelected()));
        btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    };
  }

  private static Color getToolButtonBackground(boolean selected) {
    return selected ? COLOR_TOOL_SELECTED : COLOR_BUTTON_DEFAULT;
  }

  private MouseAdapter makeLayerTabHoverListener(AbstractButton button) {
    return new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent event) {
        if (!button.isSelected()) button.setBackground(COLOR_LAYER_HOVER);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent event) {
        button.setBackground(button.isSelected() ? COLOR_ACCENT_GREEN : COLOR_LAYER_DEFAULT);
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
