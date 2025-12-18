package io.github.sasori_256.town_planning.common.event.events;

import java.awt.geom.Point2D;

/**
 * 新しい住民が誕生したことを通知するイベント。
 */
public record ResidentBornEvent(Point2D.Double position) {
}
