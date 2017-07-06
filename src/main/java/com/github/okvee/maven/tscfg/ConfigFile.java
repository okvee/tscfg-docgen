package com.github.okvee.maven.tscfg;

import com.typesafe.config.Config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ConfigFile {

  private final String moduleName;
  private final List<ConfigValue> values;

  public ConfigFile(String moduleName, Config config) {
    this.moduleName = moduleName;
    this.values = new ArrayList<>(config.entrySet().size());
    for (Map.Entry<String, com.typesafe.config.ConfigValue> entry : config.entrySet()) {
      // TODO okv: if entry.getValue() is a LIST, we should handle each list item individually (items are either LIST again or OBJECT -- these need to be expanded again somehow?, or one of the simple values)
      values.add(new ConfigValue(entry));
    }
    values.sort(Comparator.comparing(ConfigValue::getName));
  }

  public String getModuleName() {
    return moduleName;
  }

  public List<ConfigValue> getValues() {
    return values;
  }

  public static class ConfigValue {
    private final String name;
    private final String description;
    private final String valueType;
    private final String defaultValue;

    public ConfigValue(Map.Entry<String, com.typesafe.config.ConfigValue> keyValue) {
      name = keyValue.getKey();
      description = getComment(keyValue.getValue());
      valueType = keyValue.getValue().valueType().name().toLowerCase();
      defaultValue = keyValue.getValue().unwrapped().toString();
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

    private static String getComment(com.typesafe.config.ConfigValue value) {
      StringBuilder sb = new StringBuilder();
      for (String line : value.origin().comments()) {
        if (sb.length() > 0) {
          sb.append(' ');
        }
        sb.append(line.trim());
      }
      return sb.toString();
    }
  }
}
