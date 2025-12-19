package io.github.sasori_256.town_planning.common.event.events;

/**
 * 魂を収穫したことを通知するイベント。
 * 獲得量を含む。
 */
public record SoulHarvestedEvent(int amount) {
}
