package com.tondise.ecommerce.services;

import com.tondise.ecommerce.dao.dto.DocumentDto;
import com.tondise.utils.exception.BadRequestException;
import com.tondise.utils.storage.Storage;
import com.tondise.utils.storage.utils.DocumentType;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Fine couche au-dessus de {@code Storage} (tondise-util, backé par MinIO) :
 * génère un nom d'objet unique par upload et le fait doubler comme id — il
 * n'existe pas d'entité "Document" en base, {@code Storage} ne connaît que
 * des noms de fichiers.
 */
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final Storage storage;

    public DocumentDto upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier est vide");
        }

        String id = UUID.randomUUID() + extractExtension(file.getOriginalFilename());
        String documentType = resolveDocumentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            storage.save(id, documentType, inputStream);
        } catch (IOException e) {
            throw new BadRequestException("Impossible de lire le fichier envoyé : " + e.getMessage());
        }

        return DocumentDto.builder()
                .id(id)
                .url(storage.download(id))
                .contentType(file.getContentType())
                .build();
    }

    /** Régénère un lien de téléchargement temporaire pour un document déjà uploadé. */
    public DocumentDto getById(String id) {
        return DocumentDto.builder()
                .id(id)
                .url(storage.download(id))
                .build();
    }

    public void delete(String id) {
        storage.delete(id);
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }

    /**
     * {@code Storage.save} attend le nom d'une constante {@link DocumentType}
     * (pas un content-type MIME brut) : on mappe le content-type détecté par
     * Spring vers la constante correspondante, avec repli sur
     * {@code APPLICATION_OCTET_STREAM} pour tout type non reconnu.
     */
    private String resolveDocumentType(String contentType) {
        if (contentType == null) {
            return DocumentType.APPLICATION_OCTET_STREAM.name();
        }
        if (contentType.equalsIgnoreCase(MediaType.IMAGE_JPEG_VALUE) || contentType.equalsIgnoreCase("image/jpg")) {
            return DocumentType.IMAGE_JPEG.name();
        }
        if (contentType.equalsIgnoreCase(MediaType.IMAGE_PNG_VALUE)) {
            return DocumentType.IMAGE_PNG.name();
        }
        if (contentType.equalsIgnoreCase(MediaType.IMAGE_GIF_VALUE)) {
            return DocumentType.IMAGE_GIF.name();
        }
        if (contentType.equalsIgnoreCase(MediaType.APPLICATION_PDF_VALUE)) {
            return DocumentType.APPLICATION_PDF.name();
        }
        if (contentType.equalsIgnoreCase(MediaType.TEXT_PLAIN_VALUE)) {
            return DocumentType.TEXT_PLAIN.name();
        }
        return DocumentType.APPLICATION_OCTET_STREAM.name();
    }
}
