package io.github.sasori_256.town_planning.common.event.events;

import io.github.sasori_256.town_planning.gameobject.disaster.DisasterType;

/**
 * 災害が発生したことを通知するイベント。
 */
public record DisasterOccurredEvent(DisasterType disasterType) {
}
