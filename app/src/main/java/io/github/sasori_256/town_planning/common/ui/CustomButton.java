
package io.github.sasori_256.town_planning.common.ui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.ImageIcon;
import javax.swing.JButton;

class CustomButton extends JButton {
  private int originalWidth;
  private int originalHeight;
  private int centerX;
  private int centerY;
  private static final float SCALE_FACTOR = 0.95f; // 押下時に95%のサイズに変更
  private static final int ANIMATION_DURATION = 100; // アニメーション時間（ミリ秒）
  private Thread animationThread;

  public CustomButton(String text) {
    super(text);
    // ボタンモデルにリスナーを追加
    getModel().addChangeListener(e -> {
      if (getModel().isPressed()) {
        startPressAnimation();
      } else {
        startReleaseAnimation();
      }
    });
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
    int x = centerX - width / 2;
    int y = centerY - height / 2;
    super.setBounds(x, y, width, height);
    setPreferredSize(new Dimension(width, height));
  }

  public void setImage(Image image, int width, int height) {
    // 通常のボタンの外観を非表示にする
    this.setContentAreaFilled(false);
    this.setBorderPainted(false);
    // 通常時の画像を設定
    ImageIcon icon = new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    this.setIcon(icon);
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
      int targetWidth = (int) (originalWidth * SCALE_FACTOR);
      int targetHeight = (int) (originalHeight * SCALE_FACTOR);

      while (System.currentTimeMillis() - startTime < ANIMATION_DURATION) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        float progress = (float) elapsed / ANIMATION_DURATION;

        int currentWidth = (int) (originalWidth + (targetWidth - originalWidth) * progress);
        int currentHeight = (int) (originalHeight + (targetHeight - originalHeight) * progress);

        CustomButton.this.setBoundsCentered(currentWidth, currentHeight);
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
      Dimension currentSize = CustomButton.this.getPreferredSize();
      int startWidth = currentSize.width;
      int startHeight = currentSize.height;

      while (System.currentTimeMillis() - startTime < ANIMATION_DURATION) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        float progress = (float) elapsed / ANIMATION_DURATION;

        int currentWidth = (int) (startWidth + (originalWidth - startWidth) * progress);
        int currentHeight = (int) (startHeight + (originalHeight - startHeight) * progress);

        CustomButton.this.setBoundsCentered(currentWidth, currentHeight);
        CustomButton.this.revalidate();
        CustomButton.this.repaint();

        try {
          Thread.sleep(16); // 約60FPS
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          break;
        }
      }

      // 最終的に元のサイズに戻す
      CustomButton.this.setSize(new Dimension(originalWidth,
          originalHeight));
      CustomButton.this.revalidate();
      CustomButton.this.repaint();
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