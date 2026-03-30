package ui;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * UIPanel
 * Control panel for canvas, vector field, and brush tools.
 * Phase 1: Canvas controls
 * Phase 2: Vector field visualization
 * Phase 4: Brush size, hardness, strength controls
 */
public class UIPanel extends JPanel {
  private JCheckBox showVectorFieldCheckBox;
  private JCheckBox showStrokesCheckBox;
  private JButton resetCanvasButton;
  private JButton clearStrokesButton;
  private JButton clearBotsButton;
  private JLabel statusLabel;
  
  // Phase 4: Brush controls
  private JSlider brushSizeSlider;
  private JSlider brushHardnessSlider;
  private JSlider brushStrengthSlider;
  private JLabel brushSizeLabel;
  private JLabel brushHardnessLabel;
  private JLabel brushStrengthLabel;
  
  // Phase 5: Bot controls
  private JButton spawnBotButton;
  private JCheckBox autoSpawnCheckBox;
  private JSlider botSpawnRateSlider;
  private JLabel botSpawnRateLabel;
  private JSlider botLifeSlider;
  private JLabel botLifeLabel;
  private JSlider botRadarSlider;
  private JLabel botRadarLabel;
  private JSlider botDriftSlider;
  private JLabel botDriftLabel;
  private JSlider botSpeedSlider;
  private JLabel botSpeedLabel;
  
  // Visualization mode radio buttons
  private JRadioButton arrowModeButton;
  private JRadioButton flowMapModeButton;
  private ButtonGroup vizModeGroup;
  
  private ActionListener resetCanvasListener;
  private ActionListener clearStrokesListener;
  private ActionListener clearBotsListener;
  private ChangeListener brushChangeListener;
  private UIListener uiListener;
  
  public interface UIListener {
    void onResetCanvas();
    void onClearStrokes();
    void onClearBots();
    void onClearVectorField();
    void onVectorFieldToggle(boolean show);
    void onStrokesToggle(boolean show);
    void onBrushSizeChanged(float size);
    void onBrushHardnessChanged(float hardness);
    void onBrushStrengthChanged(float strength);
    void onVisualizationModeChanged(String mode);
    // Phase 5: Bot controls
    void onSpawnBot();
    void onAutoSpawnToggle(boolean enabled);
    void onBotSpawnRateChanged(int rate);
    void onBotLifeChanged(float life);
    void onBotRadarChanged(float radar);
    void onBotDriftChanged(float drift);
    void onBotSpeedChanged(float speed);
  }
  
  public UIPanel(UIListener listener) {
    this.uiListener = listener;
    setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
    setBackground(new Color(240, 240, 240));
    setBorder(BorderFactory.createEtchedBorder());
    
    // Vector field toggle
    showVectorFieldCheckBox = new JCheckBox("Show Vector Field");
    showVectorFieldCheckBox.setSelected(true);  // Enable by default
    showVectorFieldCheckBox.addActionListener(e -> {
      if (uiListener != null) {
        uiListener.onVectorFieldToggle(showVectorFieldCheckBox.isSelected());
      }
    });
    add(showVectorFieldCheckBox);
    
    // Strokes toggle
    showStrokesCheckBox = new JCheckBox("Show Strokes");
    showStrokesCheckBox.setSelected(true);  // Enable by default
    showStrokesCheckBox.addActionListener(e -> {
      if (uiListener != null) {
        uiListener.onStrokesToggle(showStrokesCheckBox.isSelected());
      }
    });
    add(showStrokesCheckBox);
    
    add(new JSeparator(JSeparator.VERTICAL));
    
    // Reset canvas button
    resetCanvasButton = new JButton("Reset Canvas");
    resetCanvasButton.addActionListener(e -> {
      if (uiListener != null) {
        uiListener.onResetCanvas();
      }
    });
    add(resetCanvasButton);
    
    // Clear strokes button
    clearStrokesButton = new JButton("Clear Strokes");
    clearStrokesButton.addActionListener(e -> {
      if (uiListener != null) {
        uiListener.onClearStrokes();
      }
    });
    add(clearStrokesButton);
    
    // Clear bots button
    clearBotsButton = new JButton("Clear Bots");
    clearBotsButton.addActionListener(e -> {
      if (uiListener != null) {
        uiListener.onClearBots();
      }
    });
    add(clearBotsButton);
    
    // Clear vector field button
    JButton clearVectorFieldButton = new JButton("Clear Field");
    clearVectorFieldButton.addActionListener(e -> {
      if (uiListener != null) {
        uiListener.onClearVectorField();
      }
    });
    add(clearVectorFieldButton);
    
    add(new JSeparator(JSeparator.VERTICAL));
    
    // Phase 4: Brush controls
    brushSizeLabel = new JLabel("Size: 30");
    add(brushSizeLabel);
    
    brushSizeSlider = new JSlider(JSlider.HORIZONTAL, 5, 100, 30);
    brushSizeSlider.setPreferredSize(new Dimension(100, 40));
    brushSizeSlider.addChangeListener(e -> {
      float size = brushSizeSlider.getValue();
      brushSizeLabel.setText(String.format("Size: %.0f", size));
      if (uiListener != null) {
        uiListener.onBrushSizeChanged(size);
      }
    });
    add(brushSizeSlider);
    
    brushHardnessLabel = new JLabel("Hard: 0.50");
    add(brushHardnessLabel);
    
    brushHardnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 130, 65);
    brushHardnessSlider.setPreferredSize(new Dimension(80, 40));
    brushHardnessSlider.addChangeListener(e -> {
      float hardness = brushHardnessSlider.getValue() / 100.0f;
      brushHardnessLabel.setText(String.format("Hard: %.2f", hardness));
      if (uiListener != null) {
        uiListener.onBrushHardnessChanged(hardness);
      }
    });
    add(brushHardnessSlider);
    
