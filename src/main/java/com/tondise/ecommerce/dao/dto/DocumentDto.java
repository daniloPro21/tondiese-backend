package com.tondise.ecommerce.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pas d'entité "Document" en base : le fichier vit uniquement dans le bucket
 * S3/MinIO, {@code id} (nom d'objet généré) est la seule référence à
 * conserver côté métier (ex. {@code Product.mainImage}) pour pouvoir
 * régénérer un lien plus tard via {@code GET /documents/{id}}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private String id;
    private String url;
    private String contentType;
}
