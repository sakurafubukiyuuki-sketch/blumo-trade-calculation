package com.blumo.tradecalculation.presentation.dto;

import java.util.List;
import java.util.Map;

public record TradeResponse(int amount, Map<String, Integer> targetPortfolio, List<OrderResponse> orders) {
}
