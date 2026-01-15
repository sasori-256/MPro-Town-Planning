package io.github.sasori_256.town_planning.common.ui;

import java.awt.Image;

import javax.swing.JPanel;

/**
 * JPanelを継承し、背景画像付きの表示を行う。
 */
public class CustomPanel extends JPanel {
    private final Image bgImage;

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
