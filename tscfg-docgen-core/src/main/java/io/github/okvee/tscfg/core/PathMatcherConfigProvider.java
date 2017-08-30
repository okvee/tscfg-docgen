package io.github.okvee.tscfg.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigResolveOptions;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Configuration provider reading configuration from one or more configuration files.
 * <p>
 * To find the configuration files, this provider recursively walks a hierarchy
 * of sub-directories starting from a given base directory and picks all files
 * whose path matches given pattern.
 *
 */
public class PathMatcherConfigProvider implements ConfigProvider {

  private final Path baseDirectory;
  private final String inputFilePattern;

  /**
   * Default constructor
   * @param baseDirectory base directory to start searching for configuration files
   * @param inputFilePattern pattern for path matcher used to identify configuration
   *                        files to read the configuration from
   *                        (see {@link FileSystem#getPathMatcher(String)}).
   */
  public PathMatcherConfigProvider(Path baseDirectory, String inputFilePattern) {
    this.baseDirectory = Objects.requireNonNull(
        baseDirectory, "Base directory must not be null.");
    this.inputFilePattern = Objects.requireNonNull(
        inputFilePattern, "Input file pattern must not be null.");
  }

  @Override
  public Config getConfig() throws IOException {
    PathMatcher pathMatcher;

    try {
      pathMatcher = FileSystems.getDefault().getPathMatcher(inputFilePattern);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid input file pattern: " + inputFilePattern);
    }

    try (Stream<Path> pathStream = Files.walk(baseDirectory)) {
      return pathStream
          .filter(p -> Files.isRegularFile(p) && pathMatcher.matches(p))
          .map(p -> ConfigFactory.parseFile(p.toFile()))
          .reduce(ConfigFactory.empty(), Config::withFallback)
          .resolve(
              ConfigResolveOptions.defaults()
                  .setAllowUnresolved(true)
                  .setUseSystemEnvironment(false)
          );
    }
  }
}
