package io.github.okvee.tscfg.core;

import com.typesafe.config.Config;

public interface ConfigProvider {

  Config getConfig();
}
