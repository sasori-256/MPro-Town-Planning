package io.github.sasori_256.town_planning.common.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * CustomLabel
 * テキストの縁取りやグラデーション、二色塗りなどの機能を提供する。
 */
public class CustomLabel extends JLabel {
  private boolean isOutlineEnabled = false;
  private Color outlineColor = Color.BLACK;
  private float outlineWidth = 2.0f;

  /**
   * Color fill mode.
   * (色を塗る方法のモード)
   */
  private enum ColorMode {
    NORMAL, GRADATION, TWO_TONE
  }

  private ColorMode colorMode = ColorMode.NORMAL;
  private Color topColor = Color.BLACK;
  private Color bottomColor = Color.BLACK;

  public CustomLabel(String text) {
    this(text, SwingConstants.LEFT);
  }

  public CustomLabel(String text, int horizontalAlignment) {
    super(text, horizontalAlignment);
  }

  /**
   * フォントの縁取りを設定する。
   * 
   * @param color
   * @param width
   */
  public void setOutline(Color color, float width) {
    this.isOutlineEnabled = true;
    this.outlineColor = color;
    this.outlineWidth = width;
    repaint();
  }

  /**
   * フォントをグラデーションで塗る。
   * 
   * @param topColor
   * @param bottomColor
   */
  public void setGradation(Color topColor, Color bottomColor) {
    this.colorMode = ColorMode.GRADATION;
    this.topColor = topColor;
    this.bottomColor = bottomColor;
    repaint();
  }

  /**
   * フォントを上下で二色に塗り分ける。
   * 
   * @param topColor
   * @param bottomColor
   */
  public void setTwoTone(Color topColor, Color bottomColor) {
    this.colorMode = ColorMode.TWO_TONE;
    this.topColor = topColor;
    this.bottomColor = bottomColor;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    String text = this.getText();
    if ((!isOutlineEnabled && colorMode == ColorMode.NORMAL) || text == null || text.isEmpty()) {
      super.paintComponent(g);
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, getWidth(), getHeight());
    }

    Font font = this.getFont();
    FontRenderContext frc = g2.getFontRenderContext();
    TextLayout textLayout = new TextLayout(text, font, frc);

    Rectangle bounds = this.getBounds();
    float textWidth = (float) textLayout.getBounds().getWidth();
    float ascent = textLayout.getAscent();
    float descent = textLayout.getDescent();
    float textHeight = ascent + descent;

    float y = (bounds.height - textHeight) / 2 + ascent;

    float x = 0;
    int alignment = getHorizontalAlignment();
    if (alignment == SwingConstants.CENTER) {
      x = (bounds.width - textWidth) / 2;
    } else if (alignment == SwingConstants.RIGHT) {
      x = bounds.width - textWidth - getInsets().right;
    } else {
      x = getInsets().left;
    }

    g2.translate(x, y);

    Shape shape = textLayout.getOutline(AffineTransform.getTranslateInstance(0, 0));

    // 縁取り描画
    if (isOutlineEnabled) {
      g2.setColor(outlineColor);
      g2.setStroke(new BasicStroke(outlineWidth));
      g2.draw(shape);
    }

    // 色塗り
    if (colorMode == ColorMode.GRADATION) {
      GradientPaint gp = new GradientPaint(
          0, -ascent, topColor,
          0, descent, bottomColor);
      g2.setPaint(gp);

    } else if (colorMode == ColorMode.TWO_TONE) {
      float[] fractions = { 0.0f, 0.5f, 0.501f, 1.0f };
      Color[] colors = { topColor, topColor, bottomColor, bottomColor };

      LinearGradientPaint lgp = new LinearGradientPaint(
          0, -ascent,
          0, descent,
          fractions,
          colors);
      g2.setPaint(lgp);

    } else {
      g2.setColor(getForeground());
    }

    g2.fill(shape);
    g2.dispose();
  }
}
