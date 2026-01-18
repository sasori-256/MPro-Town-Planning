package io.github.sasori_256.town_planning.common.event.events;

import java.awt.geom.Point2D;

/**
 * エンティティ生成に失敗したことを通知するイベント。
 */
public record EntitySpawnFailedEvent(
    EntitySpawnKind kind,
    EntitySpawnFailureReason reason,
    Point2D.Double position,
    String detail) {
}
