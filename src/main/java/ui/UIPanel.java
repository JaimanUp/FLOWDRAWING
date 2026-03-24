package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * UIPanel
 * Basic control panel for Phase 1 canvas system.
 * Allows toggling vector field visibility and resetting canvas.
 */
public class UIPanel extends JPanel {
  private JCheckBox showVectorFieldCheckBox;
  private JButton resetCanvasButton;
  private JButton clearStrokesButton;
  private JButton clearBotsButton;
  private JLabel statusLabel;
  
  private ActionListener resetCanvasListener;
  private ActionListener clearStrokesListener;
  private ActionListener clearBotsListener;
  private UIListener uiListener;
  
  public interface UIListener {
    void onResetCanvas();
    void onClearStrokes();
    void onClearBots();
    void onVectorFieldToggle(boolean show);
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
