package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.command;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.gameObject.model.BaseGameEntity;

public class ClickMenuBuildingCommand implements Consumer<Supplier<BaseGameEntity>> {
    @Override
    public void accept(Supplier<BaseGameEntity> generator) {
        
    }
}
