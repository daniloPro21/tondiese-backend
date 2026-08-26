package com.tondise.ecommerce.controllers.publish;

import com.tondise.ecommerce.dao.dto.CategoryDto;
import com.tondise.ecommerce.dao.dto.ProductSummaryDto;
import com.tondise.ecommerce.dao.request.ProductFilterRequest;
import com.tondise.ecommerce.dao.response.DataResponse;
import com.tondise.ecommerce.dao.response.PaginatedResponse;
import com.tondise.ecommerce.services.CategoryService;
import com.tondise.ecommerce.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Catalogue - Catégories", description = "Consultation publique de l'arborescence des catégories et des produits qu'elles contiennent.")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @Operation(summary = "Lister les catégories racines",
            description = "Renvoie les catégories de premier niveau (sans parent), chacune avec ses sous-catégories imbriquées.")
    @GetMapping
    public DataResponse<List<CategoryDto>> getCategories() {
        return DataResponse.of(categoryService.getCategories());
    }

    @Operation(summary = "Récupérer une catégorie par son slug", description = "Renvoie le détail d'une catégorie identifiée par son slug (URL-friendly).")
    @GetMapping("/{slug}")
    public DataResponse<CategoryDto> getBySlug(@PathVariable String slug) {
        return DataResponse.of(categoryService.getBySlug(slug));
    }

    @Operation(summary = "Lister les produits d'une catégorie",
            description = "Renvoie, paginés et filtrables, les produits appartenant à la catégorie identifiée par son slug.")
    @GetMapping("/{slug}/products")
    public PaginatedResponse<ProductSummaryDto> getProducts(@PathVariable String slug, ProductFilterRequest filters) {
        CategoryDto category = categoryService.getBySlug(slug);
        Page<ProductSummaryDto> page = productService.getProductsByCategory(category.getId(), filters);
        return PaginatedResponse.of(page, page.getContent());
    }

    @Operation(summary = "Lister les sous-catégories", description = "Renvoie les sous-catégories directes de la catégorie identifiée par son slug.")
    @GetMapping("/{slug}/subcategories")
    public DataResponse<List<CategoryDto>> getSubcategories(@PathVariable String slug) {
        CategoryDto category = categoryService.getBySlug(slug);
        return DataResponse.of(category.getChildren());
    }
}
