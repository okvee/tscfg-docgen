package com.github.okvee.maven.tscfg;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "generate-docs", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class GenDocMojo extends AbstractMojo {

  private final Configuration freeMarkerConfig;

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

  @Parameter(defaultValue = "<!-- tscfg-gendoc-maven-plugin-start -->")
  private String injectionStartPlaceholder;

  @Parameter(defaultValue = "<!-- tscfg-gendoc-maven-plugin-end -->")
  private String injectionEndPlaceholder;

  public GenDocMojo() {
    freeMarkerConfig = new Configuration(Configuration.VERSION_2_3_23);
    freeMarkerConfig.setDefaultEncoding(StandardCharsets.UTF_8.name());
    freeMarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    freeMarkerConfig.setLogTemplateExceptions(false);
  }

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      generateDocs(readInputFiles());
    } catch (Exception e) {
      throw new MojoFailureException("Failed to generate configuration docs.", e);
    }
  }

  private ConfigFile readInputFiles() throws IOException {
    PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(inputFilePattern);
    try (Stream<Path> pathStream = Files.walk(project.getBasedir().toPath())) {
      Config config = pathStream
          .filter(p -> Files.isRegularFile(p) && pathMatcher.matches(p))
          .peek(p -> getLog().info("Found input file: " + p))
          .map(p -> ConfigFactory.parseFile(p.toFile()).resolve())
          .reduce(ConfigFactory.empty(), Config::withFallback);
      return new ConfigFile(project.getArtifactId(), config);
    }
  }

  private void generateDocs(ConfigFile configFile) throws Exception {
    Template template = loadTemplate();
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
      template.process(configFile, writer);
      outputLines.add(writer.getBuffer().toString());
      outputLines.addAll(originalLines.subList(injectionEnd, originalLines.size()));
      getLog().info("Injecting generated documentation to " + outputFile);
      Files.write(outputFile.toPath(), outputLines);
    } else {
      getLog().info("Writing generated documentation to " + outputFile);
      try (Writer writer = new FileWriter(outputFile)) {
        template.process(configFile, writer);
      }
    }
  }

  private Template loadTemplate() throws IOException {
    if (customTemplateFile != null) {
      freeMarkerConfig.setDirectoryForTemplateLoading(customTemplateFile.getParentFile());
      return freeMarkerConfig.getTemplate(customTemplateFile.getName());
    } else {
      freeMarkerConfig.setClassForTemplateLoading(this.getClass(), "/");
      return freeMarkerConfig.getTemplate("templates/" + templateName + ".ftl");
    }
  }
}