    brushStrengthLabel = new JLabel("Str: 2.0");
    add(brushStrengthLabel);
    
    brushStrengthSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, 20);
    brushStrengthSlider.setPreferredSize(new Dimension(80, 40));
    brushStrengthSlider.addChangeListener(e -> {
      float strength = brushStrengthSlider.getValue() / 10.0f;
      brushStrengthLabel.setText(String.format("Str: %.1f", strength));
      if (uiListener != null) {
        uiListener.onBrushStrengthChanged(strength);
      }
    });
    add(brushStrengthSlider);
    
    add(new JSeparator(JSeparator.VERTICAL));
    
    // Visualization mode radio buttons
    arrowModeButton = new JRadioButton("Arrow Mode");
    flowMapModeButton = new JRadioButton("Flow Map Mode");
    vizModeGroup = new ButtonGroup();
    vizModeGroup.add(arrowModeButton);
    vizModeGroup.add(flowMapModeButton);
    arrowModeButton.setSelected(true);
    add(arrowModeButton);
    add(flowMapModeButton);
    arrowModeButton.addActionListener(e -> {
      if (uiListener != null && arrowModeButton.isSelected()) {
        uiListener.onVisualizationModeChanged("arrow");
      }
    });
    flowMapModeButton.addActionListener(e -> {
      if (uiListener != null && flowMapModeButton.isSelected()) {
        uiListener.onVisualizationModeChanged("flowmap");
      }
    });
    
    add(new JSeparator(JSeparator.VERTICAL));
    
    // Phase 5: Bot controls
    spawnBotButton = new JButton("Spawn Bot");
    spawnBotButton.addActionListener(e -> {
      if (uiListener != null) {
        uiListener.onSpawnBot();
      }
    });
    add(spawnBotButton);
    
    autoSpawnCheckBox = new JCheckBox("Auto Spawn");
    autoSpawnCheckBox.setSelected(false);
    autoSpawnCheckBox.addActionListener(e -> {
      if (uiListener != null) {
        uiListener.onAutoSpawnToggle(autoSpawnCheckBox.isSelected());
      }
    });
    add(autoSpawnCheckBox);
    
    botSpawnRateLabel = new JLabel("Rate: 5");
    add(botSpawnRateLabel);
    
    botSpawnRateSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 5);
    botSpawnRateSlider.setPreferredSize(new Dimension(80, 40));
    botSpawnRateSlider.addChangeListener(e -> {
      int rate = botSpawnRateSlider.getValue();
      botSpawnRateLabel.setText(String.format("Rate: %d", rate));
      if (uiListener != null) {
        uiListener.onBotSpawnRateChanged(rate);
      }
    });
    add(botSpawnRateSlider);
    
    add(new JSeparator(JSeparator.VERTICAL));
    
    botLifeLabel = new JLabel("Life: 500");
    add(botLifeLabel);
    
    botLifeSlider = new JSlider(JSlider.HORIZONTAL, 100, 2000, 500);
    botLifeSlider.setPreferredSize(new Dimension(80, 40));
    botLifeSlider.addChangeListener(e -> {
      float life = botLifeSlider.getValue();
      botLifeLabel.setText(String.format("Life: %.0f", life));
      if (uiListener != null) {
        uiListener.onBotLifeChanged(life);
      }
    });
    add(botLifeSlider);
    
    botRadarLabel = new JLabel("Radar: 50");
    add(botRadarLabel);
    
    botRadarSlider = new JSlider(JSlider.HORIZONTAL, 10, 200, 50);
    botRadarSlider.setPreferredSize(new Dimension(80, 40));
    botRadarSlider.addChangeListener(e -> {
      float radar = botRadarSlider.getValue();
      botRadarLabel.setText(String.format("Radar: %.0f", radar));
      if (uiListener != null) {
        uiListener.onBotRadarChanged(radar);
      }
    });
    add(botRadarSlider);
    
    botDriftLabel = new JLabel("Drift: 0.30");
    add(botDriftLabel);
    
    botDriftSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 30);
    botDriftSlider.setPreferredSize(new Dimension(80, 40));
    botDriftSlider.addChangeListener(e -> {
      float drift = botDriftSlider.getValue() / 100.0f;
      botDriftLabel.setText(String.format("Drift: %.2f", drift));
      if (uiListener != null) {
        uiListener.onBotDriftChanged(drift);
      }
    });
    add(botDriftSlider);
    
    botSpeedLabel = new JLabel("Speed: 2.0");
    add(botSpeedLabel);
    
    botSpeedSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, 20);
    botSpeedSlider.setPreferredSize(new Dimension(80, 40));
    botSpeedSlider.addChangeListener(e -> {
      float speed = botSpeedSlider.getValue() / 10.0f;
      botSpeedLabel.setText(String.format("Speed: %.1f", speed));
      if (uiListener != null) {
        uiListener.onBotSpeedChanged(speed);
      }
    });
    add(botSpeedSlider);
    
    add(new JSeparator(JSeparator.VERTICAL));
    
    // Status label
    statusLabel = new JLabel("Ready");
    statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    add(statusLabel);
  }
  
  public void setStatus(String status) {
    statusLabel.setText(status);
  }
  
  public boolean isVectorFieldVisible() {
    return showVectorFieldCheckBox.isSelected();
  }
  
  public void setVectorFieldVisible(boolean visible) {
    showVectorFieldCheckBox.setSelected(visible);
  }
  
  public boolean areStrokesVisible() {
    return showStrokesCheckBox.isSelected();
  }
  
  public void setStrokesVisible(boolean visible) {
    showStrokesCheckBox.setSelected(visible);
  }
}
