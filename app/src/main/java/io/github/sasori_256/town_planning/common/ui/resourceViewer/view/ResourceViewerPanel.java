package io.github.sasori_256.town_planning.common.ui.resourceViewer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JLabel;

import io.github.sasori_256.town_planning.common.ui.CustomPanel;

/**
 * 背景画像の上にリソースの値を表示するためのパネル。
 * CustomPanelを継承し、背景画像付きの表示と値ラベルの配置を行う。
 */
public class ResourceViewerPanel extends CustomPanel {
    private JLabel valueLabel;

    public ResourceViewerPanel(String displayValue, Image bgImage) {
        super(bgImage);
        initUI(displayValue);
    }

    private void initUI(String displayValue) {
        valueLabel = new JLabel(displayValue);
        valueLabel.setForeground(Color.BLACK);
        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        // valueLabel.setFont(); TODO: フォント設定
        this.setLayout(new BorderLayout());
        this.add(valueLabel, BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(300, 100));
    }

    public void setDisplayValue(String displayValue) {
        valueLabel.setText(displayValue);
    }
}
