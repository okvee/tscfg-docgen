package io.github.okvee.tscfg.core;

import io.github.okvee.tscfg.core.model.Configuration;

import java.io.Writer;

public interface DocGenerator {

  void generateDoc(Configuration config, Writer writer) throws Exception;
}
