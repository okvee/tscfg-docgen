package io.github.okvee.tscfg.core.model;

import com.typesafe.config.Config;

public interface ModelBuilder {

  Configuration build(Config config);

}
