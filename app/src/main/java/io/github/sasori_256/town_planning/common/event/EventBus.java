package io.github.sasori_256.town_planning.common.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.Objects;

/**
 * 型安全なイベントバスの実装。
 * イベントクラス（Record推奨）をキーとしてPub/Subを行う。
 * EventBusはシングルトンとして実装している。
 * 使用する際は各クラスでEventBus.getInstance()によりインスタンスを取得し、フィールドとして持つこと。
 * ただしstaticなメソッドで使用する場合は都度EventBus.getInstance()で取得して使用すること。
 */
public class EventBus {
  private static final EventBus instance = new EventBus();
  private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

  private EventBus() {
  }

  /**
   * イベントバスのシングルトンインスタンスを取得する。
   *
   * @return イベントバスのインスタンス
   */
  public static EventBus getInstance() {
    return instance;
  }

  /**
   * イベントを購読する。
   *
   * <h2>購読方法</h2>
   *
   * イベントを購読(値の変更などを検知)したいオブジェクトに対して以下を適用する。
   *
   * ```java
   * Subscription sub =
   * eventBus.subscribe(購読したいイベント.class, event -> {
   * イベント発生時に実行される処理;
   * });
   * ```
   *
   * また、Windowパネルなどが破棄されるときにはunsubscribeが必要になる。
   * unsubscribeのための関数はsubに入っているので、そこからunsubscribeを実行すれば良い。
   *
   * ```java
   * sub.unsubscribe();
   * ```
   *
   * @param <T>       イベントの型
   * @param eventType 購読したいイベントのクラスオブジェクト
   * @param listener  イベント発生時に実行される処理
   * @return 購読解除用のSubscriptionオブジェクト
   */
  public <T> Subscription subscribe(Class<T> eventType, Consumer<T> listener) {
    Objects.requireNonNull(eventType, "eventType");
    Objects.requireNonNull(listener, "listener");
    List<Consumer<?>> list = listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
    list.add(listener);

    // 購読解除用のアクションを返す
    AtomicBoolean removed = new AtomicBoolean(false);
    return () -> {
      if (!removed.compareAndSet(false, true)) {
        return;
      }
      list.remove(listener);
      if (list.isEmpty()) {
        listeners.remove(eventType, list);
      }
    };
  }

  /**
   * イベントを発行する。
   *
   * <h2>発行方法</h2>
   *
   * 以下により発行したいイベントとその引数を指定することでイベントの発行が可能。
   *
   * ```java
   * eventBus.publish(発行したいイベントのRecordクラス(必要な引数));
   * ```
   *
   * 引数では、SoulChangedEvent(currentSoul)のようにフィールドを変更したときの数値など、伝達するべき値を引数に入れる。
   *
   * @param event 発行するイベントオブジェクト
   * @implNote リスナー内の例外は捕捉し、後続リスナーの実行は継続する。
   */
  public void publish(Object event) {
    Objects.requireNonNull(event, "event");
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
