package com.tondise.ecommerce.controllers.admin;

import com.tondise.ecommerce.dao.dto.ProductDto;
import com.tondise.ecommerce.dao.models.Product;
import com.tondise.ecommerce.dao.request.ProductRequest;
import com.tondise.ecommerce.services.ProductService;
import com.tondise.ecommerce.services.export.ProductExportService;
import com.tondise.utils.abstractController.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD produit hérité de {@link AbstractController} (create/read/update/
 * soft-delete/delete/filter/search) ; seuls les exports restent spécifiques
 * à ce contrôleur.
 */
@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Produits", description = "Gestion des produits du catalogue (CRUD complet + exports). Réservé aux administrateurs.")
public class AdminProductController extends AbstractController<Product, ProductDto, ProductRequest> {

    private final ProductExportService productExportService;

    public AdminProductController(ProductService service, ProductExportService productExportService) {
        super(service);
        this.productExportService = productExportService;
    }

    @Operation(summary = "Exporter le catalogue en PDF",
            description = "Génère un PDF listant l'ensemble des produits du catalogue.")
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        return fileResponse(productExportService.exportPdf(), "catalogue-produits.pdf", MediaType.APPLICATION_PDF);
    }

    @Operation(summary = "Exporter le catalogue en Excel",
            description = "Génère un fichier Excel (.xlsx) listant l'ensemble des produits du catalogue.")
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        return fileResponse(productExportService.exportExcel(), "catalogue-produits.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    private ResponseEntity<byte[]> fileResponse(byte[] content, String filename, MediaType mediaType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(content);
    }
}
