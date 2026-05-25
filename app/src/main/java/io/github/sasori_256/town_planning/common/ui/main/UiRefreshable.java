package io.github.sasori_256.town_planning.common.ui.main;

/**
 * 画面リサイズ時などにUIを再配置するための契約。
 */
public interface UiRefreshable {
  /**
   * UIの再描画・再配置を実行する。
   */
  void repaintUI();
}
