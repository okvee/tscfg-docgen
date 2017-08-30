package io.github.okvee.tscfg.core;

import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.github.okvee.tscfg.core.model.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class FreemarkerDocGenerator implements DocGenerator {

  private String standardTemplateName;
  private File customTemplateFile;
  private freemarker.template.Configuration freeMarkerConfig;

  public FreemarkerDocGenerator(String standardTemplateName) {
    this.standardTemplateName = standardTemplateName;
    initFreeMarkerConfig();
  }

  public FreemarkerDocGenerator(File customTemplateFile) {
    if (!customTemplateFile.exists()) {
      throw new IllegalArgumentException(
          "Custom template file " + customTemplateFile + " does not exist.");
    }
    this.customTemplateFile = customTemplateFile;
    initFreeMarkerConfig();
  }

  private void initFreeMarkerConfig() {
    freeMarkerConfig = new freemarker.template.Configuration(
        freemarker.template.Configuration.VERSION_2_3_26);
    freeMarkerConfig.setDefaultEncoding(StandardCharsets.UTF_8.name());
    freeMarkerConfig.setTemplateExceptionHandler(
        TemplateExceptionHandler.RETHROW_HANDLER);
    freeMarkerConfig.setLogTemplateExceptions(false);
  }

  @Override
  public String generateDoc(Configuration config) throws Exception {
    StringWriter writer = new StringWriter();
    loadTemplate().process(config, writer);
    return writer.toString();
  }

  private Template loadTemplate() throws IOException {
    if (customTemplateFile != null) {
      freeMarkerConfig.setDirectoryForTemplateLoading(customTemplateFile.getParentFile());
      return freeMarkerConfig.getTemplate(customTemplateFile.getName());
    } else {
      freeMarkerConfig.setClassForTemplateLoading(this.getClass(), "/");
      return freeMarkerConfig.getTemplate("templates/" + standardTemplateName + ".ftl");
    }
  }

}
