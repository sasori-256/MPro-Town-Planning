package io.github.sasori_256.town_planning.common.event;

@FunctionalInterface
public interface Subscription {
  void unsubscribe();
}
