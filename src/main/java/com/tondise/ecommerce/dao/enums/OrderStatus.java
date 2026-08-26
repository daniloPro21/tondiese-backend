package com.tondise.ecommerce.dao.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("En attente", "gray"),
    PROCESSING("En traitement", "blue"),
    SHIPPED("Expédiée", "indigo"),
    DELIVERED("Livrée", "green"),
    CANCELLED("Annulée", "red"),
    REFUNDED("Remboursée", "orange");

    private final String label;
    private final String color;

    OrderStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }
}
