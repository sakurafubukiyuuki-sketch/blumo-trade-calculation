package com.blumo.tradecalculation.application;

import com.blumo.tradecalculation.domain.model.Order;
import com.blumo.tradecalculation.domain.model.TargetPortfolio;
import com.blumo.tradecalculation.domain.model.Trade;

import java.util.List;

public record CreatedTrade(Trade trade, TargetPortfolio targetPortfolio, List<Order> orders) {

    public CreatedTrade {
        orders = List.copyOf(orders);
    }
}
