package com.tondise.ecommerce.dao.request;

/**
 * Type {@code K} vide pour {@code AbstractController<PaymentTransaction, ...>} :
 * une transaction ne se crée jamais depuis un payload client direct (voir
 * {@code PaymentController.createIntent}, qui résout la commande et le
 * gateway côté serveur) — {@code CREATE} reste d'ailleurs exclu de
 * {@code allowedOperations()}.
 */
public class PaymentTransactionRequest {
}
