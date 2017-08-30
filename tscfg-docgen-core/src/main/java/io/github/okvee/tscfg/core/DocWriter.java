package io.github.okvee.tscfg.core;

import java.io.IOException;

public interface DocWriter extends AutoCloseable {

  void write(String doc) throws IOException;
}
