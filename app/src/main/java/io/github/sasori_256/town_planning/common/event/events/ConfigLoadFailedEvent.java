package io.github.sasori_256.town_planning.common.event.events;

/**
 * 設定ファイル読み込みに失敗したことを通知するイベント。
 */
public record ConfigLoadFailedEvent(String code, String message) {
}
