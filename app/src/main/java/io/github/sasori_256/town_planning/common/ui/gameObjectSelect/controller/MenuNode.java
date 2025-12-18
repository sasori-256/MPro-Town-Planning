package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.ArrayList;

public interface MenuNode {
    String getName();
    Boolean isLeaf();
    ArrayList<MenuNode> getChildren();
}
