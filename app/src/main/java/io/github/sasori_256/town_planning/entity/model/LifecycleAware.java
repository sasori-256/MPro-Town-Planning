package io.github.sasori_256.town_planning.entity.model;

/**
 * エンティティのライフサイクルフックを提供する。
 */
public interface LifecycleAware {
  /**
   * エンティティがゲームに追加された直後に呼ばれる。
   *
   * @param context ゲーム内の環境情報
   */
  void onSpawn(GameContext context);

  /**
   * エンティティがゲームから削除される直前に呼ばれる。
   *
   * @param context ゲーム内の環境情報
   */
  void onRemove(GameContext context);
}
