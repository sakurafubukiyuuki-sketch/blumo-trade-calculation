package com.blumo.tradecalculation.domain.service;

import com.blumo.tradecalculation.domain.exception.InconsistentStockDataException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderCalculationServiceTest {

    private final OrderCalculationService orderCalculationService = new OrderCalculationService();

    @Test
    void shouldCalculateQuantityWithThreeDecimalPlaces() {
        BigDecimal quantity = orderCalculationService.calculateQuantity(6_000, new BigDecimal("155"));

        assertEquals(0, new BigDecimal("38.709").compareTo(quantity));
    }

    @Test
    void shouldKeepQuantityTimesPriceWithinOrderAmount() {
        BigDecimal price = new BigDecimal("155");
        BigDecimal quantity = orderCalculationService.calculateQuantity(6_000, price);

        assertTrue(quantity.multiply(price).compareTo(new BigDecimal("6000")) <= 0);
        BigDecimal oneIncrement = quantity.add(new BigDecimal("0.001"));
        assertTrue(oneIncrement.multiply(price).compareTo(new BigDecimal("6000")) > 0);
    }

    @Test
    void shouldRejectNonPositivePrice() {
        assertThrows(InconsistentStockDataException.class,
                () -> orderCalculationService.calculateQuantity(1_000, BigDecimal.ZERO));
    }
}
