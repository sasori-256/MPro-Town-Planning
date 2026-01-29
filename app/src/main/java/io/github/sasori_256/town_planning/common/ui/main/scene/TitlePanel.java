package io.github.sasori_256.town_planning.common.ui.main.scene;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import io.github.sasori_256.town_planning.common.ui.CustomButton;
import io.github.sasori_256.town_planning.common.ui.CustomPanel;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;
import io.github.sasori_256.town_planning.common.ui.main.GameFlowNavigator;

public class TitlePanel extends CustomPanel {
  private final GameFlowNavigator navigator;
  private final ImageManager imageManager;

  public TitlePanel(GameFlowNavigator navigator, ImageManager imageManager) {
    ImageStorage backgroundImageStorage = imageManager.getImageStorage("background");
    if (backgroundImageStorage == null || backgroundImageStorage.getImage() == null
        || backgroundImageStorage.getName().equals("error")) {
      System.err.println("\u001B[31mError: Image not found: " + "background" + "\u001B[0m");
    }
    super(backgroundImageStorage != null ? backgroundImageStorage.getImage() : null, false);

    this.navigator = navigator;
    this.imageManager = imageManager;
    initCenterComponent();
  }

  private void initCenterComponent() {
    JPanel centerPanel = new JPanel();
    centerPanel.setOpaque(false);
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    centerPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);

    ImageStorage titleImageStorage = imageManager.getImageStorage("title");
    if (titleImageStorage == null || titleImageStorage.getImage() == null
        || titleImageStorage.getName().equals("error")) {
      System.err.println("\u001B[31mError: Image not found: " + "title" + "\u001B[0m");
    } else {
      CustomPanel logoPanel = new CustomPanel(titleImageStorage.getImage());
      logoPanel.setPreferredSize(
          new Dimension(titleImageStorage.getImage().getWidth(null) * 2,
              titleImageStorage.getImage().getHeight(null) * 2));
      logoPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
      centerPanel.add(logoPanel);
    }

    ImageStorage startImageStorage = imageManager.getImageStorage("start_button");
    if (startImageStorage == null || startImageStorage.getImage() == null
        || startImageStorage.getName().equals("error")) {
      System.err.println("\u001B[31mError: Image not found: " + "start_button" + "\u001B[0m");
    } else {
      CustomButton startButton = new CustomButton("Start Game", startImageStorage, 0, 0);
      startButton.addActionListener(e -> {
        navigator.startNewGame();
      });
      startButton.setAlignmentX(JPanel.CENTER_ALIGNMENT);
      centerPanel.add(startButton);
      startButton.setBorder(null);
    }
    this.add(centerPanel);
  }
}
