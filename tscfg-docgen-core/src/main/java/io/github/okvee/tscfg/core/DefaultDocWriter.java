package io.github.okvee.tscfg.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DefaultDocWriter implements DocWriter {

  private final FileWriter fileWriter;

  public DefaultDocWriter(File outputFile, boolean overwriteExisting)
      throws IOException
  {
    if (!overwriteExisting && outputFile.exists()) {
      throw new IllegalArgumentException(
          "Not allowed to overwrite already existing file at " + outputFile.toString());
    }
    fileWriter = new FileWriter(outputFile);
  }

  @Override
  public void write(String doc) throws IOException {
    fileWriter.write(doc);
  }

  @Override
  public void close() throws Exception {
    fileWriter.close();
  }
}
