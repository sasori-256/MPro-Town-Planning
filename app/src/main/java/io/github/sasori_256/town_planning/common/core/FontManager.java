package io.github.sasori_256.town_planning.common.core;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * アプリケーション全体で使用するフォントを管理するクラス。
 * ラノベPOPv2フォントを読み込み、UIManagerを通じてすべてのフォントに関する設定に適用する。
 */
public class FontManager {

  public FontManager() {
    init();
  }

  public void init() {
    try {
      InputStream is = FontManager.class.getResourceAsStream("/fonts/BestTen-CRT.otf");

      if (is == null) {
        System.err.println("\u001B[31merror: Did not find font file\u001B[0m");
      } else {
        Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(baseFont);
        setGlobalFont(new FontUIResource(baseFont.deriveFont(12f)));

      }
    } catch (IOException | FontFormatException e) {
      e.printStackTrace();
      System.err.println("\u001B[31merror: Failed to load font. Using default font.\u001B[0m");
    }
  }

  private void setGlobalFont(FontUIResource fontResource) {
    Enumeration<Object> keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      Object value = UIManager.get(key);
      if (value instanceof FontUIResource) {
        UIManager.put(key, fontResource);
      }
    }
  }
}
