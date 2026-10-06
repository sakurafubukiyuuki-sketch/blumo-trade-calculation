package com.blumo.tradecalculation.domain.exception;

public class TradeAmountNotAcceptableException extends RuntimeException {

    public TradeAmountNotAcceptableException() {
        super("trade amount is below the minimum");
    }
}
