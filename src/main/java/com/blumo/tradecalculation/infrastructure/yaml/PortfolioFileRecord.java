package com.blumo.tradecalculation.infrastructure.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

record PortfolioFileRecord(
        @JsonProperty("user_id") int userId,
        @JsonProperty("target_portfolio") Map<String, Integer> targetPortfolio
) {
}
