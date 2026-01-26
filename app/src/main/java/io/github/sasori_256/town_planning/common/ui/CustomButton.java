
package io.github.sasori_256.town_planning.common.ui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;

/**
 * 画像表示や押下アニメーション付きのボタン。
 */
public class CustomButton extends JButton {
  private int originalWidth;
  private int originalHeight;
  private int centerX;
  private int centerY;
  private final String textContent; // 元のテキスト内容を保持
  private float ScaleFactor = 0.9f; // 押下時に90%のサイズに変更
  private int AnimationDuration = 100; // アニメーション時間（ミリ秒）
  private Thread animationThread;
  private final Image originalImage;
  private double animationProgress = 0.0;

  private void addChangeListenerToModel() {
    // ボタンモデルにリスナーを追加
    getModel().addChangeListener(e -> {
      if (getModel().isPressed()) {
        startPressAnimation();
      } else if (animationProgress > 0.0) {
        startReleaseAnimation();
      }
    });
  }

  private void textPositionOptimize() {
    this.setHorizontalTextPosition(JButton.CENTER);
    this.setVerticalTextPosition(JButton.BOTTOM);
  }

  /**
   * テキストのみのボタンを生成する。
   *
   * @param text   表示テキスト
   * @param xPos   ボタンの位置X (中心指定)
   * @param yPos   ボタンの位置Y (中心指定)
   * @param width  ボタンの幅
   * @param height ボタンの高さ
   */
  public CustomButton(String text, int xPos, int yPos, int width, int height) {
    this.textContent = text;
    this.originalImage = null;
    // いい感じに折り返せるようにHTMLを使う
    super("<html><div style='text-align: center;'>" + text + "</div></html>");
    addChangeListenerToModel();
    textPositionOptimize();
    setCustomBounds(xPos, yPos, width, height);
  }

  /**
   * 画像付きのボタンを生成する。
   *
   * @param text         表示テキスト
   * @param imageStorage 画像情報
   * @param xPos         ボタンの位置X (中心指定)
   * @param yPos         ボタンの位置Y (中心指定)
   * @param width        ボタンの幅 (ない場合は画像の幅)
   * @param height       ボタンの高さ (ない場合は画像の高さ)
   */
  public CustomButton(String text, ImageStorage imageStorage, int xPos, int yPos, int width, int height) {
    this.textContent = text;
    this.originalImage = imageStorage.getImage();
    super();
    addChangeListenerToModel();
    setImage(imageStorage.getImage(), width, height);
    setCustomBounds(xPos, yPos, width, height);
    setFocusable(false);
  }

  public CustomButton(String text, ImageStorage imageStorage, int xPos, int yPos) {
    int width = imageStorage.getImage().getWidth(null);
    int height = imageStorage.getImage().getHeight(null);
    this(text, imageStorage, xPos, yPos, width, height);
  }

  /**
   * 元のテキストを返す。
   *
   * @return テキスト
   */
  public String getTextContent() {
    return textContent;
  }

  /**
   * マウス押下・解放時の拡大縮小率を設定する。
   * 
   * @param scaleFactor 拡大縮小率 (例: 0.9f は90%)
   */
  public void setScaleFactor(float scaleFactor) {
    this.ScaleFactor = scaleFactor;
  }

  /**
   * アニメーションの継続時間を設定する。
   * 
   * @param duration 継続時間(ms)
   */
  public void setAnimationDuration(int duration) {
    this.AnimationDuration = duration;
  }

  /**
   * 表示位置とサイズを設定し、内部の基準座標を更新する。
   *
   * @param x      左上X
   * @param y      左上Y
   * @param width  幅
   * @param height 高さ
   */
  public final void setCustomBounds(int x, int y, int width, int height) {
    setBounds(x, y, width, height);
    this.setPreferredSize(new Dimension(width, height));
    this.originalWidth = width;
    this.originalHeight = height;
    this.centerX = x + width / 2;
    this.centerY = y + height / 2;
  }

  /**
   * レイアウト更新時に位置とサイズを更新する。
   *
   * @param x      左上X
   * @param y      左上Y
   * @param width  幅
   * @param height 高さ
   */
  public void updateLayout(int x, int y, int width, int height) {
    setCustomBounds(x, y, width, height);
    if (originalImage != null) {
      setImage(originalImage, width, height);
    }
  }

  private void setBoundsCentered(int width, int height) {
    if (originalImage != null) {
      setImage(originalImage, width, height);
    } else {
      int x = centerX - width / 2;
      int y = centerY - height / 2;
      setBounds(x, y, width, height);
      setPreferredSize(new Dimension(width, height));
    }
  }

