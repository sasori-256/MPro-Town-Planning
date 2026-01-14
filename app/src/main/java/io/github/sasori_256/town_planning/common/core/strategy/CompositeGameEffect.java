package io.github.sasori_256.town_planning.common.core.strategy;

import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.model.GameEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * 複数のGameEffectをまとめて実行するコンポジットクラス。
 */
public class CompositeGameEffect implements GameEffect {
  private final List<GameEffect> effects = new ArrayList<>();

  /**
   * エフェクトを追加する。
   *
   * @param effect エフェクト
   */
  public void add(GameEffect effect) {
    effects.add(effect);
  }

  /**
   * エフェクトを削除する。
   *
   * @param effect エフェクト
   */
  public void remove(GameEffect effect) {
    effects.remove(effect);
  }

  /** {@inheritDoc} */
  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    // リストのコピーを作成してConcurrentModificationExceptionを防ぐ（簡易実装）
    // パフォーマンスがクリティカルになる場合はイテレーション方法を見直す
    for (GameEffect effect : new ArrayList<>(effects)) {
      effect.execute(context, self);
    }
  }
}
