package io.github.sasori_256.town_planning.common.event.events;

/**
 * エンティティ生成失敗の理由。
 */
public enum EntitySpawnFailureReason {
  INSUFFICIENT_SOUL,
  INVALID_POSITION,
  PLACEMENT_BLOCKED,
  INVALID_ENTITY,
  UNKNOWN
}
