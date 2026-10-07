package com.blumo.tradecalculation.infrastructure.yaml;

import com.blumo.tradecalculation.domain.exception.InconsistentStockDataException;
import com.blumo.tradecalculation.domain.model.Holding;
import com.blumo.tradecalculation.domain.model.TargetPortfolio;
import com.blumo.tradecalculation.domain.repository.PortfolioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public final class YamlPortfolioRepository implements PortfolioRepository {

    private final Map<String, TargetPortfolio> portfoliosByUserId;

    public YamlPortfolioRepository() {
        portfoliosByUserId = loadPortfolios();
    }

    @Override
    public Optional<TargetPortfolio> findByUserId(String userId) {
        return Optional.ofNullable(portfoliosByUserId.get(userId));
    }

    private static Map<String, TargetPortfolio> loadPortfolios() {
        List<PortfolioFileRecord> records = YamlResources.read(
                "portfolios.yaml",
                new TypeReference<List<PortfolioFileRecord>>() {
                }
        );
        Map<String, TargetPortfolio> loaded = new LinkedHashMap<>();
        for (PortfolioFileRecord record : records) {
            String userId = Integer.toString(record.userId());
            if (loaded.containsKey(userId)) {
                throw new InconsistentStockDataException("duplicate portfolio. userId=" + userId);
            }
            loaded.put(userId, toTargetPortfolio(userId, record));
        }
        return Map.copyOf(loaded);
    }

    private static TargetPortfolio toTargetPortfolio(String userId, PortfolioFileRecord record) {
        List<Holding> holdings = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : record.targetPortfolio().entrySet()) {
            holdings.add(new Holding(entry.getKey(), entry.getValue()));
        }
        return new TargetPortfolio(userId, holdings);
    }
}
