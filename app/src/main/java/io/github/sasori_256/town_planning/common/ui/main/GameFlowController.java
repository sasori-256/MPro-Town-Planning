package io.github.sasori_256.town_planning.common.ui.main;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Timer;

import io.github.sasori_256.town_planning.common.core.GameConfig;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.Subscription;
import io.github.sasori_256.town_planning.common.event.events.ConfigLoadFailedEvent;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailedEvent;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailureReason;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnKind;
import io.github.sasori_256.town_planning.common.event.events.GameOverEvent;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.ToastManager;
import io.github.sasori_256.town_planning.common.ui.main.scene.EndPanel;
import io.github.sasori_256.town_planning.common.ui.main.scene.TitlePanel;

public class GameFlowController implements GameFlowNavigator {
  private final GameWindow window;
  private final ImageManager imageManager;
  private final int windowWidth;
  private final int windowHeight;
  private final int mapWidth;
  private final int mapHeight;
  private final long seed;

  private TitlePanel titlePanel;
  private EndPanel endPanel;
  private GameSession currentSession;
  private Subscription configSub;
  private Subscription spawnSub;
  private Subscription gameOverSub;
  private Timer gameOverTimer;
  private final AtomicBoolean gameOverHandled = new AtomicBoolean(false);

  public GameFlowController(
      GameWindow window,
      ImageManager imageManager,
      int windowWidth,
      int windowHeight,
      int mapWidth,
      int mapHeight,
      long seed) {
    this.window = window;
    this.imageManager = imageManager;
    this.windowWidth = windowWidth;
    this.windowHeight = windowHeight;
    this.mapWidth = mapWidth;
    this.mapHeight = mapHeight;
    this.seed = seed;

    this.window.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        updateCameraToWindow();
      }
    });
  }

  public void initialize() {
    this.titlePanel = new TitlePanel(this, imageManager);
    this.endPanel = new EndPanel(this);
    window.setScene(SceneId.TITLE, titlePanel);
    window.setScene(SceneId.END, endPanel);
    window.showScene(SceneId.TITLE);
  }

  @Override
  public void startNewGame() {
    clearGameOverTimer();
    disposeSession();
    gameOverHandled.set(false);

    currentSession = new GameSession(windowWidth, windowHeight, mapWidth, mapHeight, seed, imageManager, this);
    window.setScene(SceneId.GAME, currentSession.getView());
    window.showScene(SceneId.GAME);
    attachSessionSubscriptions(currentSession);
    updateCameraToWindow();
    currentSession.start(window::repaint);
  }

  @Override
  public void goToTitle() {
    clearGameOverTimer();
    disposeSession();
    window.showScene(SceneId.TITLE);
  }

  private void attachSessionSubscriptions(GameSession session) {
    EventBus eventBus = session.getEventBus();
    configSub = eventBus.subscribe(ConfigLoadFailedEvent.class,
        event -> window.showToast(buildConfigMessage(event), ToastManager.ToastType.ERROR));
    spawnSub = eventBus.subscribe(EntitySpawnFailedEvent.class,
        event -> window.showToast(buildSpawnFailureMessage(event), ToastManager.ToastType.WARNING));
    gameOverSub = eventBus.subscribe(GameOverEvent.class, event -> handleGameOver());
    GameConfig.reportErrors();
  }

  private void handleGameOver() {
    if (currentSession == null) {
      return;
    }
    if (!gameOverHandled.compareAndSet(false, true)) {
      return;
    }
    window.showToast("住民がいなくなりました。", ToastManager.ToastType.INFO);
    currentSession.pause();

    GameResult result = currentSession.snapshot();
    endPanel.setResult(result);
    scheduleGameOverTransition();
  }

  private void scheduleGameOverTransition() {
    clearGameOverTimer();
    gameOverTimer = new Timer(3000, event -> {
      disposeSession();
      window.showScene(SceneId.END);
    });
    gameOverTimer.setRepeats(false);
    gameOverTimer.start();
  }

  private void clearGameOverTimer() {
    if (gameOverTimer != null) {
      gameOverTimer.stop();
      gameOverTimer = null;
    }
  }

  private void disposeSession() {
    if (currentSession != null) {
      currentSession.dispose();
      currentSession = null;
    }
    unsubscribe(configSub);
    unsubscribe(spawnSub);
    unsubscribe(gameOverSub);
    configSub = null;
    spawnSub = null;
    gameOverSub = null;
  }

  private void updateCameraToWindow() {
    if (currentSession == null) {
      return;
    }
    Dimension size = window.getSceneSize();
    if (size.width <= 0 || size.height <= 0) {
      return;
    }
    currentSession.updateCameraScreenSize(size.width, size.height);
  }

  private void unsubscribe(Subscription subscription) {
    if (subscription != null) {
      subscription.unsubscribe();
    }
  }

  private static String buildConfigMessage(ConfigLoadFailedEvent event) {
    String code = event.code();
    if ("CONFIG_NOT_FOUND".equals(code)) {
      return "設定ファイルが見つかりません (game.properties)";
    }
    if ("CONFIG_LOAD_FAILED".equals(code)) {
      return "設定ファイルの読み込みに失敗しました";
    }
    if ("CONFIG_PARSE_FAILED".equals(code)) {
      return "設定値が不正です: " + safeDetail(event.message());
    }
    return "設定読み込みエラー: " + safeDetail(event.message());
  }

  private static String buildSpawnFailureMessage(EntitySpawnFailedEvent event) {
    String kindName = describeKind(event.kind());
    EntitySpawnFailureReason reason = event.reason();
    if (reason == EntitySpawnFailureReason.INSUFFICIENT_SOUL) {
      return kindName + "を生成できません: 魂が足りません";
    }
    if (reason == EntitySpawnFailureReason.INVALID_POSITION) {
      return kindName + "を生成できません: 設置できない場所です";
    }
    if (reason == EntitySpawnFailureReason.PLACEMENT_BLOCKED) {
      return kindName + "を生成できません: 設置場所が塞がっています";
    }
    if (reason == EntitySpawnFailureReason.INVALID_ENTITY) {
      return kindName + "の生成に失敗しました: 対象が不正です";
    }
    return kindName + "の生成に失敗しました";
  }

  private static String describeKind(EntitySpawnKind kind) {
    if (kind == EntitySpawnKind.DISASTER) {
      return "災害";
    }
    return "建物";
  }

  private static String safeDetail(String detail) {
    return detail == null || detail.isBlank() ? "詳細不明" : detail;
  }
}
