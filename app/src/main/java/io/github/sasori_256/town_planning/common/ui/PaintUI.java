package io.github.sasori_256.town_planning.common.ui;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.CategoryNode;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.MenuNode;

/**
 * ゲーム上のUIを描画するクラス
 * 最初にモード選択ボタンを描画し、次に選択されたモードに応じたカテゴリボタンを描画する
 * 更に、選択されたカテゴリに応じたオブジェクトボタンを描画する
 */
public class PaintUI {
  private String selectedModeName = "creative"; // UIのモード(view, creative, disaster)
  private String selectedCategoryName = "";
  private String selectedObjectName = "";
  private MenuNode selectedCategoryNode = null;
  private List<List<JButton>> createdButtons = new ArrayList<>(
      List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

  private void createButtonIfNotExists(JPanel panel, String buttonText, int xPos, int yPos, int width, int height,
      ActionListener actionListener, int level) {
    boolean exists = false;
    for (JButton button : createdButtons.get(level)) {
      if (button.getText().equals(buttonText)) {
        exists = true;
        break;
      }
    }
    if (!exists) { // 同じ名前のボタンが存在しない場合は追加する
      JButton button = new JButton(buttonText);
      button.setBounds(xPos, yPos, width, height);
      button.addActionListener(actionListener);
      createdButtons.get(level).add(button);
      panel.add(button);
      panel.revalidate();
      panel.repaint();
    }
  }

  public void repaintUI(JPanel panel) {
    for (List<JButton> buttonList : createdButtons) {
      for (JButton button : buttonList) {
        panel.remove(button);
      }
    }
    createdButtons = new ArrayList<>(
        List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
  }

  public void paintUI(Graphics g, CategoryNode root, Double UIScale, ImageManager imageManager, JPanel panel) {
    // モード選択ボタンの描画
    panel.setLayout(null);
    String[] modeButtons = {
        "creative",
        "disaster",
        "view"
    };
    for (int i = 0; i < modeButtons.length; i++) {
      String buttonText = modeButtons[i];
      int panelWidth = panel.getWidth();
      int xPos = (int) (panelWidth - 90 * UIScale);
      int yPos = (int) (100 + 90 * i * UIScale);
      int width = (int) (80 * UIScale);
      int height = (int) (80 * UIScale);
      ActionListener listener = e -> {
        selectedModeName = buttonText;
        System.out.println("Mode selected: " + selectedModeName);
        selectedCategoryName = "";
        selectedObjectName = "";
        for (JButton button : createdButtons.get(1)) {
          panel.remove(button);
        }
        createdButtons.get(1).clear();
        for (JButton button : createdButtons.get(2)) {
          panel.remove(button);
        }
        createdButtons.get(2).clear();
        panel.repaint();
      };
      createButtonIfNotExists(panel, buttonText, xPos, yPos, width, height, listener, 0);

    }

    // 選択されたモードに応じたカテゴリボタンの描画
    MenuNode categoryRoot;
    switch (selectedModeName) {
      case "creative":
        categoryRoot = root.getChildren().get(0); // 創造モードのルートノード
        break;
      case "disaster":
        categoryRoot = root.getChildren().get(1); // 災害モードのルートノード
        break;
      default:
      case "view":
        categoryRoot = null; // ビューモードのルートノード
        break;
    }
    if (categoryRoot != null) {
      for (int i = 0; i < categoryRoot.getChildren().size(); i++) {
        MenuNode categoryNode = categoryRoot.getChildren().get(i);
        String buttonText = categoryNode.getName();
        int xPos = (int) (-80 + 90 * UIScale);
        int yPos = (int) (100 + 90 * i * UIScale);
        int width = (int) (80 * UIScale);
        int height = (int) (80 * UIScale);
        ActionListener listener = e -> {
          selectedCategoryName = buttonText;
          selectedCategoryNode = categoryNode;
          System.out.println("Category selected: " + selectedCategoryName);
          for (MenuNode child : categoryNode.getChildren()) {
            System.out.println(" - Child: " + child.getName());
          }
          selectedObjectName = "";
          for (JButton button : createdButtons.get(2)) {
            panel.remove(button);
          }
          createdButtons.get(2).clear();
          panel.repaint();
        };
        createButtonIfNotExists(panel, buttonText, xPos, yPos, width, height, listener, 1);
      }
    } else {
      System.err.println("No category root for mode: " + selectedModeName);
    }
    // 選択されたカテゴリに応じたオブジェクトボタンの描画
    if (!selectedCategoryName.isEmpty()) {
      if (selectedCategoryNode != null) {
        for (int i = 0; i < selectedCategoryNode.getChildren().size(); i++) {
          MenuNode objectNode = selectedCategoryNode.getChildren().get(i);
          String buttonText = objectNode.getName();
          int panelHeight = panel.getHeight();
          int xPos = (int) (90 + 90 * i * UIScale);
          int yPos = (int) (panelHeight - 90 * UIScale);
          int width = (int) (80 * UIScale);
          int height = (int) (80 * UIScale);
          ActionListener listener = e -> {
            selectedObjectName = buttonText;
            System.out.println("Object selected: " + selectedObjectName);
          };
          createButtonIfNotExists(panel, buttonText, xPos, yPos, width, height, listener, 2);
        }
      } else {
        System.err.println("No object root for category: " + selectedCategoryName);
      }
    }
  }
}
