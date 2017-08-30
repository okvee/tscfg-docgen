package io.github.okvee.tscfg.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter @Setter
public class GroupDefinition {
  private String heading;
  private String prefix;

  public GroupDefinition(String heading, String prefix) {
    this.heading = Objects.requireNonNull(heading, "Group heading must not be null.");
    this.prefix = Objects.requireNonNull(prefix, "Group prefix must not be null.");
  }
}
