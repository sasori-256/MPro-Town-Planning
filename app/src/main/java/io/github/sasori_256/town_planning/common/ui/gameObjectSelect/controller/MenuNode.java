package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * メニューのノードを表すインターフェース.
 * @implNote MenuNodeはActionListenerを継承しており、メニュー項目が選択されたときの動作を定義できる.
 */
public interface MenuNode extends ActionListener {
    String getName();
    boolean isLeaf();
    ArrayList<MenuNode> getChildren();
}
