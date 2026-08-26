package com.tondise.ecommerce.controllers.admin;

import com.tondise.ecommerce.dao.dto.OrderDto;
import com.tondise.ecommerce.dao.enums.OrderStatus;
import com.tondise.ecommerce.dao.models.Order;
import com.tondise.ecommerce.dao.request.CreateOrderRequest;
import com.tondise.ecommerce.dao.response.PaginatedResponse;
import com.tondise.ecommerce.services.OrderService;
import com.tondise.ecommerce.services.export.OrderExportService;
import com.tondise.utils.abstractController.AbstractController;
import com.tondise.utils.abstractController.CrudOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Étend {@link AbstractController} : contrairement à {@code OrderController}
 * (client), pas de scoping par utilisateur ici — un admin voit toutes les
 * commandes, donc {@code getById}/{@code count}/le listing paginé hérités
 * ({@code GET /all}) restent tels quels (non surchargés). {@code CREATE}/
 * {@code UPDATE}/{@code SOFT_DELETE}/{@code DELETE}/{@code FILTER}/{@code
 * SEARCH} restent désactivées : un statut de commande ne change que via
 * {@link #updateStatus}, jamais par écrasement générique de champs
 * financiers. {@code GET /admin/orders} (racine) garde sa forme historique
 * ({@code PaginatedResponse}) — {@code getAll()} hérité est déplacé sur
 * {@code /list} pour éviter le conflit de route.
 */
@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Commandes", description = "Consultation de toutes les commandes et changement de statut. Réservé aux administrateurs.")
public class AdminOrderController extends AbstractController<Order, OrderDto, CreateOrderRequest> {

    private final OrderService orderService;
    private final OrderExportService orderExportService;

    public AdminOrderController(OrderService service, OrderExportService orderExportService) {
        super(service);
        this.orderService = service;
        this.orderExportService = orderExportService;
    }

    @Override
    protected Set<CrudOperation> allowedOperations() {
        return EnumSet.of(CrudOperation.READ);
    }

    @Operation(summary = "Lister toutes les commandes (paginé)",
            description = "Renvoie toutes les commandes de tous les utilisateurs, triées de la plus récente à la plus ancienne, sous forme paginée.")
    @GetMapping
    public PaginatedResponse<OrderDto> getOrders(
            @Parameter(description = "Numéro de page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre de commandes par page") @RequestParam(defaultValue = "20") int limit) {
        Page<OrderDto> orders = orderService.getAllOrders(
                PageRequest.of(page, limit, Sort.by("created").descending()));
        return PaginatedResponse.of(orders, orders.getContent());
    }

    @Override
    @Operation(summary = "Lister les 50 commandes les plus récentes",
            description = "Version non paginée (bornée à 50 résultats) de la liste de toutes les commandes — préférer GET /admin/orders pour une pagination complète.")
    @GetMapping("/list")
    public ResponseEntity<List<OrderDto>> getAll() {
        Page<OrderDto> firstPage = orderService.getAllOrders(PageRequest.of(0, 50, Sort.by("created").descending()));
        return ResponseEntity.ok(firstPage.getContent());
    }

    @Operation(summary = "Changer le statut d'une commande",
            description = "Met à jour le statut d'une commande (ex. PROCESSING, SHIPPED, DELIVERED, CANCELLED) indépendamment de son propriétaire.")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(
            @Parameter(description = "Id de la commande") @PathVariable UUID id,
            @Parameter(description = "Nouveau statut") @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @Operation(summary = "Exporter les commandes en PDF",
            description = "Génère un PDF listant l'ensemble des commandes.")
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        return fileResponse(orderExportService.exportPdf(), "commandes.pdf", MediaType.APPLICATION_PDF);
    }

    @Operation(summary = "Exporter les commandes en Excel",
            description = "Génère un fichier Excel (.xlsx) listant l'ensemble des commandes.")
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        return fileResponse(orderExportService.exportExcel(), "commandes.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    private ResponseEntity<byte[]> fileResponse(byte[] content, String filename, MediaType mediaType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(content);
    }
}
