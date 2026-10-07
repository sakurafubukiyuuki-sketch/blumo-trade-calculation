package com.blumo.tradecalculation.presentation.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record TradeResponse(int amount, Map<String, Integer> targetPortfolio, List<OrderResponse> orders) {

    public TradeResponse {
        targetPortfolio = Collections.unmodifiableMap(new LinkedHashMap<>(targetPortfolio));
        orders = List.copyOf(orders);
    }

    @Override
    public Map<String, Integer> targetPortfolio() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(targetPortfolio));
    }

    @Override
    public List<OrderResponse> orders() {
        return List.copyOf(orders);
    }
}
