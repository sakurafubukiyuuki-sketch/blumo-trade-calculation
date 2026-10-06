package com.blumo.tradecalculation.domain.model;

import java.math.BigDecimal;

public record Order(String symbol, long amount, BigDecimal quantity) {

    public static final long MINIMUM_ORDER_AMOUNT = 200;

    public Order {
        if (amount < MINIMUM_ORDER_AMOUNT) {
            throw new IllegalArgumentException("order amount is below the minimum");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("quantity is required");
        }
    }
}
