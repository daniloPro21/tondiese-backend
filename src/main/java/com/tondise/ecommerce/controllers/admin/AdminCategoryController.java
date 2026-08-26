package com.tondise.ecommerce.controllers.admin;

import com.tondise.ecommerce.dao.dto.CategoryDto;
import com.tondise.ecommerce.dao.models.Category;
import com.tondise.ecommerce.dao.request.CategoryRequest;
import com.tondise.ecommerce.services.CategoryService;
import com.tondise.utils.abstractController.AbstractController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD complet (create/read/update/soft-delete/delete/filter/search) hérité
 * de {@link AbstractController} : voir {@link CategoryService} pour la
 * logique métier. Le listing hiérarchique (racines + enfants) reste sur le
 * contrôleur public ({@code CategoryController}) — ici c'est la liste plate
 * de toutes les catégories qui sert à l'administration.
 */
@RestController
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Catégories", description = "Gestion des catégories du catalogue (CRUD complet). Réservé aux administrateurs.")
public class AdminCategoryController extends AbstractController<Category, CategoryDto, CategoryRequest> {

    public AdminCategoryController(CategoryService service) {
        super(service);
    }
}
