package io.github.sasori_256.town_planning.common.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class ToastManager {
  private static final int DISPLAY_MILLIS = 3000;
  private static final int MAX_VISIBLE = 3;
  private static final int MARGIN = 12;
  private static final int SPACING = 8;

  private final JLayeredPane layeredPane;
  private final JComponent anchor;
  private final List<ToastPanel> toasts = new ArrayList<>();

  public ToastManager(JLayeredPane layeredPane, JComponent anchor) {
    this.layeredPane = layeredPane;
    this.anchor = anchor;
    this.anchor.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        relayoutToasts();
      }
    });
  }

  public void show(String message, ToastType type) {
    Runnable task = () -> showInternal(message, type);
    if (SwingUtilities.isEventDispatchThread()) {
      task.run();
    } else {
      SwingUtilities.invokeLater(task);
    }
  }

  private void showInternal(String message, ToastType type) {
    ToastPanel panel = new ToastPanel(message, type);
    panel.setSize(panel.getPreferredSize());
    if (toasts.size() >= MAX_VISIBLE) {
      removeToast(toasts.get(0));
    }
    toasts.add(panel);
    layeredPane.add(panel, JLayeredPane.POPUP_LAYER);
    relayoutToasts();
    Timer timer = new Timer(DISPLAY_MILLIS, e -> removeToast(panel));
    timer.setRepeats(false);
    timer.start();
  }

  private void removeToast(ToastPanel panel) {
    toasts.remove(panel);
    layeredPane.remove(panel);
    layeredPane.repaint();
    relayoutToasts();
  }

  private void relayoutToasts() {
    int width = layeredPane.getWidth();
    int height = layeredPane.getHeight();
    if (width <= 0 || height <= 0) {
      return;
    }
    int y = height - MARGIN;
    for (int i = toasts.size() - 1; i >= 0; i--) {
      ToastPanel panel = toasts.get(i);
      Dimension size = panel.getPreferredSize();
      int x = Math.max(MARGIN, width - MARGIN - size.width);
      y -= size.height;
      panel.setBounds(x, y, size.width, size.height);
      y -= SPACING;
    }
    layeredPane.repaint();
  }

  public enum ToastType {
    INFO(new Color(45, 45, 45, 230), new Color(80, 80, 80), Color.WHITE),
    WARNING(new Color(160, 95, 20, 230), new Color(200, 130, 40), Color.WHITE),
    ERROR(new Color(150, 40, 40, 230), new Color(200, 70, 70), Color.WHITE);

    private final Color backgroundColor;
    private final Color borderColor;
    private final Color textColor;

    ToastType(Color backgroundColor, Color borderColor, Color textColor) {
      this.backgroundColor = backgroundColor;
      this.borderColor = borderColor;
      this.textColor = textColor;
    }

    Color getBackgroundColor() {
      return backgroundColor;
    }

    Color getBorderColor() {
      return borderColor;
    }

    Color getTextColor() {
      return textColor;
    }
  }
}
