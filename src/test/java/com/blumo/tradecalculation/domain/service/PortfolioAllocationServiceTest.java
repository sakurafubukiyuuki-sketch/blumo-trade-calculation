package com.blumo.tradecalculation.domain.service;

import com.blumo.tradecalculation.domain.exception.InconsistentStockDataException;
import com.blumo.tradecalculation.domain.model.Holding;
import com.blumo.tradecalculation.domain.model.Stock;
import com.blumo.tradecalculation.domain.model.TargetPortfolio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortfolioAllocationServiceTest {

    private final PortfolioAllocationService portfolioAllocationService = new PortfolioAllocationService();

    @Test
    void shouldAllocateTradeAmountByTargetWeight() {
        List<AllocatedAmount> allocatedAmounts = portfolioAllocationService.allocate(
                10_000,
                portfolio("1", holding("A", 40), holding("B", 60)),
                stocks()
        );

        assertEquals(4_000, amountOf(allocatedAmounts, "A"));
        assertEquals(6_000, amountOf(allocatedAmounts, "B"));
    }

    @Test
    void shouldExcludeNonTradableStock() {
        List<AllocatedAmount> allocatedAmounts = portfolioAllocationService.allocate(
                10_000,
                portfolio("2", holding("E", 100)),
                stocks()
        );

        assertTrue(allocatedAmounts.isEmpty());
    }

    @Test
    void shouldRedistributeWeightAfterRemovingNonTradableStock() {
        List<AllocatedAmount> allocatedAmounts = portfolioAllocationService.allocate(
                10_000,
                portfolio("3", holding("A", 31), holding("B", 40), holding("E", 29)),
                stocks()
        );

        assertEquals(4_366, amountOf(allocatedAmounts, "A"));
        assertEquals(5_633, amountOf(allocatedAmounts, "B"));
        assertEquals(2, allocatedAmounts.size());
    }

    @Test
    void shouldExcludeOrderWhenAmountIsLessThanMinimum() {
        List<AllocatedAmount> allocatedAmounts = portfolioAllocationService.allocate(
                1_000,
                portfolio("boundary", holding("LOW", 199), holding("HIGH", 801)),
                Map.of(
                        "LOW", stock("LOW", "100", true),
                        "HIGH", stock("HIGH", "100", true)
                )
        );

        assertEquals(1, allocatedAmounts.size());
        assertEquals(1_000, amountOf(allocatedAmounts, "HIGH"));
    }

    @Test
    void shouldRedistributeWeightAfterRemovingOrderBelowMinimum() {
        List<AllocatedAmount> allocatedAmounts = portfolioAllocationService.allocate(
                10_000,
                portfolio("4", holding("B", 50), holding("C", 49), holding("D", 1)),
                stocks()
        );

        assertEquals(5_050, amountOf(allocatedAmounts, "B"));
        assertEquals(4_949, amountOf(allocatedAmounts, "C"));
        assertEquals(2, allocatedAmounts.size());
    }

    @Test
    void shouldCreateOrderWhenAmountEqualsMinimum() {
        List<AllocatedAmount> allocatedAmounts = portfolioAllocationService.allocate(
                1_000,
                portfolio("boundary", holding("LOW", 200), holding("HIGH", 800)),
                Map.of(
                        "LOW", stock("LOW", "100", true),
                        "HIGH", stock("HIGH", "100", true)
                )
        );

        assertEquals(200, amountOf(allocatedAmounts, "LOW"));
        assertEquals(800, amountOf(allocatedAmounts, "HIGH"));
    }

    @Test
    void shouldRejectNonPositiveStockPrice() {
        assertThrows(InconsistentStockDataException.class, () -> portfolioAllocationService.allocate(
                10_000,
                portfolio("1", holding("A", 100)),
                Map.of("A", stock("A", "0", true))
        ));
    }

    @Test
    void shouldRejectMissingStock() {
        assertThrows(InconsistentStockDataException.class, () -> portfolioAllocationService.allocate(
                10_000,
                portfolio("1", holding("Z", 100)),
                stocks()
        ));
    }

    private static long amountOf(List<AllocatedAmount> allocatedAmounts, String symbol) {
        for (AllocatedAmount allocatedAmount : allocatedAmounts) {
            if (allocatedAmount.symbol().equals(symbol)) {
                return allocatedAmount.amount();
            }
        }
        throw new AssertionError("order was not allocated. symbol=" + symbol);
    }

    private static TargetPortfolio portfolio(String userId, Holding... holdings) {
        return new TargetPortfolio(userId, List.of(holdings));
    }

    private static Holding holding(String symbol, int weight) {
        return new Holding(symbol, weight);
    }

    private static Map<String, Stock> stocks() {
        return Map.of(
                "A", stock("A", "1000", true),
                "B", stock("B", "155", true),
                "C", stock("C", "2222", true),
                "D", stock("D", "467", true),
                "E", stock("E", "888", false)
        );
    }

    private static Stock stock(String ticker, String price, boolean tradable) {
        return new Stock(ticker, new BigDecimal(price), tradable);
    }
}
