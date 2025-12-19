package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.awt.event.ActionListener;
import java.util.ArrayList;

public interface MenuNode extends ActionListener {
    String getName();
    Boolean isLeaf();
    ArrayList<MenuNode> getChildren();
}
