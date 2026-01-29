package io.github.sasori_256.town_planning.common.ui;

import java.awt.Image;

import javax.swing.JPanel;

/**
 * JPanelを継承し、背景画像付きの表示を行う。
 */
public class CustomPanel extends JPanel {
    private final Image bgImage;
    private final boolean isFit;

    /**
     * イメージを背景に持つカスタムパネル
     * 
     * @param bgImage 背景として描画するイメージ
     * @param isFit   パネルサイズに合わせてイメージを引き伸ばすかどうか
     */
    public CustomPanel(Image bgImage, boolean isFit) {
        super();
        this.bgImage = bgImage;
        this.isFit = isFit;
        this.setFocusable(false);
        this.setOpaque(false);
    }

    /**
     * イメージを背景に持つカスタムパネル
     * 
     * @param bgImage 背景として描画するイメージ
     */
    public CustomPanel(Image bgImage) {
        this(bgImage, true);
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (this.bgImage != null) {
            if (isFit) {
                g.drawImage(this.bgImage, 0, 0, this.getWidth(), this.getHeight(), this);
            } else {
                g.drawImage(this.bgImage, 0, 0, this);
            }
        }
    }
}
