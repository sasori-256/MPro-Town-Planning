package io.github.sasori_256.town_planning.common.ui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import io.github.sasori_256.town_planning.common.ui.ToastManager;

/**
 * ゲームの画面遷移とトースト表示を担うUIシェル。
 */
public class GameWindow extends JFrame {
  private final JPanel mainPanel;
  private final CardLayout cardLayout;
  private final Map<SceneId, JComponent> scenes = new EnumMap<>(SceneId.class);
  private final ToastManager toastManager;
  private static final int MIN_WIDTH = 640;
  private static final int MIN_HEIGHT = 480;

  public GameWindow(int width, int height) {
    setTitle("Town Planning Game");
    setSize(width, height);
    setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    this.cardLayout = new CardLayout();
    this.mainPanel = new JPanel(this.cardLayout);
    this.add(this.mainPanel, BorderLayout.CENTER);

    this.toastManager = new ToastManager(getLayeredPane(), getLayeredPane());
    this.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        repaintCurrentSceneUI();
      }
    });
    setVisible(true);
  }

  public void setScene(SceneId sceneId, JComponent component) {
    JComponent existing = scenes.put(sceneId, component);
    if (existing != null) {
      this.mainPanel.remove(existing);
    }
    this.mainPanel.add(component, sceneId.name());
    this.mainPanel.revalidate();
    this.mainPanel.repaint();
  }

  public void showScene(SceneId sceneId) {
    this.cardLayout.show(this.mainPanel, sceneId.name());
    repaintCurrentSceneUI();
  }

  public void showToast(String message, ToastManager.ToastType type) {
    toastManager.show(message, type);
  }

  public Dimension getSceneSize() {
    return this.mainPanel.getSize();
  }

  private void repaintCurrentSceneUI() {
    for (Component comp : this.mainPanel.getComponents()) {
      if (comp.isVisible() && comp instanceof UiRefreshable) {
        ((UiRefreshable) comp).repaintUI();
      }
    }
  }
}
