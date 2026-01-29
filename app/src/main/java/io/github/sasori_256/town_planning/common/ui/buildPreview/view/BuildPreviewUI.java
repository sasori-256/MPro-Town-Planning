package io.github.sasori_256.town_planning.common.ui.buildPreview.view;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import java.awt.Point;
import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.ui.CustomButton;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;
import io.github.sasori_256.town_planning.common.ui.buildPreview.PreviewButtonType;
import io.github.sasori_256.town_planning.common.ui.buildPreview.controller.BuildAccept;
import io.github.sasori_256.town_planning.common.ui.buildPreview.controller.BuildCancel;
import io.github.sasori_256.town_planning.common.ui.buildPreview.controller.BuildRotate;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.model.GameModel;

public class BuildPreviewUI extends JPanel {
  private final ImageManager imageManager;
  private Camera camera;
  private final GameModel gameModel;
  private final int buttonMargin = 10;
  private double uiScale = 1.0;

  public BuildPreviewUI(ImageManager imageManager, Camera camera, GameModel gameModel) {
    this.imageManager = imageManager;
    this.camera = camera;
    this.gameModel = gameModel;

    initUI(new Point2D.Double(0, 0));
  }

  private void initUI(Point2D.Double pos) {
    this.setFocusable(false);
    this.setOpaque(false);
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    for (PreviewButtonType type : PreviewButtonType.values()) {
      ImageStorage imageStorage = imageManager.getImageStorage(type.getImageName());
      if (imageStorage == null || imageStorage.getImage() == null || imageStorage.getName().equals("error")) {
        System.err.println("\u001B[31mError: Image not found: " + type.getImageName() + "\u001B[0m");
      } else {
        CustomButton button = new CustomButton(type.getImageName(), imageStorage, 0, 0);
        this.add(button);
        switch (type) {
          case ACCEPT -> button.addActionListener(new BuildAccept(gameModel));
          case CANCEL -> button.addActionListener(new BuildCancel());
          case ROTATE -> button.addActionListener(new BuildRotate());
        }
        this.add(Box.createHorizontalStrut(buttonMargin));

      }
    }

    if (this.getComponentCount() == 0) {
      System.err.println("\u001B[31mError: No buttons added to BuildPreviewUI\u001B[0m");
      return;
    }

    this.setBounds((int) pos.x - this.getWidth() / 2, (int) pos.y - this.getHeight() * 2,
        (this.getComponent(0).getWidth() + buttonMargin) * PreviewButtonType.values().length,
        this.getComponent(0).getHeight());
  }

  // private void updateUiScale() {
  // for (int i = 0; i < this.getComponentCount(); i++) {
  // if (this.getComponent(i) instanceof CustomButton button) {
  // button.setUiScale(uiScale);
  // }
  // }
  // }

  public void updateUpPos(Point2D.Double pos) {
    if (pos == null) {
      return;
    }
    Point2D.Double roundedPos = new Point2D.Double(Math.round(pos.x), Math.round(pos.y));
    Point2D.Double screenPos = camera.isoToScreen(roundedPos);
    Point location = new Point((int) screenPos.x - this.getWidth() / 2, (int) screenPos.y - this.getHeight() * 2);
    this.setLocation(location);
  }

  public void setUiScale(double uiScale) {
    this.uiScale = uiScale;
    // updateUiScale();
  }
}
