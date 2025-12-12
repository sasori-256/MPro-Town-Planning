package io.github.sasori_256.town_planning.common.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventBus {
  private final Map<EventType, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

  public void subscribe(EventType type, Consumer<Object> listener) {
    listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
  }

  public void publish(EventType type, Object data) {
    List<Consumer<Object>> typeListeners = listeners.get(type);
    if (typeListeners != null) {
      typeListeners.forEach(l -> l.accept(data));
    }
  }
}
