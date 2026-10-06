package com.blumo.tradecalculation.domain.repository;

import com.blumo.tradecalculation.domain.model.TargetPortfolio;

import java.util.Optional;

public interface PortfolioRepository {

    Optional<TargetPortfolio> findByUserId(String userId);
}
