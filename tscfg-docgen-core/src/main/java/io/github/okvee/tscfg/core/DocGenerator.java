package io.github.okvee.tscfg.core;

import io.github.okvee.tscfg.core.model.Configuration;

public interface DocGenerator {

  String generateDoc(Configuration config) throws Exception;
}
