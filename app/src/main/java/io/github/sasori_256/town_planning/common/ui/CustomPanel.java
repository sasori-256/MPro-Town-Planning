package io.github.sasori_256.town_planning.common.ui;

import java.awt.Image;

import javax.swing.JPanel;

/**
 * パネルにマウスホバーした時の処理などを共通化するために使用するカスタムパネル
 */
public class CustomPanel extends JPanel {
    private Image bgImage;

    /**
     * イメージを背景に持つカスタムパネル
     * 
     * @param bgImage
     */
    public CustomPanel(Image bgImage) {
        super();
        this.bgImage = bgImage;
        this.setOpaque(false);
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (this.bgImage != null) {
            g.drawImage(this.bgImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }
}
