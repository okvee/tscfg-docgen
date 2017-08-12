package io.github.okvee.tscfg.core.model;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Configuration {

  private final List<KeyValue> keyValues;
  private final List<Group> groups = new ArrayList<>();

  // TODO okv: create and use builder instead of constructor
  public Configuration(Config config, List<String> ignoredPrefixes,
                       List<GroupDefinition> groupDefinitions)
  {
    this.keyValues = new ArrayList<>(config.entrySet().size());
    // TODO okv: entrySet() won't contain null values, but we want those too... Need to fix this, see SimpleConfig.findPaths() (btw: how to render null value, how to distinguish it from empty string?)
    for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
      boolean ignored = false;
      for (String ignoredPrefix : ignoredPrefixes) {
        if (entry.getKey().startsWith(ignoredPrefix)) {
          ignored = true;
          break;
        }
      }
      if (!ignored) {
        keyValues.add(new KeyValue(entry));
      }
    }
    keyValues.sort(Comparator.comparing(KeyValue::getKey));

    if (!groupDefinitions.isEmpty()) {
      final List<GroupDefinition> groupsSortedByPrefix = new ArrayList<>(groupDefinitions);
      groupsSortedByPrefix.sort((g1, g2) -> g2.getPrefix().length() - g1.getPrefix().length());

      final Map<String, Group> groupsMap = new HashMap<>();
      for (GroupDefinition groupDefinition : groupDefinitions) {
        final Group group = new Group(groupDefinition.getHeading());
        groupsMap.put(groupDefinition.getPrefix(), group);
        this.groups.add(group);
      }

      for (KeyValue keyValue : keyValues) {
        boolean added = false;
        for (GroupDefinition g : groupsSortedByPrefix) {
          if (keyValue.getKey().startsWith(g.getPrefix())) {
            groupsMap.get(g.getPrefix()).add(keyValue);
            added = true;
            break;
          }
        }
        if (!added) {
          throw new IllegalArgumentException(
              "Suitable group not found for config key \"" + keyValue.getKey() + "\"");
        }
      }
    }
  }

  @Getter
  public static class KeyValue {
    private final String key;
    private final String description;
    private final String valueType;
    private final String defaultValue;

    KeyValue(Map.Entry<String, ConfigValue> keyValue) {
      key = keyValue.getKey();
      description = getComment(keyValue.getValue());
      valueType = keyValue.getValue().valueType().name().toLowerCase();
      final String rawValue = keyValue.getValue().unwrapped().toString();
      if (keyValue.getValue().valueType().equals(ConfigValueType.STRING)) {
        defaultValue = '"' + rawValue + '"';
      } else {
        defaultValue = rawValue;
      }
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
  }

  @Getter
  public static class Group {
    private final String heading;
    private final List<KeyValue> keyValues = new ArrayList<>();

    Group(String heading) {
      this.heading = heading;
    }

    void add(KeyValue keyValue) {
      keyValues.add(keyValue);
    }
  }
}
