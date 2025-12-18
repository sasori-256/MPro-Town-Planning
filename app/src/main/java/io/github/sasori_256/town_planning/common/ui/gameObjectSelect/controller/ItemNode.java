package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.awt.event.ActionListener;

import io.github.sasori_256.town_planning.gameObject.model.BaseGameEntity;

public class ItemNode implements MenuNode, ActionListener {
    private final String name;
    private final Supplier<BaseGameEntity> generator;
    private final Consumer<Supplier<BaseGameEntity>> onClickAction;

    public ItemNode(String name, Supplier<BaseGameEntity> generator, Consumer<Supplier<BaseGameEntity>> onClickAction) {
        this.name = name;
        this.generator = generator;
        this.onClickAction = onClickAction;
    }

    @Override
    public String getName() { return name;}

    @Override
    public Boolean isLeaf() { return true; }

    @Override
    public ArrayList<MenuNode> getChildren() { 
        System.err.println("Error: Attempted to get children from ItemNode");
        return null;   
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        onClickAction.accept(generator);
    }
}