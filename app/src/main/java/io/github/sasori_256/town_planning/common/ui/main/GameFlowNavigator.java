package io.github.sasori_256.town_planning.common.ui.main;

/**
 * ゲームの画面遷移を要求するための窓口。
 */
public interface GameFlowNavigator {
  /**
   * 新しいゲームを開始する。
   */
  void startNewGame();

  /**
   * タイトル画面へ遷移する。
   */
  void goToTitle();
}