  @Override
  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    this.centerX = x + width / 2;
    this.centerY = y + height / 2;
  }

  /**
   * 表示画像とサイズを設定する。
   *
   * @param image  画像
   * @param width  幅
   * @param height 高さ
   */
  public final void setImage(Image image, int width, int height) {
    // 通常のボタンの外観を非表示にする
    this.setContentAreaFilled(false);
    this.setBorderPainted(false);
    this.setText("");
    // 通常時の画像を設定
    ImageIcon icon = new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    this.setIcon(icon);
    this.setHideActionText(true);
    // 押下時の画像を通常時の画像の少し暗くしたバージョンに設定
    ImageIcon pressedIcon = new ImageIcon(darkenImage(icon.getImage(), width, height, 0.7f));
    this.setPressedIcon(pressedIcon);
    // フォーカス時の画像を通常時の画像の少し明るくしたバージョンに設定
    ImageIcon rolloverIcon = new ImageIcon(brightenImage(icon.getImage(), width, height, 1.3f));
    this.setRolloverIcon(rolloverIcon);
  }

  /**
   * 画像を暗くします
   * 
   * @param image    元の画像
   * @param strength スケール値（0.0～1.0、小さいほど暗くなります）
   * @return 暗くされた画像
   */
  private Image darkenImage(Image image, int width, int height, float strength) {
    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    bufferedImage.getGraphics().drawImage(image, 0, 0, null);

    RescaleOp rescaleOp = new RescaleOp(strength, 0, null);
    return rescaleOp.filter(bufferedImage, null);
  }

  /**
   * 画像を明るくします
   * 
   * @param image    元の画像
   * @param strength スケール値（1.0より大きい値で明るくなります）
   * @return 明るくされた画像
   */
  private Image brightenImage(Image image, int width, int height, float strength) {
    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    bufferedImage.getGraphics().drawImage(image, 0, 0, null);

    RescaleOp rescaleOp = new RescaleOp(strength, 0, null);
    return rescaleOp.filter(bufferedImage, null);
  }

  // ボタン押下時にボタンのサイズをアニメーションさせる

  private void startPressAnimation() {
    // 既存のアニメーションスレッドがあれば中断
    if (animationThread != null && animationThread.isAlive()) {
      animationThread.interrupt();
    }

    // 押下時：縮小アニメーション
    animationThread = new Thread(() -> {
      long startTime = System.currentTimeMillis();
      double startProgress = this.animationProgress;
      int targetWidth = (int) (originalWidth * ScaleFactor);
      int targetHeight = (int) (originalHeight * ScaleFactor);

      while (this.animationProgress < 1.0) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        this.animationProgress = (double) elapsed / AnimationDuration + startProgress;

        int currentWidth = (int) (originalWidth + (targetWidth - originalWidth) * this.animationProgress);
        if (currentWidth < 1) {
          System.out.println("currentWidth is negative: " + currentWidth + ", originalWidth: " + originalWidth
              + ", targetWidth: " + targetWidth + ", animationProgress: " + this.animationProgress);
        }
        int currentHeight = (int) (originalHeight + (targetHeight - originalHeight) * this.animationProgress);

        CustomButton.this.setBoundsCentered(currentWidth, currentHeight);
        // 画像のサイズを更新
        if (CustomButton.this.originalImage != null) {
          CustomButton.this.setImage(CustomButton.this.originalImage, currentWidth, currentHeight);
        }

        CustomButton.this.revalidate();
        CustomButton.this.repaint();

        try {
          Thread.sleep(16); // 約60FPS
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    });
    animationThread.start();
  }

  private void startReleaseAnimation() {
    // 既存のアニメーションスレッドがあれば中断
    if (animationThread != null && animationThread.isAlive()) {
      animationThread.interrupt();
    }

    // 解放時：拡大アニメーション
    animationThread = new Thread(() -> {
      long startTime = System.currentTimeMillis();
      double startProgress = this.animationProgress;
      int targetWidth = (int) (originalWidth * ScaleFactor);
      int targetHeight = (int) (originalHeight * ScaleFactor);
      while (0 < this.animationProgress) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        this.animationProgress = -(double) elapsed / AnimationDuration + startProgress;

        int currentWidth = (int) (originalWidth + (targetWidth - originalWidth) * this.animationProgress);
        int currentHeight = (int) (originalHeight + (targetHeight - originalHeight) * this.animationProgress);

        CustomButton.this.setBoundsCentered(currentWidth, currentHeight);
        // 画像のサイズを更新
        if (CustomButton.this.originalImage != null) {
          CustomButton.this.setImage(CustomButton.this.originalImage, currentWidth, currentHeight);
        }

        CustomButton.this.revalidate();
        CustomButton.this.repaint();

        try {
          Thread.sleep(16); // 約60FPS
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          break;
        }
      }
      // this.animationProgress = 0.0;
    });
    animationThread.start();
  }

  /** {@inheritDoc} */
  @Override
  public void removeNotify() { // コンポーネントが破棄されるときにアニメーションを停止
    if (animationThread != null && animationThread.isAlive()) {
      animationThread.interrupt();
    }
    super.removeNotify();
  }
}
