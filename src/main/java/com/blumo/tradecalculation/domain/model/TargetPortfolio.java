package com.blumo.tradecalculation.domain.model;

import java.util.List;

public record TargetPortfolio(String userId, List<Holding> holdings) {

    public TargetPortfolio {
        holdings = List.copyOf(holdings);
    }
}
