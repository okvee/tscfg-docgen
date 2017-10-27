package io.github.okvee.tscfg.maven;

import lombok.Getter;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * Container for configuration values describing a group definition
 */
@Getter
public class GroupDefinition {

  @Parameter(required = true)
  private String heading;
  @Parameter(required = true)
  private List<String> prefixes;
}
