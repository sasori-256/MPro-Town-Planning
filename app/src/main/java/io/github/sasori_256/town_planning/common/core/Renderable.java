package io.github.sasori_256.town_planning.common.core;

/**
 * 描画可能なオブジェクトを表すインターフェース。
 * 各オブジェクトは表示用のシンボルを持つ(予定)。
 */
public interface Renderable {
  String getDisplaySymbol();
}
