package com.blumo.tradecalculation.presentation.dto;

import java.math.BigDecimal;

public record OrderResponse(String symbol, long amount, BigDecimal quantity) {
}
