package io.github.sasori_256.town_planning.core;

import io.github.sasori_256.town_planning.core.strategy.RenderStrategy;
import io.github.sasori_256.town_planning.core.strategy.UpdateStrategy;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ゲーム内の全ての動的オブジェクトの基底クラス。
 * Strategyパターンを用いて振る舞いを定義する。
 * 継承による拡張ではなく、コンポジション（Strategy）による機能追加を推奨する。
 */
public class GameObject implements GameEntity {
  private final String id;
  private Point2D position;

  // Strategies
  private UpdateStrategy updateStrategy;
  private RenderStrategy renderStrategy;

  // 汎用的な属性ストレージ (ECSのComponentの簡易版)
  private final Map<String, Object> attributes = new HashMap<>();

  public GameObject(Point2D position) {
    this.id = UUID.randomUUID().toString();
    this.position = position;
    // Default strategies (No-op)
    this.updateStrategy = (ctx, self) -> {
    };
    this.renderStrategy = (g, self) -> {
    };
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Point2D getPosition() {
    return position;
  }

  public void setPosition(Point2D position) {
    this.position = position;
  }

  public void setUpdateStrategy(UpdateStrategy updateStrategy) {
    this.updateStrategy = updateStrategy;
  }

  public void setRenderStrategy(RenderStrategy renderStrategy) {
    this.renderStrategy = renderStrategy;
  }

  public void update(GameContext context) {
    if (updateStrategy != null) {
      updateStrategy.update(context, this);
    }
  }

  public void render(Graphics2D g) {
    if (renderStrategy != null) {
      renderStrategy.render(g, this);
    }
  }

  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttribute(String key) {
    return (T) attributes.get(key);
  }
}
