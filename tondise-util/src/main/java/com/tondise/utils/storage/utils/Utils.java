package com.tondise.utils.storage.utils;

import org.springframework.http.MediaType;

public class Utils {
    public MediaType getContentType(String type) {
        final var documentType = DocumentType.valueOf(type);
        return switch (documentType) {
            case APPLICATION_ATOM_XML -> MediaType.APPLICATION_ATOM_XML;
            case APPLICATION_FORM_URLENCODED -> MediaType.APPLICATION_FORM_URLENCODED;
            case APPLICATION_JSON -> MediaType.APPLICATION_JSON;
            case APPLICATION_XHTML_XML -> MediaType.APPLICATION_XHTML_XML;
            case APPLICATION_XML -> MediaType.APPLICATION_XML;
            case IMAGE_GIF -> MediaType.IMAGE_GIF;
            case IMAGE_JPEG, IMAGE_JPG -> MediaType.IMAGE_JPEG;
            case IMAGE_PNG -> MediaType.IMAGE_PNG;
            case MULTIPART_FORM_DATA -> MediaType.MULTIPART_FORM_DATA;
            case TEXT_HTML -> MediaType.TEXT_HTML;
            case TEXT_PLAIN -> MediaType.TEXT_PLAIN;
            case TEXT_XML -> MediaType.TEXT_XML;
            case APPLICATION_PDF -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    public String getContentTypeValue(String type) {
        final var documentType = DocumentType.valueOf(type);
        return switch (documentType) {
            case APPLICATION_ATOM_XML -> MediaType.APPLICATION_ATOM_XML_VALUE;
            case APPLICATION_FORM_URLENCODED -> MediaType.APPLICATION_FORM_URLENCODED_VALUE;
            case APPLICATION_JSON -> MediaType.APPLICATION_JSON_VALUE;
            case APPLICATION_XHTML_XML -> MediaType.APPLICATION_XHTML_XML_VALUE;
            case APPLICATION_XML -> MediaType.APPLICATION_XML_VALUE;
            case IMAGE_GIF -> MediaType.IMAGE_GIF_VALUE;
            case IMAGE_JPEG, IMAGE_JPG -> MediaType.IMAGE_JPEG_VALUE;
            case IMAGE_PNG -> MediaType.IMAGE_PNG_VALUE;
            case MULTIPART_FORM_DATA -> MediaType.MULTIPART_FORM_DATA_VALUE;
            case TEXT_HTML -> MediaType.TEXT_HTML_VALUE;
            case TEXT_PLAIN -> MediaType.TEXT_PLAIN_VALUE;
            case TEXT_XML -> MediaType.TEXT_XML_VALUE;
            case APPLICATION_PDF -> MediaType.APPLICATION_PDF_VALUE;
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}
