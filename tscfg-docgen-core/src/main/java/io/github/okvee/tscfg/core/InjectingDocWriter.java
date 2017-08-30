package io.github.okvee.tscfg.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InjectingDocWriter implements DocWriter {

  public static final String DEFAULT_START_PLACEHOLDER = "<!-- tscfg-docgen-start -->";
  public static final String DEFAULT_END_PLACEHOLDER = "<!-- tscfg-docgen-end -->";

  private final File outputFile;
  private final String injectionStartPlaceholder;
  private final String injectionEndPlaceholder;

  public InjectingDocWriter(File outputFile,
                            String injectionStartPlaceholder,
                            String injectionEndPlaceholder)
  {
    if (!outputFile.exists()) {
      throw new IllegalArgumentException(
          "Unable to inject generated docs to a non-existent file " + outputFile);
    }
    if (injectionStartPlaceholder == null || injectionStartPlaceholder.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "Injection start placeholder must not be empty.");
    }
    if (injectionEndPlaceholder == null || injectionEndPlaceholder.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "Injection end placeholder must not be empty.");
    }
    if (injectionStartPlaceholder.equals(injectionEndPlaceholder)) {
      throw new IllegalArgumentException(
          "Injection start/end placeholders must not match.");
    }

    this.outputFile = outputFile;
    this.injectionStartPlaceholder = injectionStartPlaceholder.trim();
    this.injectionEndPlaceholder = injectionEndPlaceholder.trim();
  }

  @Override
  public void write(String doc) throws IOException {
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
    outputLines.add(doc);
    outputLines.addAll(originalLines.subList(injectionEnd, originalLines.size()));
    Files.write(outputFile.toPath(), outputLines);
  }

  @Override
  public void close() {
    // no-op
  }
}
