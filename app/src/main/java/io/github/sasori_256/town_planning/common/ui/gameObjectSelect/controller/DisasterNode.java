package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

public class DisasterNode implements MenuNode {
    private final String name;
    private final Supplier<BaseGameEntity> generator;
    private final Consumer<Supplier<BaseGameEntity>> onClickAction;

    public DisasterNode(String name, Supplier<BaseGameEntity> generator, Consumer<Supplier<BaseGameEntity>> onClickAction) {
        this.name = name;
        this.generator = generator;
    }

    @Override
    public String getName() { return name;}

    @Override
    public Boolean isLeaf() { return true; }

    @Override
    public ArrayList<MenuNode> getChildren() { 
        System.err.println("Error: Attempted to get children from DisasterNode");
        return null;   
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        
    }
}