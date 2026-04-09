package ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

/**
 * Custom ButtonUI that prevents the default L&F from overriding colors when pressed/armed.
 */
public class CustomButtonUI extends BasicButtonUI {
  @Override
  public void paint(Graphics g, JComponent c) {
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
