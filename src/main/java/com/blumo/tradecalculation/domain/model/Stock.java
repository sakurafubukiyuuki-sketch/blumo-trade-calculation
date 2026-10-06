package com.blumo.tradecalculation.domain.model;

import java.math.BigDecimal;

public record Stock(String ticker, BigDecimal price, boolean tradable) {

    public boolean isTradable() {
        return tradable;
    }
}
