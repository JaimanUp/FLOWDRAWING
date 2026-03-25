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
    void onBrushSizeChanged(float size);
    void onBrushHardnessChanged(float hardness);
    void onBrushStrengthChanged(float strength);
  }
  
  public UIPanel(UIListener listener) {
    this.uiListener = listener;
    setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
    setBackground(new Color(240, 240, 240));
    setBorder(BorderFactory.createEtchedBorder());
    
    // Vector field toggle
    showVectorFieldCheckBox = new JCheckBox("Show Vector Field");
    showVectorFieldCheckBox.addActionListener(e -> {
      if (uiListener != null) {
        uiListener.onVectorFieldToggle(showVectorFieldCheckBox.isSelected());
      }
    });
    add(showVectorFieldCheckBox);
    
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
    
    brushHardnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
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
}
