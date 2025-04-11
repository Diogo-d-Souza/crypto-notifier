package com.diogo.crypto.entities.dto;

public record PriceUpdateMessage(
        String symbol,
        String message,
        Double price
        ) {
}
