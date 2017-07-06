package com.github.okvee.maven.tscfg;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ConfigFile {

  private final String moduleName;
  private final List<KeyValue> keyValues;

  public ConfigFile(String moduleName, Config config) {
    this.moduleName = moduleName;
    this.keyValues = new ArrayList<>(config.entrySet().size());
    for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
      keyValues.add(new KeyValue(entry));
    }
    keyValues.sort(Comparator.comparing(KeyValue::getName));
  }

  public String getModuleName() {
    return moduleName;
  }

  public List<KeyValue> getKeyValues() {
    return keyValues;
  }

  public static class KeyValue {
    private final String name;
    private final String description;
    private final String valueType;
    private final String defaultValue;

    KeyValue(Map.Entry<String, ConfigValue> keyValue) {
      name = escapeMarkdown(keyValue.getKey());
      description = escapeMarkdown(getComment(keyValue.getValue()));
      valueType = keyValue.getValue().valueType().name().toLowerCase();
      defaultValue = escapeMarkdown(keyValue.getValue().unwrapped().toString());
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public String getValueType() {
      return valueType;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    private static String getComment(ConfigValue value) {
      StringBuilder sb = new StringBuilder();
      for (String line : value.origin().comments()) {
        if (sb.length() > 0) {
          sb.append(' ');
        }
        sb.append(line.trim());
      }
      return sb.toString();
    }

    private static String escapeMarkdown(String str) {
      return str.replace("|", "&#124;");
    }
  }
}
