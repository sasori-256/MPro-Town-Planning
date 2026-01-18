package io.github.sasori_256.town_planning.common.ui.main.seen;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.github.sasori_256.town_planning.common.ui.CustomPanel;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;
import io.github.sasori_256.town_planning.common.ui.main.SceneNavigator;

public class TitlePanel extends JPanel {
  private SceneNavigator sceneNavigator;
  private ImageManager imageManager;

  public TitlePanel(SceneNavigator sceneNavigator, ImageManager imageManager) {
    this.sceneNavigator = sceneNavigator;
    this.imageManager = imageManager;
    JButton startButton = new JButton("Start Game");
    startButton.addActionListener(e -> {
      sceneNavigator.changeScene("GAME_MAP");
    });
    ImageStorage imageStorage = imageManager.getImageStorage("title");
    if (imageStorage == null || imageStorage.getImage() == null || imageStorage.getName().equals("error")) {
      System.err.println("\u001B[31mError: Image not found: " + "title" + "\u001B[0m");
    } else {
      CustomPanel logoPanel = new CustomPanel(imageStorage.getImage());
      logoPanel.setPreferredSize(
          new Dimension(imageStorage.getImage().getWidth(null) * 2, imageStorage.getImage().getHeight(null) * 2));
      this.add(logoPanel);
      this.add(startButton);
    }
  }
}