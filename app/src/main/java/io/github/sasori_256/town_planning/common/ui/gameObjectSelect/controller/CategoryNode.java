package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.ArrayList;

public class CategoryNode implements MenuNode {
    private final String name;
    private ArrayList<MenuNode> children = new ArrayList<>();

    public CategoryNode(String name) { this.name = name; }
    public void add(MenuNode node) { children.add(node); }
    
    @Override
    public String getName() { return name; }

    @Override
    public Boolean isLeaf() { return false; }

    @Override
    public ArrayList<MenuNode> getChildren() { return children; }
    
}
