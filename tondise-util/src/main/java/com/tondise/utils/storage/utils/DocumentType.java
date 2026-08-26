package com.tondise.utils.storage.utils;

public enum DocumentType {
  APPLICATION_ATOM_XML("application/atom+xml"),
  APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
  APPLICATION_JSON("application/json"),
  APPLICATION_OCTET_STREAM("application/octet-stream"),
  APPLICATION_XHTML_XML("application/xhtml+xml"),
  APPLICATION_XML("application/xml"),
  IMAGE_GIF("image/gif"),
  IMAGE_JPEG("image/jpeg"),
  IMAGE_PNG("image/png"),
  MULTIPART_FORM_DATA("multipart/form-data"),
  TEXT_HTML("text/html"),
  TEXT_PLAIN("text/plain"),
  TEXT_XML("text/xml"),
  IMAGE_JPG("image/jpg"),
  APPLICATION_PDF( "application/pdf");

  private final String name;

  DocumentType(String name) {
    this.name = name;
  }
}
