package com.blumo.tradecalculation.domain.exception;

public class InconsistentStockDataException extends RuntimeException {

    public InconsistentStockDataException(String detail) {
        super(detail);
    }
}
