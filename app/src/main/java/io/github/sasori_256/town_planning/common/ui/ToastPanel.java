package io.github.sasori_256.town_planning.common.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ToastPanel extends JPanel {
  private static final int ARC = 12;
  private static final int MAX_TEXT_WIDTH = 260;

  private final Color backgroundColor;
  private final Color borderColor;

  public ToastPanel(String message, ToastManager.ToastType type) {
    this.backgroundColor = type.getBackgroundColor();
    this.borderColor = type.getBorderColor();
    setOpaque(false);
    setLayout(new BorderLayout());
    JLabel label = new JLabel(formatMessage(message));
    label.setForeground(type.getTextColor());
    label.setBorder(new EmptyBorder(8, 12, 8, 12));
    add(label, BorderLayout.CENTER);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int width = getWidth();
    int height = getHeight();
    g2.setColor(backgroundColor);
    g2.fillRoundRect(0, 0, width, height, ARC, ARC);
    g2.setColor(borderColor);
    g2.drawRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
    g2.dispose();
  }

  private String formatMessage(String message) {
    String safe = escapeHtml(message == null ? "" : message.trim());
    return "<html><div style='width: " + MAX_TEXT_WIDTH + "px;'>" + safe + "</div></html>";
  }

  private String escapeHtml(String text) {
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }
}
