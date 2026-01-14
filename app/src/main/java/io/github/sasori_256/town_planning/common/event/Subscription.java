package io.github.sasori_256.town_planning.common.event;

/**
 * 購読解除を行うためのインターフェース。
 */
@FunctionalInterface
public interface Subscription {
  /**
   * 購読を解除する。
   */
  void unsubscribe();
}
