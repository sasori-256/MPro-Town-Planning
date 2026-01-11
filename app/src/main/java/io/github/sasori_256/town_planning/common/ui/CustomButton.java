
package io.github.sasori_256.town_planning.common.ui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;

public class CustomButton extends JButton {
  private int originalWidth;
  private int originalHeight;
  private int centerX;
  private int centerY;
  private final String textContent;
  private static final float SCALE_FACTOR = 0.9f; // 押下時に90%のサイズに変更
  private static final int ANIMATION_DURATION = 100; // アニメーション時間（ミリ秒）
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

  public CustomButton(String text) {
    this.textContent = text;
    this.originalImage = null;
    // いい感じに折り返せるようにHTMLを使う
    super("<html><div style='text-align: center;'>" + text + "</div></html>");
    addChangeListenerToModel();
    textPositionOptimize();
  }

  public CustomButton(String text, ImageStorage imageStorage) {
    this.textContent = text;
    this.originalImage = imageStorage.getImage();
    super();
    addChangeListenerToModel();
    setImage(imageStorage.getImage(), (int) imageStorage.size.x, (int) imageStorage.size.y);
  }

  public String getTextContent() {
    return textContent;
  }

  public void setCustomBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    this.setPreferredSize(new Dimension(width, height));
    this.originalWidth = width;
    this.originalHeight = height;
    this.centerX = x + width / 2;
    this.centerY = y + height / 2;
  }

  private void setBoundsCentered(int width, int height) {
    if (originalImage != null) {
      setImage(originalImage, width, height);
    } else {
      int x = centerX - width / 2;
      int y = centerY - height / 2;
      super.setBounds(x, y, width, height);
      setPreferredSize(new Dimension(width, height));
    }
  }

  public void setImage(Image image, int width, int height) {
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
      int targetWidth = (int) (originalWidth * SCALE_FACTOR);
      int targetHeight = (int) (originalHeight * SCALE_FACTOR);

      while (this.animationProgress < 1.0) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        this.animationProgress = (double) elapsed / ANIMATION_DURATION + startProgress;

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
      int targetWidth = (int) (originalWidth * SCALE_FACTOR);
      int targetHeight = (int) (originalHeight * SCALE_FACTOR);
      while (0 < this.animationProgress) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        this.animationProgress = -(double) elapsed / ANIMATION_DURATION + startProgress;

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

  @Override
  public void removeNotify() { // コンポーネントが破棄されるときにアニメーションを停止
    if (animationThread != null && animationThread.isAlive()) {
      animationThread.interrupt();
    }
    super.removeNotify();
  }
}
