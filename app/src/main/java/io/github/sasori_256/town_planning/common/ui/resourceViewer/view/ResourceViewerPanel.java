package io.github.sasori_256.town_planning.common.ui.resourceViewer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JLabel;

import io.github.sasori_256.town_planning.common.ui.CustomLabel;
import io.github.sasori_256.town_planning.common.ui.CustomPanel;

/**
 * 背景画像の上にリソースの値を表示するためのパネル。
 * CustomPanelを継承し、背景画像付きの表示と値ラベルの配置を行う。
 */
public class ResourceViewerPanel extends CustomPanel {
    private CustomLabel valueLabel;

    public ResourceViewerPanel(String displayValue, String displayUnit, Image bgImage) {
        super(bgImage);
        initUI(displayValue, displayUnit, bgImage);
    }

    private void initUI(String displayValue, String displayUnit, Image bgImage) {
        valueLabel = new CustomLabel(displayValue + " " + displayUnit);
        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        valueLabel.setOutline(Color.BLACK, 3.0f);
        valueLabel.setTwoTone(new Color(227, 201, 154), new Color(186, 156, 123)); // 薄い黄色と暗い黄色
        this.setLayout(new BorderLayout());
        this.add(valueLabel, BorderLayout.CENTER);
        if (bgImage != null) {
            this.setPreferredSize(new Dimension(bgImage.getWidth(null), bgImage.getHeight(null)));
        } else {
            this.setPreferredSize(new Dimension(300, 100));
        }
    }

    public void setDisplayValue(String displayValue, String displayUnit) {
        valueLabel.setText(displayValue + " " + displayUnit);
    }
}
