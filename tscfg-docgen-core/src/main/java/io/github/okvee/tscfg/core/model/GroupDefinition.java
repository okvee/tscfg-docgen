package io.github.okvee.tscfg.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter @Setter
public class GroupDefinition {
  private String heading;
  private List<String> prefixes;

  public GroupDefinition(String heading, List<String> prefixes) {
    this.heading = Objects.requireNonNull(heading, "Group heading must not be null.");
    this.prefixes = Objects.requireNonNull(prefixes, "Group prefixes must not be null.");
  }
}
