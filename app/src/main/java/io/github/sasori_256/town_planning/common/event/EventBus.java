package io.github.sasori_256.town_planning.common.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 型安全なイベントバスの実装。
 * イベントクラス（Record推奨）をキーとしてPub/Subを行う。
 */
public class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    /**
     * イベントを購読する。
     * 
     * @param <T> イベントの型
     * @param eventType 購読したいイベントのクラスオブジェクト
     * @param listener イベント発生時に実行される処理
     * @return 購読解除用のSubscriptionオブジェクト
     */
    public <T> Subscription subscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<?>> list = listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
        list.add(listener);

        // 購読解除用のアクションを返す
        return () -> list.remove(listener);
    }

    /**
     * イベントを発行する。
     * 
     * @param event 発行するイベントオブジェクト
     */
    public void publish(Object event) {
        Class<?> type = event.getClass();
        List<Consumer<?>> list = listeners.get(type);
        if (list != null) {
            for (Consumer<?> handler : list) {
                try {
                    @SuppressWarnings("unchecked")
                    Consumer<Object> castedHandler = (Consumer<Object>) handler;
                    castedHandler.accept(event);
                } catch (Exception e) {
                    System.err.println("Error handling event: " + event);
                    e.printStackTrace();
                }
            }
        }
    }
}