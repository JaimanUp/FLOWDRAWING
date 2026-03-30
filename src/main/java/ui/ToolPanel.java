package ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * ToolPanel
 * Left-side vertical tabbed tool panel.
 * Four tabs: Background | Vector Field | Sketch | Botfield
 * Replaces the old horizontal UIPanel strip.
 */
public class ToolPanel extends JPanel {

  private static final int PANEL_WIDTH = 230;
  private static final int SLIDER_WIDTH = 180;

  // ── Color Palette ──────────────────────────────────────────────────────
  private static final Color COLOR_PANEL_BG = new Color(46, 46, 46);
  // Main background for panels and tabs. Dark gray base color.

  private static final Color COLOR_SECTION_BG = new Color(55, 55, 55);
  // Background for individual section panels. Slightly lighter than main panel.

  private static final Color COLOR_SECTION_BORDER = new Color(90, 90, 90);
  // Border color for section dividers. Medium gray for subtle contrast.

  private static final Color COLOR_TEXT_PRIMARY = new Color(200, 200, 200);
  // Primary text color for labels, checkboxes, and interactive elements.

  private static final Color COLOR_TEXT_SECONDARY = new Color(180, 180, 180);
  // Secondary text color for section titles and muted labels.

  private static final Color COLOR_TEXT_TAB = Color.LIGHT_GRAY;
  // Tab text color. Light gray for tab labels.

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
    void onClearBots();
  }

  private final ToolListener listener;
  private JLabel brushSizeLabel, brushHardnessLabel, brushStrengthLabel;
  private JLabel botLifeLabel, botRadarLabel, botDriftLabel, botSpeedLabel, botSpawnRateLabel;

  public ToolPanel(ToolListener listener) {
    this.listener = listener;
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(PANEL_WIDTH, 0));
    setBackground(COLOR_PANEL_BG);

    JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.setBackground(COLOR_PANEL_BG);
    tabs.setForeground(COLOR_TEXT_TAB);

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

  // ── Helper: section separator label ──────────────────────────────────
  private JPanel section(String title) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLineBorder(COLOR_SECTION_BORDER),
      title,
      TitledBorder.LEFT,
      TitledBorder.TOP,
      new Font("Arial", Font.BOLD, 11),
      COLOR_TEXT_SECONDARY
    ));
    panel.setBackground(COLOR_SECTION_BG);
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
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
    s.setPreferredSize(new Dimension(SLIDER_WIDTH, 28));
    s.setMaximumSize(new Dimension(SLIDER_WIDTH, 28));
    s.setBackground(COLOR_SECTION_BG);
    s.setAlignmentX(Component.LEFT_ALIGNMENT);
    return s;
  }

  private JButton rowButton(String text) {
    JButton b = new JButton(text);
    b.setAlignmentX(Component.LEFT_ALIGNMENT);
    b.setMaximumSize(new Dimension(SLIDER_WIDTH, 26));
    return b;
  }

  private JCheckBox rowCheckBox(String text, boolean selected) {
    JCheckBox cb = new JCheckBox(text, selected);
    cb.setBackground(COLOR_SECTION_BG);
    cb.setForeground(COLOR_TEXT_PRIMARY);
    cb.setFont(new Font("Arial", Font.PLAIN, 11));
    cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    return cb;
  }

  private void pad(JPanel p) {
    p.add(Box.createRigidArea(new Dimension(0, 4)));
  }

  // ────────────────────────────────────────────────────────────────────
  // TAB 1 — Background
  // ────────────────────────────────────────────────────────────────────
  private JPanel buildBackgroundTab() {
    JPanel root = new JPanel();
    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
    root.setBackground(COLOR_PANEL_BG);
    root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    // Visibility
    JPanel visSection = section("Visibility");
    JCheckBox showBg = rowCheckBox("Show Background", true);
    showBg.addActionListener(e -> { if (listener != null) listener.onBackgroundToggle(showBg.isSelected()); });
    visSection.add(showBg);
    root.add(visSection);
    pad(root);

    // Appearance
    JPanel appearSection = section("Appearance");
    JLabel alphaLabel = rowLabel("Transparency: 100%");
    appearSection.add(alphaLabel);
    JSlider alphaSlider = rowSlider(0, 100, 100);
    alphaSlider.addChangeListener(e -> {
      float alpha = alphaSlider.getValue() / 100.0f;
      alphaLabel.setText(String.format("Transparency: %d%%", alphaSlider.getValue()));
      if (listener != null) listener.onBackgroundTransparencyChanged(alpha);
    });
    appearSection.add(alphaSlider);
    pad(appearSection);
    JButton colorBtn = rowButton("Colour...");
    colorBtn.addActionListener(e -> { if (listener != null) listener.onBackgroundColorPicker(); });
    appearSection.add(colorBtn);
    root.add(appearSection);
    pad(root);

    // Image
    JPanel imgSection = section("Image");
    JButton loadBtn = rowButton("Load Image...");
    loadBtn.addActionListener(e -> { if (listener != null) listener.onLoadBackgroundImage(); });
    JButton clearImgBtn = rowButton("Clear Image");
    clearImgBtn.addActionListener(e -> { if (listener != null) listener.onClearBackgroundImage(); });
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
    root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    // Visibility
    JPanel visSection = section("Visibility");
    JCheckBox showField = rowCheckBox("Show Vector Field", true);
    showField.addActionListener(e -> { if (listener != null) listener.onVectorFieldToggle(showField.isSelected()); });
    visSection.add(showField);
    root.add(visSection);
    pad(root);

    // Appearance
    JPanel appearSection = section("Appearance");
    JLabel alphaLabel = rowLabel("Transparency: 100%");
    appearSection.add(alphaLabel);
    JSlider alphaSlider = rowSlider(0, 100, 100);
    alphaSlider.addChangeListener(e -> {
      alphaLabel.setText(String.format("Transparency: %d%%", alphaSlider.getValue()));
      if (listener != null) listener.onVectorFieldTransparencyChanged(alphaSlider.getValue() / 100.0f);
    });
    appearSection.add(alphaSlider);
    pad(appearSection);

    JLabel modeLabel = rowLabel("Visualization Mode");
    appearSection.add(modeLabel);
    JRadioButton arrowBtn = new JRadioButton("Arrow");
    JRadioButton heatmapBtn = new JRadioButton("Heatmap");
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
    resetBtn.addActionListener(e -> { if (listener != null) listener.onResetVectorField(); });
    JButton randomBtn = rowButton("Randomize Field");
    randomBtn.addActionListener(e -> { if (listener != null) listener.onRandomVectorField(); });
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
    root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    // Visibility
    JPanel visSection = section("Visibility");
    JCheckBox showStrokes = rowCheckBox("Show Strokes", true);
    showStrokes.addActionListener(e -> { if (listener != null) listener.onSketchToggle(showStrokes.isSelected()); });
    visSection.add(showStrokes);
    root.add(visSection);
    pad(root);

    // Appearance
    JPanel appearSection = section("Appearance");
    JLabel alphaLabel = rowLabel("Transparency: 100%");
    appearSection.add(alphaLabel);
    JSlider alphaSlider = rowSlider(0, 100, 100);
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
    JSlider sizeSlider = rowSlider(5, 100, 30);
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
    clearBtn.addActionListener(e -> { if (listener != null) listener.onClearSketch(); });
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
    root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    // Visibility
    JPanel visSection = section("Visibility");
    JCheckBox showBots = rowCheckBox("Show Botfield", true);
    showBots.addActionListener(e -> { if (listener != null) listener.onBotfieldToggle(showBots.isSelected()); });
    visSection.add(showBots);
    root.add(visSection);
    pad(root);

    // Appearance
    JPanel appearSection = section("Appearance");
    JLabel alphaLabel = rowLabel("Transparency: 100%");
    appearSection.add(alphaLabel);
    JSlider alphaSlider = rowSlider(0, 100, 100);
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
    lifeSlider.addChangeListener(e -> {
      botLifeLabel.setText(String.format("Life: %d", lifeSlider.getValue()));
      if (listener != null) listener.onBotLifeChanged(lifeSlider.getValue());
    });
    botSection.add(lifeSlider);
    pad(botSection);

    botRadarLabel = rowLabel("Radar: 50");
    botSection.add(botRadarLabel);
    JSlider radarSlider = rowSlider(10, 200, 50);
    radarSlider.addChangeListener(e -> {
      botRadarLabel.setText(String.format("Radar: %d", radarSlider.getValue()));
      if (listener != null) listener.onBotRadarChanged(radarSlider.getValue());
    });
    botSection.add(radarSlider);
    pad(botSection);

    botDriftLabel = rowLabel("Drift: 0.30");
    botSection.add(botDriftLabel);
    JSlider driftSlider = rowSlider(0, 100, 30);
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

    botSpawnRateLabel = rowLabel("Spawn Rate: 5");
    simSection.add(botSpawnRateLabel);
    JSlider rateSlider = rowSlider(1, 20, 5);
    rateSlider.addChangeListener(e -> {
      botSpawnRateLabel.setText(String.format("Spawn Rate: %d", rateSlider.getValue()));
      if (listener != null) listener.onBotSpawnRateChanged(rateSlider.getValue());
    });
    simSection.add(rateSlider);
    pad(simSection);

    JCheckBox autoSpawn = rowCheckBox("Auto Spawn", false);
    autoSpawn.addActionListener(e -> { if (listener != null) listener.onAutoSpawnToggle(autoSpawn.isSelected()); });
    simSection.add(autoSpawn);
    pad(simSection);

    JButton spawnBtn = rowButton("Spawn Bot");
    spawnBtn.addActionListener(e -> { if (listener != null) listener.onSpawnBot(); });
    simSection.add(spawnBtn);
    pad(simSection);

    JButton clearBotsBtn = rowButton("Clear Bots");
    clearBotsBtn.addActionListener(e -> { if (listener != null) listener.onClearBots(); });
    simSection.add(clearBotsBtn);
    root.add(simSection);

    root.add(Box.createVerticalGlue());
    return root;
  }

  // ── Radio button styling helper ────────────────────────────────────
  private void styleRadio(JRadioButton rb) {
    rb.setBackground(COLOR_SECTION_BG);
    rb.setForeground(COLOR_TEXT_PRIMARY);
    rb.setFont(new Font("Arial", Font.PLAIN, 11));
    rb.setAlignmentX(Component.LEFT_ALIGNMENT);
  }
}
