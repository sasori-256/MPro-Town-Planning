package io.github.sasori_256.town_planning.common.ui.buildPreview.controller;

import javax.swing.SwingUtilities;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.Subscription;
import io.github.sasori_256.town_planning.common.event.events.CancelBuildEvent;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.event.events.TemporaryBuildEvent;
import io.github.sasori_256.town_planning.common.ui.main.scene.GameMapPanel;

/**
 * 建物プレビュー表示のイベント購読とUI更新を担当するコントローラ。
 */
public class BuildPreviewController {
  private final EventBus eventBus = EventBus.getInstance();
  private final GameMapPanel mapPanel;
  private Subscription temporarySub;
  private Subscription mapSub;
  private Subscription cancelSub;

  /**
   * 建物プレビュー用のイベント連携を初期化する。
   *
   * @param mapPanel 表示対象のマップパネル
   */
  public BuildPreviewController(GameMapPanel mapPanel) {
    this.mapPanel = mapPanel;
    subscribe();
  }

  /**
   * 購読を解除して後始末する。
   */
  public void dispose() {
    unsubscribe(temporarySub);
    unsubscribe(mapSub);
    unsubscribe(cancelSub);
    temporarySub = null;
    mapSub = null;
    cancelSub = null;
  }

  private void subscribe() {
    temporarySub = eventBus.subscribe(TemporaryBuildEvent.class, event -> runOnEdt(() -> {
      mapPanel.showBuildPreview();
    }));
    mapSub = eventBus.subscribe(MapUpdatedEvent.class, event -> runOnEdt(() -> {
      mapPanel.updateBuildPreviewPosition();
    }));
    cancelSub = eventBus.subscribe(CancelBuildEvent.class, event -> runOnEdt(() -> {
      mapPanel.hideBuildPreview();
    }));
  }

  private void runOnEdt(Runnable action) {
    if (SwingUtilities.isEventDispatchThread()) {
      action.run();
    } else {
      SwingUtilities.invokeLater(action);
    }
  }

  private void unsubscribe(Subscription subscription) {
    if (subscription != null) {
      subscription.unsubscribe();
    }
  }
}
