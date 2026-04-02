package ui;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * AppMenuBar
 * Standard Swing JMenuBar for the main application frame.
 * Covers project-level actions only; tool controls live in the left ToolPanel.
 */
public class AppMenuBar extends JMenuBar {

  public interface MenuListener {
    // File
    void onNewProject();
    void onExit();
    // View
    void onResetView();
    void onZoomIn();
    void onZoomOut();
    void onFitCanvas();
    void onToggleToolPanel();
  }

  public AppMenuBar(MenuListener listener) {

    // ── File ──────────────────────────────────────────────────────────────
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    JMenuItem newItem = new JMenuItem("New Project");
    newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
    newItem.addActionListener(e -> { if (listener != null) listener.onNewProject(); });
    fileMenu.add(newItem);

    JMenuItem openItem = new JMenuItem("Open Project...");
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
    openItem.setEnabled(false);  // [planned]
    fileMenu.add(openItem);

    JMenuItem saveItem = new JMenuItem("Save Project");
    saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
    saveItem.setEnabled(false);  // [planned]
    fileMenu.add(saveItem);

    JMenuItem saveAsItem = new JMenuItem("Save Project As...");
    saveAsItem.setEnabled(false);  // [planned]
    fileMenu.add(saveAsItem);

    fileMenu.addSeparator();

    JMenu exportMenu = new JMenu("Export");
    JMenuItem exportSvg = new JMenuItem("Export as SVG");
    exportSvg.setEnabled(false);  // [planned]
    JMenuItem exportDxf = new JMenuItem("Export as DXF");
    exportDxf.setEnabled(false);  // [planned]
    JMenuItem exportPng = new JMenuItem("Export as PNG");
    exportPng.setEnabled(false);  // [planned]
    exportMenu.add(exportSvg);
    exportMenu.add(exportDxf);
    exportMenu.add(exportPng);
    fileMenu.add(exportMenu);

    fileMenu.addSeparator();

    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
    exitItem.addActionListener(e -> { if (listener != null) listener.onExit(); });
    fileMenu.add(exitItem);

    add(fileMenu);

    // ── Edit ──────────────────────────────────────────────────────────────
    JMenu editMenu = new JMenu("Edit");
    editMenu.setMnemonic(KeyEvent.VK_E);

    JMenuItem undoItem = new JMenuItem("Undo");
    undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
    undoItem.setEnabled(false);  // [planned]
    editMenu.add(undoItem);

    JMenuItem redoItem = new JMenuItem("Redo");
    redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
    redoItem.setEnabled(false);  // [planned]
    editMenu.add(redoItem);

    editMenu.addSeparator();

    JMenuItem prefsItem = new JMenuItem("Preferences");
    prefsItem.setEnabled(false);  // [planned]
    editMenu.add(prefsItem);

    add(editMenu);

    // ── View ──────────────────────────────────────────────────────────────
    JMenu viewMenu = new JMenu("View");
    viewMenu.setMnemonic(KeyEvent.VK_V);

    JMenuItem fitItem = new JMenuItem("Fit Canvas to Screen");
    fitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK));
    fitItem.addActionListener(e -> { if (listener != null) listener.onFitCanvas(); });
    viewMenu.add(fitItem);

    JMenuItem resetViewItem = new JMenuItem("Reset View");
    resetViewItem.addActionListener(e -> { if (listener != null) listener.onResetView(); });
    viewMenu.add(resetViewItem);

    viewMenu.addSeparator();

    JMenuItem zoomInItem = new JMenuItem("Zoom In");
    zoomInItem.setToolTipText("Zoom in (or use Space + +)");
    zoomInItem.addActionListener(e -> { if (listener != null) listener.onZoomIn(); });
    viewMenu.add(zoomInItem);

    JMenuItem zoomOutItem = new JMenuItem("Zoom Out");
    zoomOutItem.setToolTipText("Zoom out (or use Space + -)");
    zoomOutItem.addActionListener(e -> { if (listener != null) listener.onZoomOut(); });
    viewMenu.add(zoomOutItem);

    viewMenu.addSeparator();

    JMenuItem togglePanelItem = new JMenuItem("Toggle Tool Panel");
    togglePanelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK));
    togglePanelItem.addActionListener(e -> { if (listener != null) listener.onToggleToolPanel(); });
    viewMenu.add(togglePanelItem);

    add(viewMenu);

    // ── Help ──────────────────────────────────────────────────────────────
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);

    JMenuItem aboutItem = new JMenuItem("About");
    aboutItem.addActionListener(e -> {
      JOptionPane.showMessageDialog(
        null,
        "Flow-Guided Generative Drawing Engine\n\nPhase 5 — Bot Simulation\n\nControls:\n  Left Click — Paint / Guide field\n  Middle Click + Drag — Pan\n  Scroll — Zoom\n  R — Reset view",
        "About FlowDrawing",
        JOptionPane.INFORMATION_MESSAGE
      );
    });
    helpMenu.add(aboutItem);

    JMenuItem shortcutsItem = new JMenuItem("Keyboard Shortcuts");
    shortcutsItem.addActionListener(e -> {
      JOptionPane.showMessageDialog(
        null,
        "Ctrl+N          New Project\n" +
        "Ctrl+Q          Exit\n" +
        "Ctrl+0          Fit Canvas to Screen\n" +
        "R               Reset view\n" +
        "Space + +       Zoom in\n" +
        "Space + -       Zoom out\n" +
        "Space + ↑       Pan up\n" +
        "Space + ↓       Pan down\n" +
        "Space + ←       Pan left\n" +
        "Space + →       Pan right\n" +
        "Ctrl+T          Toggle Tool Panel\n" +
        "MMB + Drag      Pan (middle mouse)\n" +
        "Scroll          Zoom at mouse position",
        "Keyboard Shortcuts",
        JOptionPane.PLAIN_MESSAGE
      );
    });
    helpMenu.add(shortcutsItem);

    add(helpMenu);
  }
}
