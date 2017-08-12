package io.github.okvee.tscfg.maven;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.okvee.tscfg.core.FreemarkerDocGenerator;
import io.github.okvee.tscfg.core.model.Configuration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "generate-docs", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class GenerateDocsMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "glob:/**/src/main/resources/reference.conf", required = true)
  private String inputFilePattern;

  @Parameter(defaultValue = "${basedir}/config.md", required = true)
  private File outputFile;

  @Parameter(defaultValue = "markdown-gitlab", required = true)
  private String templateName;

  @Parameter
  private File customTemplateFile;

  @Parameter(defaultValue = "false", required = true)
  private boolean injectGeneratedDocs;

  @Parameter(defaultValue = "<!-- tscfg-docgen-maven-plugin-start -->")
  private String injectionStartPlaceholder;

  @Parameter(defaultValue = "<!-- tscfg-docgen-maven-plugin-end -->")
  private String injectionEndPlaceholder;

  @Parameter
  private List<String> ignoredPrefixes = new ArrayList<>();

  @Parameter
  private List<GroupDefinition> groups = new ArrayList<>();

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      // TODO okv: validate configuration
      // - inputFilePattern must be non-empty
      // - group prefixes must be non-empty and unique, each group must have non-empty heading
      // - if injection is enabled, injection placeholders must be non-empty and each must be different
      // - if specified, custom template file must exist
      // - do not overwrite output file if it exists unless allowed (not allowed by default - new configuration parameter)
      // - otoh, if injection is enabled, the output file _must_ exist, otherwise fail
      // - ALL this should probably be handled in core classes, maven plugin should be only an interface
      generateDocs(readInputFiles());
    } catch (Exception e) {
      throw new MojoFailureException("Failed to generate configuration docs.", e);
    }
  }

  private Configuration readInputFiles() throws IOException {
    PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(inputFilePattern);
    try (Stream<Path> pathStream = Files.walk(project.getBasedir().toPath())) {
      Config config = pathStream
          .filter(p -> Files.isRegularFile(p) && pathMatcher.matches(p))
          .peek(p -> getLog().info("Found input file: " + p))
          .map(p -> ConfigFactory.parseFile(p.toFile()))
          .reduce(ConfigFactory.empty(), Config::withFallback);

      return new Configuration(
          config.resolve(),
          ignoredPrefixes,
          groups.stream()
              .map(groupDef -> new io.github.okvee.tscfg.core.model.GroupDefinition(
                  groupDef.getHeading(), groupDef.getPrefix())
              )
              .collect(Collectors.toList())
      );
    }
  }

  private void generateDocs(Configuration config) throws Exception {
    FreemarkerDocGenerator generator = FreemarkerDocGenerator.newBuilder()
        .setTemplateName(templateName)
        .setCustomTemplateFile(customTemplateFile)
        .build();

    if (injectGeneratedDocs) {
      List<String> originalLines = Files.readAllLines(outputFile.toPath());
      List<String> trimmedLines = originalLines.stream()
          .map(String::trim)
          .collect(Collectors.toList());
      int injectionStart = trimmedLines.indexOf(injectionStartPlaceholder.trim());
      int injectionEnd = trimmedLines.indexOf(injectionEndPlaceholder.trim());
      if (injectionStart == -1 || injectionEnd == -1 || injectionStart > injectionEnd) {
        throw new IllegalArgumentException(
            "Missing or invalid injection placeholders in output file.");
      }

      List<String> outputLines = new ArrayList<>();
      outputLines.addAll(originalLines.subList(0, injectionStart + 1));
      StringWriter writer = new StringWriter();
      generator.generateDoc(config, writer);
      outputLines.add(writer.getBuffer().toString());
      outputLines.addAll(originalLines.subList(injectionEnd, originalLines.size()));
      getLog().info("Injecting generated documentation to " + outputFile);
      Files.write(outputFile.toPath(), outputLines);
    } else {
      getLog().info("Writing generated documentation to " + outputFile);
      try (Writer writer = new FileWriter(outputFile)) {
        generator.generateDoc(config, writer);
      }
    }
  }

}
