package com.blumo.tradecalculation.domain.exception;

public class PortfolioNotFoundException extends RuntimeException {

    public PortfolioNotFoundException(String userId) {
        super("target portfolio was not found. userId=" + userId);
    }
}
