package io.github.sasori_256.town_planning.common.ui.main;

/**
 * レガシー画面遷移のための簡易インターフェース。
 */
public interface SceneNavigator {
  /**
   * 指定したシーン名へ遷移する。
   *
   * @param sceneName シーン識別名
   */
  void changeScene(String sceneName);
}
