package com.tondise.utils.storage;

import java.io.InputStream;

public interface Storage {

  void save(String filename, String type, InputStream file);

  String download(String filename);

  void delete(String filename);

  String getContentType(String type);
}
