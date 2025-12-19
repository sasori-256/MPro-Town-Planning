package io.github.sasori_256.town_planning.common.event.events;

import java.awt.geom.Point2D;

/**
 * マップ上の何かが更新された（建物建設など）ことを通知するイベント。
 */
public record MapUpdatedEvent(Point2D.Double position) {
}
