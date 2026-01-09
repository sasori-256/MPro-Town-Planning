package io.github.sasori_256.town_planning.common.ui;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.BuildingNode;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.CategoryNode;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.DisasterNode;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.MenuNode;

/**
 * ゲーム上のUIを描画するクラス
 * 最初にモード選択ボタンを描画し、次に選択されたモードに応じたカテゴリボタンを描画する
 * 更に、選択されたカテゴリに応じたオブジェクトボタンを描画する
 */
public class PaintUI {
  private final ImageManager imageManager;
  private final JPanel panel;
  private CategoryNode root;
  // private GameMapController gameMapController;
  private double UIScale = 1;

  public PaintUI(ImageManager imageManager, JPanel panel, CategoryNode root) {
    this.imageManager = imageManager;
    this.panel = panel;
    this.root = root;
    // this.gameMapController = gameMapController;
  }

  private String selectedModeName = "view"; // UIのモード(view, creative, disaster)
  private String selectedCategoryName = "";
  private String selectedObjectName = "";
  private MenuNode selectedCategoryNode = null;
  private List<List<JButton>> createdButtons = new ArrayList<>(
      List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>())); // レベルごとに作成されたボタンを保持するリスト
  // レベル0: モード選択ボタン
  // レベル1: カテゴリ選択ボタン
  // レベル2: オブジェクト選択ボタン

  private void createButtonIfNotExists(String buttonText, int xPos, int yPos, int width, int height,
      ActionListener actionListener, MenuNode objectNode, int level) {
    boolean exists = false;
    for (JButton button : createdButtons.get(level)) {
      if (button.getText().equals(buttonText)) {
        exists = true;
        break;
      }
    }
    if (!exists) { // 同じ名前のボタンが存在しない場合は追加する
      String prefix = "";
      switch (level) {
        case 0:
          prefix = "modeIcon_";
          break;
        case 1:
          prefix = "categoryIcon_";
          break;
        case 2:
          prefix = "objectIcon_";
          break;
      }
      String imageName = prefix + buttonText.toLowerCase();
      CustomButton button = new CustomButton(buttonText); // 画像が見つからない場合はテキストだけのボタンを使用
      ImageStorage imageStorage = imageManager.getImageStorage(imageName);
      if (imageStorage == null || imageStorage.name == null || imageStorage.name.equals("error")) {
        // System.err.println("Warning: Image not found: " + imageName);
      } else {
        button.setImage(imageStorage.image, width, height);
      }
      button.setCustomBounds(xPos, yPos, width, height);
      // ボタンの画像を指定
      button.addActionListener(actionListener);
      if (objectNode != null) {
        button.addActionListener(objectNode);
      }
      createdButtons.get(level).add(button);
      panel.add(button);
    }
  }

  /**
   * 全てのlevelのボタンを再配置・再描画する
   */
  public void repaintUI() {
    clearButtonLevel(0);
    clearButtonLevel(1);
    clearButtonLevel(2);
    paint();
  }

  /**
   * 指定レベルのボタンをすべて削除する
   * 
   * @param level
   */
  public void clearButtonLevel(int level) {
    for (JButton button : createdButtons.get(level)) {
      panel.remove(button);
    }
    createdButtons.get(level).clear();
  }

  /**
   * モードの選択状態を変更する
   * 
   * @param modeName
   */
  public void setSelectedMode(String modeName) {
    System.out.println("Mode selected: " + modeName);
    this.selectedModeName = modeName;
    this.selectedCategoryName = "";
    this.selectedObjectName = "";
    clearButtonLevel(1);
    clearButtonLevel(2);
    paint();
  }

  /**
   * 現在選択されているモード名を取得する
   * 
   * @return selectedModeName
   */
  public String getSelectedModeName() {
    return this.selectedModeName;
  }

  /**
   * カテゴリの選択状態を変更する
   * 
   * @param categoryName
   * @param categoryNode
   */
  public void setSelectedCategory(String categoryName, MenuNode categoryNode) {
    System.out.println("Category selected: " + categoryName);
    this.selectedCategoryName = categoryName;
    this.selectedCategoryNode = categoryNode;
    this.selectedObjectName = "";
    clearButtonLevel(2);
    paint();
  }

  /**
   * 現在選択されているカテゴリ名を取得する
   * 
   * @return selectedCategoryName
   */
  public String getSelectedCategoryName() {
    return this.selectedCategoryName;
  }

  /**
   * オブジェクトの選択状態を変更する
   * 
   * @param objectName
   */
  public void setSelectedObject(String objectName, MenuNode objectNode) {
    if (!(objectNode instanceof BuildingNode || objectNode instanceof DisasterNode)) {
      System.err.println("Selected object node is not a BuildingNode or DisasterNode: " + objectName);
      return;
    }
    objectNode.actionPerformed(null);
    this.selectedObjectName = objectName;
  }

  /**
   * 現在選択されているオブジェクト名を取得する
   * 
   * @return selectedObjectName
   */
  public String getSelectedObjectName() {
    return this.selectedObjectName;
  }

  /**
   * 現在のSelectedMode、SelectedCategoryに応じたUIを描画する
   * 
   * @param g
   */
  public void paint() {
    // モード選択ボタンの描画
    String[] modeButtons = {
        "creative",
        "disaster",
        "view"
    };
    {
      // モードボタンを画面右側に上揃え・縦並び均等配置で描画
      int buttonAmount = modeButtons.length;
      int panelHeight = panel.getHeight();
      int panelWidth = panel.getWidth();
      int height = (int) Math.clamp((panelHeight - 240) / buttonAmount, 20, 80 * UIScale);
      int width = height;
      double margin = 10 * UIScale;
      int xPos = (int) (panelWidth - (width + margin) * UIScale);
      double yPosBegin = 100;
      double yPosDelta = (height + margin) * UIScale;
      for (int i = 0; i < modeButtons.length; i++) {
        String buttonText = modeButtons[i];
        int yPos = (int) (yPosBegin + i * yPosDelta);
        ActionListener listener = e -> {
          setSelectedMode(buttonText);
        };
        createButtonIfNotExists(buttonText, xPos, yPos, width, height, listener, null, 0);
      }
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
    if (categoryRoot != null && categoryRoot.getChildren().size() > 0) {
      // カテゴリボタンを画面左側に上揃え・縦並び均等配置で描画
      int buttonAmount = categoryRoot.getChildren().size();
      int panelHeight = panel.getHeight();
      int height = (int) Math.clamp((panelHeight - 240) / buttonAmount, 20, 80 * UIScale);
      int width = height;
      double margin = 10 * UIScale;
      int xPos = (int) (10);
      double yPosBegin = 100;
      double yPosDelta = (height + margin) * UIScale;
      for (int i = 0; i < categoryRoot.getChildren().size(); i++) {
        MenuNode categoryNode = categoryRoot.getChildren().get(i);
        String buttonText = categoryNode.getName();
        int yPos = (int) (yPosBegin + i * yPosDelta);
        ActionListener listener = e -> {
          setSelectedCategory(buttonText, categoryNode);
        };
        createButtonIfNotExists(buttonText, xPos, yPos, width, height, listener, categoryNode, 1);
      }
    } else {
      if (categoryRoot == null) {
        System.err.println("Selected mode root is null for mode: " + selectedModeName);
      } else {
        System.out.println("No children in category root for mode: " + selectedModeName);
      }
    }
    // 選択されたカテゴリに応じたオブジェクトボタンの描画
    if (!selectedCategoryName.isEmpty()) {
      if (selectedCategoryNode != null && selectedCategoryNode.getChildren().size() > 0) {
        // オブジェクトボタンを画面下部に中央揃え・横並び均等配置で描画
        int buttonAmount = selectedCategoryNode.getChildren().size();
        int panelWidth = panel.getWidth();
        int panelHeight = panel.getHeight();
        int width = (int) Math.clamp((panelWidth - 160) / buttonAmount, 20, 80 * UIScale);
        int height = width;
        double margin = 10 * UIScale;
        int yPos = (int) (panelHeight - (height + margin) * UIScale);
        double xPosBegin = panelWidth / 2 - (buttonAmount * (width + margin) * UIScale) / 2 + margin / 2 * UIScale;
        double xPosDelta = (width + margin) * UIScale;
        for (int i = 0; i < buttonAmount; i++) {
          MenuNode objectNode = selectedCategoryNode.getChildren().get(i);
          String buttonText = objectNode.getName();
          int xPos = (int) (xPosBegin + i * xPosDelta);
          ActionListener listener = e -> {
            setSelectedObject(buttonText, objectNode);
          };
          createButtonIfNotExists(buttonText, xPos, yPos, width, height, listener, objectNode, 2);
        }
      } else {
        if (selectedCategoryNode == null) {
          System.err.println("Selected category node is null for category: " + selectedCategoryName);
        } else {
          System.out.println("No children in selected category node: " + selectedCategoryName);
        }
      }
    }
    panel.repaint();
  }

  /**
   * UIのスケールを設定する
   * 
   * @param scale
   */
  public void setUIScale(double scale) {
    UIScale = scale;
    this.repaintUI();
  }
}
