package com.tondise.ecommerce.controllers.privates;

import com.tondise.ecommerce.config.security.CurrentUserResolver;
import com.tondise.ecommerce.dao.dto.CartDto;
import com.tondise.ecommerce.dao.models.Cart;
import com.tondise.ecommerce.dao.request.AddToCartRequest;
import com.tondise.ecommerce.dao.request.ApplyPromoRequest;
import com.tondise.ecommerce.dao.request.CartRequest;
import com.tondise.ecommerce.dao.request.UpdateCartItemRequest;
import com.tondise.ecommerce.services.CartService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Étend {@link AbstractController} comme le reste de {@code privates/}, mais
 * {@code Cart} est un singleton par utilisateur : le client n'envoie jamais
 * d'id de panier. Toutes les routes héritées sont donc redéfinies pour
 * ignorer l'{@code {id}} du chemin et retourner/agir sur le panier de
 * l'utilisateur courant — {@code {id}} reste dans l'URL pour respecter le
 * contrat d'{@code AbstractController}, mais sa valeur n'est jamais lue.
 * {@code DELETE /{id}} vide le panier (il n'est jamais supprimé en tant que
 * ligne). {@code UPDATE}/{@code SOFT_DELETE}/{@code FILTER}/{@code SEARCH}
 * restent désactivées : la gestion fine reste sur les sous-routes
 * {@code /items}, {@code /promo}.
 */
@RestController
@RequestMapping("/cart")
@Tag(name = "Panier", description = "Panier de l'utilisateur connecté (un seul panier par utilisateur, créé automatiquement au premier accès) et ses articles.")
public class CartController extends AbstractController<Cart, CartDto, CartRequest> {

    private final CartService cartService;
    private final CurrentUserResolver currentUserResolver;

    public CartController(CartService service, CurrentUserResolver currentUserResolver) {
        super(service);
        this.cartService = service;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    protected Set<CrudOperation> allowedOperations() {
        return EnumSet.of(CrudOperation.CREATE, CrudOperation.READ, CrudOperation.DELETE);
    }

    @Override
    @Operation(summary = "Récupérer mon panier (forme héritée)",
            description = "Retourne (et crée si besoin) le panier de l'utilisateur connecté. Équivalent à GET /cart.")
    @PostMapping
    public ResponseEntity<CartDto> create(@Valid @RequestBody CartRequest dto) {
        return ResponseEntity.ok(cartService.getCart(currentUserId()));
    }

    @Override
    @Operation(summary = "Récupérer mon panier (forme héritée)",
            description = "L'id dans l'URL est ignoré : renvoie toujours le panier de l'utilisateur connecté (il n'y en a qu'un). Équivalent à GET /cart.")
    @GetMapping("/{id}")
    public ResponseEntity<CartDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(cartService.getCart(currentUserId()));
    }

    /**
     * {@code GET /cart} — forme historique (avant migration), conservée telle
     * quelle pour ne pas casser le front : un objet {@code CartDto} nu, pas une
     * liste. La route héritée équivalente ({@code getAll()}) est déplacée sur
     * {@code /cart/list} pour éviter le conflit de route.
     */
    @Operation(summary = "Récupérer mon panier",
            description = "Retourne le panier de l'utilisateur connecté (créé automatiquement s'il n'existe pas encore), avec ses articles, sous-total, remise et total.")
    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(cartService.getCart(currentUserId()));
    }

    @Override
    @Operation(summary = "Récupérer mon panier (liste à un élément)",
            description = "Forme héritée du CRUD générique : renvoie une liste contenant uniquement le panier de l'utilisateur connecté. Préférer GET /cart.")
    @GetMapping("/list")
    public ResponseEntity<List<CartDto>> getAll() {
        return ResponseEntity.ok(List.of(cartService.getCart(currentUserId())));
    }

    @Override
    @Operation(summary = "Récupérer mon panier (page à un élément)",
            description = "Forme paginée héritée du CRUD générique — il n'y a jamais qu'un seul panier pour l'utilisateur connecté.")
    @GetMapping("/all")
    public Page<CartDto> getAll(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(List.of(cartService.getCart(currentUserId())), pageable, 1);
    }

    @Override
    @Operation(summary = "Compter mes paniers", description = "Renvoie toujours 1 : un utilisateur connecté a exactement un panier.")
    @GetMapping("/count")
    public long count() {
        return 1L;
    }

    @Override
    @Operation(summary = "Vider mon panier",
            description = "Retire tous les articles du panier et son code promo éventuel. Le panier lui-même n'est jamais supprimé, l'id dans l'URL est ignoré.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        cartService.clearCart(currentUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ajouter un article au panier",
            description = "Ajoute un produit (avec quantité, options sélectionnées et design éventuel) au panier. Si le même produit avec les mêmes options y est déjà, la quantité est cumulée.")
    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(@Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addItem(currentUserId(), request));
    }

    @Operation(summary = "Modifier la quantité d'un article", description = "Change la quantité d'un article déjà présent dans le panier.")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateItem(@PathVariable UUID itemId,
                                               @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(currentUserId(), itemId, request.getQuantity()));
    }

    @Operation(summary = "Retirer un article du panier", description = "Supprime un article du panier de l'utilisateur connecté.")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID itemId) {
        cartService.removeItem(currentUserId(), itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Appliquer un code promo", description = "Applique un code promo au panier, s'il est valide et non expiré.")
    @PostMapping("/promo")
    public ResponseEntity<CartDto> applyPromo(@Valid @RequestBody ApplyPromoRequest request) {
        return ResponseEntity.ok(cartService.applyPromoCode(currentUserId(), request.getCode()));
    }

    @Operation(summary = "Retirer le code promo", description = "Retire le code promo actuellement appliqué au panier.")
    @DeleteMapping("/promo")
    public ResponseEntity<CartDto> removePromo() {
        return ResponseEntity.ok(cartService.removePromoCode(currentUserId()));
    }

    private UUID currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUserResolver.resolveUserId(jwt);
    }
}
