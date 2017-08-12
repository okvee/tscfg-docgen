package io.github.okvee.tscfg.core;

import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.github.okvee.tscfg.core.model.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class FreemarkerDocGenerator implements DocGenerator {

  private final String templateName;
  private final File customTemplateFile;
  private final freemarker.template.Configuration freeMarkerConfig;

  private FreemarkerDocGenerator(String templateName, File customTemplateFile) {
    this.templateName = templateName;
    this.customTemplateFile = customTemplateFile;

    freeMarkerConfig = new freemarker.template.Configuration(
        freemarker.template.Configuration.VERSION_2_3_26);
    freeMarkerConfig.setDefaultEncoding(StandardCharsets.UTF_8.name());
    freeMarkerConfig.setTemplateExceptionHandler(
        TemplateExceptionHandler.RETHROW_HANDLER);
    freeMarkerConfig.setLogTemplateExceptions(false);
  }

  @Override
  public void generateDoc(Configuration config, Writer writer) throws Exception {
    loadTemplate().process(config, writer);
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

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String templateName;
    private File customTemplateFile;

    private Builder() {}

    public Builder setTemplateName(String templateName) {
      this.templateName = templateName;
      return this;
    }

    public Builder setCustomTemplateFile(File customTemplateFile) {
      this.customTemplateFile = customTemplateFile;
      return this;
    }

    public FreemarkerDocGenerator build() {
      // TODO okv: validate - either templateName or customTemplateFile must be set, but not both at the same time
      return new FreemarkerDocGenerator(templateName, customTemplateFile);
    }
  }

}
