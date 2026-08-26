package com.tondise.ecommerce.controllers.publish;

import com.tondise.ecommerce.dao.dto.PriceCalculationDto;
import com.tondise.ecommerce.dao.dto.ProductDto;
import com.tondise.ecommerce.dao.dto.ProductSummaryDto;
import com.tondise.ecommerce.dao.request.CalculatePriceRequest;
import com.tondise.ecommerce.dao.request.ProductFilterRequest;
import com.tondise.ecommerce.dao.response.DataResponse;
import com.tondise.ecommerce.dao.response.PaginatedResponse;
import com.tondise.ecommerce.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Catalogue - Produits", description = "Consultation publique du catalogue produits : listing, recherche, détail, calcul de prix.")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Lister les produits", description = "Renvoie le catalogue produits, paginé et filtrable (catégorie, tri...).")
    @GetMapping
    public PaginatedResponse<ProductSummaryDto> getProducts(ProductFilterRequest filters) {
        Page<ProductSummaryDto> page = productService.getProducts(filters);
        return PaginatedResponse.of(page, page.getContent());
    }

    @Operation(summary = "Lister les produits mis en avant", description = "Renvoie jusqu'à 12 produits marqués comme mis en avant (isFeatured), pour la page d'accueil par exemple.")
    @GetMapping("/featured")
    public DataResponse<List<ProductSummaryDto>> getFeatured() {
        return DataResponse.of(productService.getFeaturedProducts());
    }

    @Operation(summary = "Rechercher des produits", description = "Recherche des produits par mot-clé (nom, description courte), paginée et filtrable.")
    @GetMapping("/search")
    public PaginatedResponse<ProductSummaryDto> search(
            @Parameter(description = "Mot-clé recherché") @RequestParam("q") String query,
            ProductFilterRequest filters) {
        Page<ProductSummaryDto> page = productService.search(query, filters);
        return PaginatedResponse.of(page, page.getContent());
    }

    @Operation(summary = "Récupérer le détail d'un produit", description = "Renvoie un produit avec ses options, ses paliers de prix et sa catégorie.")
    @GetMapping("/{id}")
    public DataResponse<ProductDto> getProduct(@PathVariable UUID id) {
        return DataResponse.of(productService.findById(id));
    }

    @Operation(summary = "Calculer le prix d'un produit",
            description = "Calcule le prix unitaire et le sous-total d'un produit pour une quantité et des options sélectionnées données (applique le palier de prix correspondant).")
    @PostMapping("/{id}/calculate-price")
    public DataResponse<PriceCalculationDto> calculatePrice(@PathVariable UUID id,
                                                             @Valid @RequestBody CalculatePriceRequest request) {
        return DataResponse.of(productService.calculatePrice(id, request.getQuantity(), request.getOptions()));
    }
}
