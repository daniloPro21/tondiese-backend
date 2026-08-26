package com.tondise.ecommerce.controllers.privates;

import com.tondise.ecommerce.config.security.CurrentUserResolver;
import com.tondise.ecommerce.dao.dto.OrderDto;
import com.tondise.ecommerce.dao.models.Order;
import com.tondise.ecommerce.dao.request.CreateOrderRequest;
import com.tondise.ecommerce.services.OrderService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Étend {@link AbstractController} comme {@code AddressController} : chaque
 * route héritée qui prend un id ou renvoie une liste est redéfinie pour se
 * limiter aux commandes de l'utilisateur courant. {@code UPDATE}/{@code
 * SOFT_DELETE}/{@code DELETE}/{@code FILTER}/{@code SEARCH} restent
 * désactivées — une commande ne change d'état que via {@code cancel}/{@code
 * reorder} (ou {@code AdminOrderController.updateStatus}), jamais par
 * écrasement générique de ses champs.
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "Commandes", description = "Commandes de l'utilisateur connecté : passer commande, consulter son historique, annuler, recommander.")
public class OrderController extends AbstractController<Order, OrderDto, CreateOrderRequest> {

    private final OrderService orderService;
    private final CurrentUserResolver currentUserResolver;

    public OrderController(OrderService service, CurrentUserResolver currentUserResolver) {
        super(service);
        this.orderService = service;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    protected Set<CrudOperation> allowedOperations() {
        return EnumSet.of(CrudOperation.CREATE, CrudOperation.READ);
    }

    @Override
    @Operation(summary = "Passer commande",
            description = "Crée une commande à partir du panier courant de l'utilisateur connecté (adresse de livraison/facturation, mode de livraison). Le panier doit être non vide ; il est vidé après création.")
    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest dto) {
        return ResponseEntity.ok(orderService.createOrder(currentUserId(), dto));
    }

    @Override
    @Operation(summary = "Récupérer une commande",
            description = "Renvoie une commande par son id, uniquement si elle appartient à l'utilisateur connecté (404 sinon).")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(currentUserId(), id));
    }

    @Override
    @Operation(summary = "Lister mes 50 commandes les plus récentes",
            description = "Version non paginée (bornée à 50 résultats) de mon historique de commandes — préférer GET /orders/all pour une pagination complète.")
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAll() {
        Page<OrderDto> firstPage = orderService.getOrders(currentUserId(),
                PageRequest.of(0, 50, Sort.by("created").descending()));
        return ResponseEntity.ok(firstPage.getContent());
    }

    @Override
    @Operation(summary = "Lister mes commandes (paginé)",
            description = "Renvoie l'historique de commandes de l'utilisateur connecté, trié du plus récent au plus ancien, page par page.")
    @GetMapping("/all")
    public Page<OrderDto> getAll(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return orderService.getOrders(currentUserId(), PageRequest.of(page, size, Sort.by("created").descending()));
    }

    @Override
    @Operation(summary = "Compter mes commandes", description = "Renvoie le nombre total de commandes passées par l'utilisateur connecté.")
    @GetMapping("/count")
    public long count() {
        return orderService.countOrders(currentUserId());
    }

    @Operation(summary = "Annuler une commande",
            description = "Annule une commande de l'utilisateur connecté, uniquement si elle est encore dans un statut annulable (en attente ou en traitement).")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelOrder(currentUserId(), id));
    }

    @Operation(summary = "Recommander",
            description = "Ajoute au panier courant les mêmes articles qu'une commande passée de l'utilisateur connecté.")
    @PostMapping("/{id}/reorder")
    public ResponseEntity<OrderDto> reorder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.reorder(currentUserId(), id));
    }

    private UUID currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUserResolver.resolveUserId(jwt);
    }
}
