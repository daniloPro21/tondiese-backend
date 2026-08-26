package com.tondise.ecommerce.controllers.publish;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Point d'entrée des webhooks des gateways de paiement (Stripe, MTN, Orange Money).
 * TODO: vérifier la signature de chaque provider avant de traiter l'évènement.
 */
@Slf4j
@RestController
@RequestMapping("/payments/webhook")
@Tag(name = "Webhooks paiement", description = "Point d'entrée serveur-à-serveur pour les notifications des gateways de paiement — non destiné au front.")
public class PaymentWebhookController {

    @Operation(summary = "Recevoir un webhook de gateway de paiement",
            description = "Appelé par le gateway (Stripe, MTN Mobile Money, Orange Money) pour notifier un évènement de paiement. Pas d'authentification applicative — la signature du provider doit être vérifiée (TODO).")
    @PostMapping("/{gateway}")
    public ResponseEntity<Void> handleWebhook(
            @Parameter(description = "Identifiant du gateway émetteur") @PathVariable String gateway,
            @RequestBody String payload) {
        log.info("Webhook reçu du gateway {}", gateway);
        return ResponseEntity.ok().build();
    }
}
