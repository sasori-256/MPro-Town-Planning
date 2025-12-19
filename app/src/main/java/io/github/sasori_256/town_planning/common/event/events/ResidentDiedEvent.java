package io.github.sasori_256.town_planning.common.event.events;

import io.github.sasori_256.town_planning.gameobject.resident.Resident;

/**
 * 住民が死亡したことを通知するイベント。
 */
public record ResidentDiedEvent(Resident resident) {
}