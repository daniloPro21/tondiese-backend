package com.tondise.ecommerce.controllers.privates;

import com.tondise.ecommerce.dao.dto.DocumentDto;
import com.tondise.ecommerce.dao.response.DataResponse;
import com.tondise.ecommerce.services.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Upload de fichiers vers le stockage S3/MinIO (via {@link DocumentService})
 * et récupération d'un lien de téléchargement — route qui manquait pour
 * qu'un front puisse ré-afficher un document déjà uploadé (ex. avatar,
 * image produit) sans avoir à en conserver le lien signé, qui expire.
 * Ouvert à tout utilisateur authentifié (pas de contrainte ADMIN) : à
 * resserrer si un usage précis (ex. images produit) doit rester réservé au
 * back-office.
 */
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Upload de fichiers (images, PDF...) vers le stockage S3/MinIO et récupération d'un lien de téléchargement temporaire.")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "Uploader un document",
            description = "Envoie un fichier vers le stockage et renvoie son id ainsi qu'un lien de téléchargement temporaire (valide 1h) — l'id est à conserver côté métier pour régénérer un lien plus tard.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DataResponse<DocumentDto> upload(@RequestParam("file") MultipartFile file) {
        return DataResponse.of(documentService.upload(file));
    }

    @Operation(summary = "Récupérer le lien d'un document",
            description = "Génère un nouveau lien de téléchargement temporaire (valide 1h) pour le document déjà uploadé identifié par son id — c'est cette route qu'un front appelle pour afficher un document dont il n'a gardé que l'id.")
    @GetMapping("/{id}")
    public DataResponse<DocumentDto> getById(@Parameter(description = "Id du document (nom d'objet renvoyé par l'upload)") @PathVariable String id) {
        return DataResponse.of(documentService.getById(id));
    }

    @Operation(summary = "Supprimer un document", description = "Supprime définitivement le fichier du stockage.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
