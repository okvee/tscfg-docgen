package io.github.okvee.tscfg.maven;

import io.github.okvee.tscfg.core.DefaultDocWriter;
import io.github.okvee.tscfg.core.DocWriter;
import io.github.okvee.tscfg.core.FreemarkerDocGenerator;
import io.github.okvee.tscfg.core.InjectingDocWriter;
import io.github.okvee.tscfg.core.PathMatcherConfigProvider;
import io.github.okvee.tscfg.core.model.Configuration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "generate-docs", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class GenerateDocsMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "glob:/**/src/main/resources/reference.conf", required = true)
  private String inputFilePattern;

  @Parameter(defaultValue = "${basedir}/config.md", required = true)
  private File outputFile;

  @Parameter(defaultValue = "false", required = true)
  private boolean overwriteExisting;

  @Parameter(defaultValue = "markdown-gitlab", required = true)
  private String templateName;

  @Parameter
  private File customTemplateFile;

  @Parameter(defaultValue = "false", required = true)
  private boolean injectGeneratedDocs;

  @Parameter(defaultValue = InjectingDocWriter.DEFAULT_START_PLACEHOLDER)
  private String injectionStartPlaceholder;

  @Parameter(defaultValue = InjectingDocWriter.DEFAULT_END_PLACEHOLDER)
  private String injectionEndPlaceholder;

  @Parameter
  private List<String> ignoredPrefixes = new ArrayList<>();

  @Parameter
  private List<GroupDefinition> groups = new ArrayList<>();

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      generateDocs(readInputFiles());
    } catch (Exception e) {
      throw new MojoFailureException("Failed to generate configuration docs", e);
    }
  }

  private Configuration readInputFiles() throws IOException {
    PathMatcherConfigProvider configProvider = new PathMatcherConfigProvider(
        project.getBasedir().toPath(),
        inputFilePattern
    );
    Configuration.Builder builder = Configuration.newBuilder()
        .setConfig(configProvider.getConfig());
    ignoredPrefixes.forEach(builder::addIgnoredPrefix);
    groups.forEach(groupDef -> builder.addGroupDefinition(
        new io.github.okvee.tscfg.core.model.GroupDefinition(
            groupDef.getHeading(),
            // empty group prefix in pom.xml will become a null reference,
            // so we need to take care of that and replace it with
            // an empty string explicitly
            groupDef.getPrefix() == null ? "" : groupDef.getPrefix()
        )
    ));
    return builder.build();
  }

  private void generateDocs(Configuration config) throws Exception {
    FreemarkerDocGenerator generator = customTemplateFile != null ?
        new FreemarkerDocGenerator(customTemplateFile) :
        new FreemarkerDocGenerator(templateName);

    getLog().info((injectGeneratedDocs ? "Injecting" : "Writing") +
        " generated documentation to " + outputFile
    );

    try (DocWriter writer = createWriter()) {
      writer.write(generator.generateDoc(config));
    }
  }

  private DocWriter createWriter() throws IOException {
    return injectGeneratedDocs ?
        new InjectingDocWriter(
            outputFile,
            injectionStartPlaceholder,
            injectionEndPlaceholder) :
        new DefaultDocWriter(outputFile, overwriteExisting);
  }
}
