package io.github.okvee.tscfg.core.model;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class Configuration {

  private final List<KeyValue> keyValues;
  private final List<Group> groups;

  private Configuration(List<KeyValue> keyValues, List<Group> groups) {
    this.keyValues = Objects.requireNonNull(keyValues);
    this.groups = Objects.requireNonNull(groups);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Getter
  public static class KeyValue {
    private final String key;
    private final String description;
    private String valueType;
    private String defaultValue;

    KeyValue(Map.Entry<String, ConfigValue> keyValue) {
      key = keyValue.getKey();
      description = createDescription(keyValue.getValue());
      try {
        if (keyValue.getValue().valueType() == ConfigValueType.NULL) {
          valueType = "N/A (null)";
          defaultValue = "null";
        } else {
          valueType = keyValue.getValue().valueType().name().toLowerCase();
          defaultValue = render(keyValue.getValue());
        }
      } catch (ConfigException.NotResolved e) {
        valueType = "N/A (unresolved)";
        defaultValue = keyValue.getValue().render();
      }
    }

    private static String render(ConfigValue value) {
      String result;
      if (value.valueType().equals(ConfigValueType.LIST)) {
        result = "[" +
            ((ConfigList) value).stream()
                .map(KeyValue::render)
                .collect(Collectors.joining(", ")) +
            "]";
      } else if (value.valueType().equals(ConfigValueType.STRING)) {
        result = '"' + value.unwrapped().toString() + '"';
      } else {
        result = value.unwrapped().toString();
      }
      return result;
    }

    private static String createDescription(ConfigValue value) {
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
    // TODO okv: include in rendered heading as "Heading (`prefix`)" and use freemarker's string?keep_after(prefix) to strip prefixes from keys
    private final List<String> prefixes;
    private final List<KeyValue> keyValues = new ArrayList<>();

    Group(String heading, List<String> prefixes) {
      this.heading = heading;
      this.prefixes = prefixes;
    }

    void add(KeyValue keyValue) {
      keyValues.add(keyValue);
    }
  }

  public static class Builder {

    private Config config;
    private Set<String> ignoredPrefixes = new HashSet<>(100);
    private List<GroupDefinition> groupDefinitions = new ArrayList<>(50);

    private Builder() {}

    public Builder setConfig(Config config) {
      this.config = config;
      return this;
    }

    public Builder addIgnoredPrefix(String ignoredPrefix) {
      ignoredPrefixes.add(ignoredPrefix);
      return this;
    }

    public Builder addGroupDefinition(GroupDefinition groupDefinition) {
      for (GroupDefinition existingDef : groupDefinitions) {
        List<String> existingPrefixes = new ArrayList<>(existingDef.getPrefixes());
        existingPrefixes.retainAll(groupDefinition.getPrefixes());
        if (!existingPrefixes.isEmpty()) {
          throw new IllegalArgumentException(
              "Duplicate group prefixes: " + existingPrefixes.toString());
        }
      }
      groupDefinitions.add(groupDefinition);
      return this;
    }

    public Configuration build() {
      Set<Map.Entry<String, ConfigValue>> entries = config.entrySet();
      // we want to include null values as well, but Config#entrySet() will
      // skip them, so we need to include them explicitly
      entries.addAll(nullValues(config.root()));
      final List<KeyValue> keyValues = new ArrayList<>(entries.size());
      for (Map.Entry<String, ConfigValue> entry : entries) {
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

      final List<Group> groups = new ArrayList<>(groupDefinitions.size());
      if (!groupDefinitions.isEmpty()) {
        List<String> allPrefixes = new ArrayList<>();
        groupDefinitions.forEach(gd -> allPrefixes.addAll(gd.getPrefixes()));
        allPrefixes.sort((p1, p2) -> p2.length() - p1.length());

        final Map<String, Group> groupsMap = new HashMap<>();
        for (GroupDefinition groupDefinition : groupDefinitions) {
          final Group group = new Group(
              groupDefinition.getHeading(),
              groupDefinition.getPrefixes()
          );
          groupDefinition.getPrefixes().forEach(prefix -> groupsMap.put(prefix, group));
          groups.add(group);
        }

        for (KeyValue keyValue : keyValues) {
          boolean added = false;
          for (String prefix : allPrefixes) {
            if (keyValue.getKey().startsWith(prefix)) {
              groupsMap.get(prefix).add(keyValue);
              added = true;
              break;
            }
          }
          if (!added) {
            throw new IllegalArgumentException("" +
                "Suitable group not found for config key '" + keyValue.getKey() + "', " +
                "did you forget to assign empty (\"\") prefix to one of the groups?");
          }
        }
      }
      return new Configuration(keyValues, groups);
    }

    private static Set<Map.Entry<String, ConfigValue>> nullValues(ConfigObject config) {
      Set<Map.Entry<String, ConfigValue>> result = new HashSet<>();
      for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
        try {
          if (entry.getValue().valueType() == ConfigValueType.NULL) {
            result.add(entry);
          } else if (entry.getValue().valueType() == ConfigValueType.OBJECT) {
            result.addAll(nullValues((ConfigObject) entry.getValue()));
          }
        } catch (ConfigException.NotResolved e) {
          // unresolved substitutions are handled elsewhere, here we just ignore them
        }
      }
      return result;
    }
  }
}
