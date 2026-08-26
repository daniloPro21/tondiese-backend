package com.tondise.ecommerce.controllers.privates;

import com.tondise.ecommerce.config.security.CurrentUserResolver;
import com.tondise.ecommerce.dao.dto.FavoriteDto;
import com.tondise.ecommerce.dao.dto.ProductSummaryDto;
import com.tondise.ecommerce.dao.models.Favorite;
import com.tondise.ecommerce.dao.request.FavoriteRequest;
import com.tondise.ecommerce.services.FavoriteService;
import com.tondise.utils.abstractController.AbstractController;
import com.tondise.utils.abstractController.CrudOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Étend {@link AbstractController} comme {@code Address}/{@code Order}. La
 * ressource {@code Favorite} a son propre id (routes héritées {@code /{id}}),
 * mais le client ne le connaît jamais côté toggle — {@code /by-product/{productId}}
 * reste disponible pour ce cas d'usage. {@code GET /favorites} change de forme
 * par rapport à avant la migration : il renvoie désormais des
 * {@link FavoriteDto} (avec {@code product} imbriqué), pas des
 * {@link ProductSummaryDto} nus — {@code GET /favorites/products} conserve
 * l'ancienne forme pour la compatibilité.
 */
@RestController
@RequestMapping("/favorites")
@Tag(name = "Favoris", description = "Produits mis en favori par l'utilisateur connecté.")
public class FavoriteController extends AbstractController<Favorite, FavoriteDto, FavoriteRequest> {

    private final FavoriteService favoriteService;
    private final CurrentUserResolver currentUserResolver;

    public FavoriteController(FavoriteService service, CurrentUserResolver currentUserResolver) {
        super(service);
        this.favoriteService = service;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    protected Set<CrudOperation> allowedOperations() {
        return EnumSet.of(CrudOperation.CREATE, CrudOperation.READ, CrudOperation.DELETE);
    }

    @Override
    @Operation(summary = "Ajouter un produit aux favoris",
            description = "Ajoute le produit donné (par son id) aux favoris de l'utilisateur connecté. Sans effet s'il y est déjà.")
    @PostMapping
    public ResponseEntity<FavoriteDto> create(@Valid @RequestBody FavoriteRequest dto) {
        return ResponseEntity.ok(favoriteService.addFavorite(currentUserId(), dto.getProductId()));
    }

    @Override
    @Operation(summary = "Récupérer un favori",
            description = "Renvoie un favori par son id (celui du favori, pas du produit), uniquement s'il appartient à l'utilisateur connecté.")
    @GetMapping("/{id}")
    public ResponseEntity<FavoriteDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(favoriteService.getFavorite(currentUserId(), id));
    }

    @Override
    @Operation(summary = "Lister mes favoris",
            description = "Renvoie tous les favoris de l'utilisateur connecté (produit imbriqué dans chaque entrée).")
    @GetMapping
    public ResponseEntity<List<FavoriteDto>> getAll() {
        return ResponseEntity.ok(favoriteService.getFavorites(currentUserId()));
    }

    @Override
    @Operation(summary = "Lister mes favoris (paginé)", description = "Renvoie les favoris de l'utilisateur connecté, page par page.")
    @GetMapping("/all")
    public Page<FavoriteDto> getAll(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return favoriteService.getFavorites(currentUserId(), PageRequest.of(page, size));
    }

    @Override
    @Operation(summary = "Compter mes favoris", description = "Renvoie le nombre de produits mis en favori par l'utilisateur connecté.")
    @GetMapping("/count")
    public long count() {
        return favoriteService.countFavorites(currentUserId());
    }

    @Override
    @Operation(summary = "Retirer un favori par son id",
            description = "Supprime un favori par son propre id (voir aussi DELETE /favorites/by-product/{productId} pour retirer par id de produit).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        favoriteService.removeFavoriteById(currentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    /** Ancienne forme de {@code GET /favorites} — conservée pour compatibilité front. */
    @Operation(summary = "Lister mes produits favoris (forme allégée)",
            description = "Ancienne forme de la liste des favoris : renvoie directement les produits, sans l'enveloppe favori. Conservée pour compatibilité front.")
    @GetMapping("/products")
    public ResponseEntity<List<ProductSummaryDto>> getFavoriteProducts() {
        return ResponseEntity.ok(favoriteService.getFavoriteProducts(currentUserId()));
    }

    @Operation(summary = "Ajouter un favori par id de produit", description = "Alternative à POST /favorites : ajoute le produit donné aux favoris via son id dans l'URL.")
    @PostMapping("/by-product/{productId}")
    public ResponseEntity<FavoriteDto> addByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(favoriteService.addFavorite(currentUserId(), productId));
    }

    @Operation(summary = "Retirer un favori par id de produit", description = "Retire un produit des favoris de l'utilisateur connecté en le désignant par son id de produit (pas l'id du favori).")
    @DeleteMapping("/by-product/{productId}")
    public ResponseEntity<Void> removeByProduct(@PathVariable UUID productId) {
        favoriteService.removeFavorite(currentUserId(), productId);
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUserResolver.resolveUserId(jwt);
    }
}
