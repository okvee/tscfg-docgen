package io.github.okvee.tscfg.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// TODO okv: implement
public class InjectingFileWriter extends FileWriter {
  public InjectingFileWriter(File file) throws IOException {
    super(file);
  }


}
