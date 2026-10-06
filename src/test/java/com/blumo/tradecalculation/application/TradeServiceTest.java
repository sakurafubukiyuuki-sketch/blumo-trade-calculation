package com.blumo.tradecalculation.application;

import com.blumo.tradecalculation.domain.exception.PortfolioNotFoundException;
import com.blumo.tradecalculation.domain.exception.TradeAmountNotAcceptableException;
import com.blumo.tradecalculation.domain.model.Holding;
import com.blumo.tradecalculation.domain.model.Order;
import com.blumo.tradecalculation.domain.model.Stock;
import com.blumo.tradecalculation.domain.model.TargetPortfolio;
import com.blumo.tradecalculation.domain.repository.PortfolioRepository;
import com.blumo.tradecalculation.domain.repository.StockRepository;
import com.blumo.tradecalculation.domain.service.OrderCalculationService;
import com.blumo.tradecalculation.domain.service.PortfolioAllocationService;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeServiceTest {

    private final TradeService tradeService = new TradeService(
            new FixedPortfolioRepository(),
            new FixedStockRepository(),
            new PortfolioAllocationService(),
            new OrderCalculationService()
    );

    @Test
    void shouldRejectTradeAmountBelowMinimum() {
        assertThrows(TradeAmountNotAcceptableException.class, () -> tradeService.createTrade("1", 999));
    }

    @Test
    void shouldAcceptTradeAmountAtMinimum() {
        CreatedTrade createdTrade = tradeService.createTrade("1", 1_000);

        assertEquals(1_000, createdTrade.trade().amount());
        assertEquals(400, amountOf(createdTrade, "A"));
        assertEquals(600, amountOf(createdTrade, "B"));
    }

    @Test
    void shouldReturnNotFoundWhenPortfolioDoesNotExist() {
        assertThrows(PortfolioNotFoundException.class, () -> tradeService.createTrade("99", 10_000));
    }

    @Test
    void shouldReturnNoOrdersWhenEveryHoldingIsNotTradable() {
        CreatedTrade createdTrade = tradeService.createTrade("2", 10_000);

        assertTrue(createdTrade.orders().isEmpty());
        assertEquals(100, createdTrade.targetPortfolio().holdings().get(0).weight());
    }

    private static long amountOf(CreatedTrade createdTrade, String symbol) {
        for (Order order : createdTrade.orders()) {
            if (order.symbol().equals(symbol)) {
                return order.amount();
            }
        }
        throw new AssertionError("order was not created. symbol=" + symbol);
    }

    private static final class FixedPortfolioRepository implements PortfolioRepository {

        @Override
        public Optional<TargetPortfolio> findByUserId(String userId) {
            return switch (userId) {
                case "1" -> Optional.of(new TargetPortfolio("1", List.of(new Holding("A", 40), new Holding("B", 60))));
                case "2" -> Optional.of(new TargetPortfolio("2", List.of(new Holding("E", 100))));
                default -> Optional.empty();
            };
        }
    }

    private static final class FixedStockRepository implements StockRepository {

        @Override
        public List<Stock> findAllByTickers(List<String> tickers) {
            return List.of(
                    new Stock("A", new BigDecimal("1000"), true),
                    new Stock("B", new BigDecimal("155"), true),
                    new Stock("E", new BigDecimal("888"), false)
            );
        }
    }
}
