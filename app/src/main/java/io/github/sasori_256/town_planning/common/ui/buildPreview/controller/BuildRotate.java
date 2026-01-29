package io.github.sasori_256.town_planning.common.ui.buildPreview.controller;

import java.awt.event.ActionListener;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.RotateBuildEvent;

public class BuildRotate implements ActionListener {
  @Override
  public void actionPerformed(java.awt.event.ActionEvent e) {
    EventBus eventBus = EventBus.getInstance();
    eventBus.publish(new RotateBuildEvent());
  }
}
