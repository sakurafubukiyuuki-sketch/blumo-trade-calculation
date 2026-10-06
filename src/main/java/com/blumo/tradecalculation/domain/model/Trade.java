package com.blumo.tradecalculation.domain.model;

import com.blumo.tradecalculation.domain.exception.TradeAmountNotAcceptableException;

public record Trade(String userId, int amount) {

    public static final int MINIMUM_TRADE_AMOUNT = 1_000;

    public Trade {
        if (amount < MINIMUM_TRADE_AMOUNT) {
            throw new TradeAmountNotAcceptableException();
        }
    }
}
