package io.github.okvee.tscfg.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Container for configuration values describing a group definition
 */
@Getter @Setter
public class GroupDefinition {

  @Parameter(property = "heading", required = true)
  private String heading;
  @Parameter(property = "prefix", required = true)
  private String prefix;
}
