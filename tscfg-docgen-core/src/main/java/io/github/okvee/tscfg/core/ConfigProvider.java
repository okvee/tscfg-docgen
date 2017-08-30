package io.github.okvee.tscfg.core;

import com.typesafe.config.Config;

import java.io.IOException;

/**
 * Provider of {@link Config} objects.
 */
public interface ConfigProvider {

  /**
   * Provides an instance of {@link Config} class. It is up to the implementation whether
   * a new object will be created or existing object will be reused.
   * @return {@link Config} instance
   */
  Config getConfig() throws IOException;
}
