package com.tondise.ecommerce.controllers.privates;

import com.tondise.ecommerce.config.security.CurrentUserResolver;
import com.tondise.ecommerce.dao.dto.GatewayConfigDto;
import com.tondise.ecommerce.dao.dto.PaymentConfigDto;
import com.tondise.ecommerce.dao.dto.PaymentConfirmResultDto;
import com.tondise.ecommerce.dao.dto.PaymentIntentDto;
import com.tondise.ecommerce.dao.dto.PaymentMethodDto;
import com.tondise.ecommerce.dao.dto.PaymentTransactionDto;
import com.tondise.ecommerce.dao.models.PaymentTransaction;
import com.tondise.ecommerce.dao.request.ConfirmPaymentRequest;
import com.tondise.ecommerce.dao.request.CreatePaymentIntentRequest;
import com.tondise.ecommerce.dao.request.PaymentTransactionRequest;
import com.tondise.ecommerce.services.PaymentService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Étend {@link AbstractController} comme le reste de {@code privates/}, sur
 * l'entité {@link PaymentTransaction} (la plus proche d'une ressource CRUD
 * ici). {@code CREATE}/{@code UPDATE}/{@code SOFT_DELETE}/{@code DELETE}/
 * {@code FILTER}/{@code SEARCH} restent désactivées : une transaction de
 * paiement ne se crée que via {@link #createIntent}, ne se met à jour que via
 * {@link #confirm} (jamais par écrasement générique de champs financiers), et
 * ne se supprime jamais. Seule lecture (scopée à l'utilisateur courant) reste
 * ouverte.
 */
@RestController
@RequestMapping("/payments")
@Tag(name = "Paiements", description = "Configuration des moyens de paiement disponibles, création et confirmation d'un paiement, historique des transactions de l'utilisateur connecté.")
public class PaymentController extends AbstractController<PaymentTransaction, PaymentTransactionDto, PaymentTransactionRequest> {

    private final PaymentService paymentService;
    private final CurrentUserResolver currentUserResolver;

    public PaymentController(PaymentService service, CurrentUserResolver currentUserResolver) {
        super(service);
        this.paymentService = service;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    protected Set<CrudOperation> allowedOperations() {
        return EnumSet.of(CrudOperation.READ);
    }

    @Override
    @Operation(summary = "Récupérer une transaction de paiement",
            description = "Renvoie une transaction par son id, uniquement si sa commande appartient à l'utilisateur connecté. Ne contient jamais l'identifiant du gateway ni le secret client (voir POST /payments/intent pour ça).")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentTransactionDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getTransaction(currentUserId(), id));
    }

    @Override
    @Operation(summary = "Lister mes 50 transactions les plus récentes",
            description = "Version non paginée (bornée à 50 résultats) de l'historique de mes transactions — préférer GET /payments/all pour une pagination complète.")
    @GetMapping
    public ResponseEntity<List<PaymentTransactionDto>> getAll() {
        return ResponseEntity.ok(paymentService.getTransactions(currentUserId(), PageRequest.of(0, 50)).getContent());
    }

    @Override
    @Operation(summary = "Lister mes transactions (paginé)", description = "Renvoie l'historique des transactions de paiement liées aux commandes de l'utilisateur connecté.")
    @GetMapping("/all")
    public Page<PaymentTransactionDto> getAll(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return paymentService.getTransactions(currentUserId(), PageRequest.of(page, size));
    }

    @Override
    @Operation(summary = "Compter mes transactions", description = "Renvoie le nombre total de transactions de paiement liées aux commandes de l'utilisateur connecté.")
    @GetMapping("/count")
    public long count() {
        return paymentService.countTransactions(currentUserId());
    }

    @Operation(summary = "Récupérer la configuration de paiement",
            description = "Renvoie la clé publique Stripe, le gateway par défaut et la liste des gateways activés — à appeler avant d'afficher le formulaire de paiement.")
    @GetMapping("/config")
    public ResponseEntity<PaymentConfigDto> getConfig() {
        return ResponseEntity.ok(paymentService.getConfig());
    }

    @Operation(summary = "Lister les moyens de paiement disponibles",
            description = "Renvoie la liste des méthodes de paiement (carte, mobile money...) proposées par chaque gateway activé.")
    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethodDto>> getMethods() {
        return ResponseEntity.ok(paymentService.getMethods());
    }

    @Operation(summary = "Récupérer la config d'un gateway",
            description = "Renvoie la configuration publique et les méthodes disponibles d'un gateway de paiement donné (ex. stripe, mtn_mobile_money, orange_money).")
    @GetMapping("/gateways/{gateway}")
    public ResponseEntity<GatewayConfigDto> getGatewayConfig(@PathVariable String gateway) {
        return ResponseEntity.ok(paymentService.getGatewayConfig(gateway));
    }

    @Operation(summary = "Créer une intention de paiement",
            description = "Initie le paiement d'une commande de l'utilisateur connecté auprès du gateway choisi (ou du gateway par défaut) ; renvoie l'id et le client secret à utiliser côté front. Le numéro de téléphone est requis pour le Mobile Money.")
    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentDto> createIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        return ResponseEntity.ok(paymentService.createIntent(
                currentUserId(), request.getOrderId(), request.getGateway(), request.getPhoneNumber()));
    }

    @Operation(summary = "Confirmer un paiement",
            description = "Marque la transaction comme réussie après validation côté gateway, et met à jour le statut de paiement de la commande associée.")
    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResultDto> confirm(@Valid @RequestBody ConfirmPaymentRequest request) {
        return ResponseEntity.ok(paymentService.confirm(
                currentUserId(), request.getPaymentIntentId(), request.getGateway()));
    }

    private UUID currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUserResolver.resolveUserId(jwt);
    }
}
