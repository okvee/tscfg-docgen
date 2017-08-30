package io.github.okvee.tscfg.core.model;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
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
          final String rawValue = keyValue.getValue().unwrapped().toString();
          if (keyValue.getValue().valueType().equals(ConfigValueType.STRING)) {
            defaultValue = '"' + rawValue + '"';
          } else {
            defaultValue = rawValue;
          }
        }
      } catch (ConfigException.NotResolved e) {
        valueType = "N/A (unresolved)";
        defaultValue = keyValue.getValue().render();
      }
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
    private final List<KeyValue> keyValues = new ArrayList<>();

    Group(String heading) {
      this.heading = heading;
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
      if (groupDefinitions.stream().anyMatch(
          def -> def.getPrefix().equals(groupDefinition.getPrefix())))
      {
        throw new IllegalArgumentException(
            "Duplicate group prefix " + groupDefinition.getPrefix());
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
        final List<GroupDefinition> groupsSortedByPrefix =
            new ArrayList<>(groupDefinitions);
        groupsSortedByPrefix.sort(
            (g1, g2) -> g2.getPrefix().length() - g1.getPrefix().length());

        final Map<String, Group> groupsMap = new HashMap<>();
        for (GroupDefinition groupDefinition : groupDefinitions) {
          final Group group = new Group(groupDefinition.getHeading());
          groupsMap.put(groupDefinition.getPrefix(), group);
          groups.add(group);
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
                "Suitable group not found for config key " + keyValue.getKey());
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
