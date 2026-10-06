package com.blumo.tradecalculation.application;

import com.blumo.tradecalculation.domain.exception.InconsistentStockDataException;
import com.blumo.tradecalculation.domain.exception.PortfolioNotFoundException;
import com.blumo.tradecalculation.domain.model.Holding;
import com.blumo.tradecalculation.domain.model.Order;
import com.blumo.tradecalculation.domain.model.Stock;
import com.blumo.tradecalculation.domain.model.TargetPortfolio;
import com.blumo.tradecalculation.domain.model.Trade;
import com.blumo.tradecalculation.domain.repository.PortfolioRepository;
import com.blumo.tradecalculation.domain.repository.StockRepository;
import com.blumo.tradecalculation.domain.service.AllocatedAmount;
import com.blumo.tradecalculation.domain.service.OrderCalculationService;
import com.blumo.tradecalculation.domain.service.PortfolioAllocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final PortfolioAllocationService portfolioAllocationService;
    private final OrderCalculationService orderCalculationService;

    public TradeService(
            PortfolioRepository portfolioRepository,
            StockRepository stockRepository,
            PortfolioAllocationService portfolioAllocationService,
            OrderCalculationService orderCalculationService) {
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
        this.portfolioAllocationService = portfolioAllocationService;
        this.orderCalculationService = orderCalculationService;
    }

    public CreatedTrade createTrade(String userId, int amount) {
        Trade trade = new Trade(userId, amount);
        TargetPortfolio targetPortfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new PortfolioNotFoundException(userId));
        Map<String, Stock> stocksByTicker = findStocks(targetPortfolio);
        List<AllocatedAmount> allocatedAmounts = portfolioAllocationService.allocate(trade.amount(), targetPortfolio, stocksByTicker);
        List<Order> orders = createOrders(allocatedAmounts, stocksByTicker);
        log.info("trade created. userId={}, amount={}, orderCount={}", trade.userId(), trade.amount(), orders.size());
        return new CreatedTrade(trade, targetPortfolio, orders);
    }

    private Map<String, Stock> findStocks(TargetPortfolio targetPortfolio) {
        List<String> tickers = new ArrayList<>();
        for (Holding holding : targetPortfolio.holdings()) {
            tickers.add(holding.symbol());
        }
        List<Stock> stocks = stockRepository.findAllByTickers(tickers);
        Map<String, Stock> stocksByTicker = new LinkedHashMap<>();
        for (Stock stock : stocks) {
            stocksByTicker.put(stock.ticker(), stock);
        }
        for (String ticker : tickers) {
            if (!stocksByTicker.containsKey(ticker)) {
                throw new InconsistentStockDataException("stock was not found. ticker=" + ticker);
            }
        }
        return stocksByTicker;
    }

    private List<Order> createOrders(List<AllocatedAmount> allocatedAmounts, Map<String, Stock> stocksByTicker) {
        List<Order> orders = new ArrayList<>();
        for (AllocatedAmount allocatedAmount : allocatedAmounts) {
            Stock stock = stocksByTicker.get(allocatedAmount.symbol());
            orders.add(new Order(
                    allocatedAmount.symbol(),
                    allocatedAmount.amount(),
                    orderCalculationService.calculateQuantity(allocatedAmount.amount(), stock.price())
            ));
        }
        return orders;
    }
}
