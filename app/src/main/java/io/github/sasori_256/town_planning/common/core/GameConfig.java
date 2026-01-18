package io.github.sasori_256.town_planning.common.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.ConfigLoadFailedEvent;

/**
 * ゲーム設定をリソースから読み込むユーティリティ。
 */
public final class GameConfig {
  private static final List<ConfigError> LOAD_ERRORS = new ArrayList<>();
  private static final Properties PROPERTIES = loadProperties();
  private static boolean errorsReported;

  private GameConfig() {
  }

  public static double getCorpseHarvestDelaySeconds() {
    return getDouble("corpse.harvestDelaySeconds", 2.0);
  }

  public static int getCorpseSoulBase() {
    return getNonNegativeInt("corpse.soulBase", 10);
  }

  public static int getCorpseSoulFaithDivisor() {
    return getPositiveInt("corpse.soulFaithDivisor", 5);
  }

  public static void preload() {
    getCorpseHarvestDelaySeconds();
    getCorpseSoulBase();
    getCorpseSoulFaithDivisor();
  }

  public static void reportErrors(EventBus eventBus) {
    if (eventBus == null) {
      return;
    }
    List<ConfigError> errors;
    synchronized (GameConfig.class) {
      if (errorsReported) {
        return;
      }
      errorsReported = true;
      errors = new ArrayList<>(LOAD_ERRORS);
      LOAD_ERRORS.clear();
    }
    for (ConfigError error : errors) {
      eventBus.publish(new ConfigLoadFailedEvent(error.code, error.message));
    }
  }

  private static Properties loadProperties() {
    Properties props = new Properties();
    try (InputStream input = GameConfig.class.getClassLoader()
        .getResourceAsStream("game.properties")) {
      if (input == null) {
        recordError("CONFIG_NOT_FOUND", "game.properties not found.");
        return props;
      }
      props.load(input);
    } catch (IOException e) {
      recordError("CONFIG_LOAD_FAILED", "Failed to load game.properties: " + e.getMessage());
    }
    return props;
  }

  private static double getDouble(String key, double defaultValue) {
    String raw = PROPERTIES.getProperty(key);
    if (raw == null) {
      return defaultValue;
    }
    try {
      return Double.parseDouble(raw.trim());
    } catch (NumberFormatException e) {
      recordError("CONFIG_PARSE_FAILED", "Invalid double for " + key + ": " + raw);
      return defaultValue;
    }
  }

  private static int getNonNegativeInt(String key, int defaultValue) {
    int value = getInt(key, defaultValue);
    return value < 0 ? defaultValue : value;
  }

  private static int getPositiveInt(String key, int defaultValue) {
    int value = getInt(key, defaultValue);
    return value <= 0 ? defaultValue : value;
  }

  private static int getInt(String key, int defaultValue) {
    String raw = PROPERTIES.getProperty(key);
    if (raw == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(raw.trim());
    } catch (NumberFormatException e) {
      recordError("CONFIG_PARSE_FAILED", "Invalid int for " + key + ": " + raw);
      return defaultValue;
    }
  }

  private static void recordError(String code, String message) {
    LOAD_ERRORS.add(new ConfigError(code, message));
  }

  private static final class ConfigError {
    private final String code;
    private final String message;

    private ConfigError(String code, String message) {
      this.code = code;
      this.message = message;
    }
  }
}
