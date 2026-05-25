package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * メニューのノードを表すインターフェース。
 *
 * @implNote MenuNodeはActionListenerを継承しており、選択時の動作を定義できる。
 */
public interface MenuNode extends ActionListener {
    /**
     * 表示名を返す。
     *
     * @return 表示名
     */
    String getName();

    /**
     * 末端ノードかどうかを返す。
     *
     * @return 末端ならtrue
     */
    boolean isLeaf();

    /**
     * 子ノードを返す。
     *
     * @return 子ノード一覧
     */
    ArrayList<MenuNode> getChildren();
}
