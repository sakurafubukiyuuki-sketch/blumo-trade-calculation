package com.blumo.tradecalculation.domain.service;

import com.blumo.tradecalculation.domain.exception.InconsistentStockDataException;
import com.blumo.tradecalculation.domain.model.Holding;
import com.blumo.tradecalculation.domain.model.Order;
import com.blumo.tradecalculation.domain.model.Stock;
import com.blumo.tradecalculation.domain.model.TargetPortfolio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PortfolioAllocationService {

    public List<AllocatedAmount> allocate(int tradeAmount, TargetPortfolio targetPortfolio, Map<String, Stock> stocksByTicker) {
        List<Holding> candidates = filterTradableHoldings(targetPortfolio, stocksByTicker);
        while (!candidates.isEmpty()) {
            long totalWeight = totalWeight(candidates);
            List<AllocatedAmount> allocatedAmounts = new ArrayList<>();
            List<Holding> holdingsMeetingMinimum = new ArrayList<>();
            boolean excludedBelowMinimum = false;
            for (Holding holding : candidates) {
                long orderAmount = calculateOrderAmount(tradeAmount, holding.weight(), totalWeight);
                if (orderAmount < Order.MINIMUM_ORDER_AMOUNT) {
                    excludedBelowMinimum = true;
                    continue;
                }
                holdingsMeetingMinimum.add(holding);
                allocatedAmounts.add(new AllocatedAmount(holding.symbol(), orderAmount));
            }
            if (!excludedBelowMinimum) {
                return allocatedAmounts;
            }
            // 200円未満を除くと残存銘柄のウェイト合計が変わるため、配分をやり直す。
            candidates = holdingsMeetingMinimum;
        }
        return List.of();
    }

    private List<Holding> filterTradableHoldings(TargetPortfolio targetPortfolio, Map<String, Stock> stocksByTicker) {
        List<Holding> tradableHoldings = new ArrayList<>();
        for (Holding holding : targetPortfolio.holdings()) {
            Stock stock = stocksByTicker.get(holding.symbol());
            if (stock == null) {
                throw new InconsistentStockDataException("stock was not found. ticker=" + holding.symbol());
            }
            if (stock.price() == null || stock.price().signum() <= 0) {
                throw new InconsistentStockDataException("stock price is not positive. ticker=" + holding.symbol());
            }
            if (stock.isTradable()) {
                tradableHoldings.add(holding);
            }
        }
        return tradableHoldings;
    }

    private long totalWeight(List<Holding> holdings) {
        long totalWeight = 0;
        for (Holding holding : holdings) {
            totalWeight += holding.weight();
        }
        if (totalWeight <= 0) {
            throw new InconsistentStockDataException("total valid weight is not positive");
        }
        return totalWeight;
    }

    private long calculateOrderAmount(int tradeAmount, int weight, long totalWeight) {
        return Math.floorDiv((long) tradeAmount * weight, totalWeight);
    }
}
