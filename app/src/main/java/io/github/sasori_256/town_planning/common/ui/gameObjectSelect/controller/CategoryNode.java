package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.ArrayList;

/**
 * メニューのカテゴリノード。
 */
public class CategoryNode implements MenuNode {
    private final String name;
    private ArrayList<MenuNode> children = new ArrayList<>();

    /**
     * カテゴリノードを生成する。
     *
     * @param name 表示名
     */
    public CategoryNode(String name) { this.name = name; }

    /**
     * 子ノードを追加する。
     *
     * @param node 追加するノード
     */
    public void add(MenuNode node) { children.add(node); }
    
    /** {@inheritDoc} */
    @Override
    public String getName() { return name; }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() { return false; }

    /** {@inheritDoc} */
    @Override
    public ArrayList<MenuNode> getChildren() { return children; }
    
    /** {@inheritDoc} */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        //TODO: Viewと連携する.
    }

}
