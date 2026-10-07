package com.blumo.tradecalculation.infrastructure.yaml;

import com.blumo.tradecalculation.domain.exception.InconsistentStockDataException;
import com.blumo.tradecalculation.domain.model.Stock;
import com.blumo.tradecalculation.domain.repository.StockRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public final class YamlStockRepository implements StockRepository {

    private final Map<String, Stock> stocksByTicker;

    public YamlStockRepository() {
        stocksByTicker = loadStocks();
    }

    @Override
    public List<Stock> findAllByTickers(List<String> tickers) {
        List<Stock> stocks = new ArrayList<>();
        for (String ticker : tickers) {
            Stock stock = stocksByTicker.get(ticker);
            if (stock != null) {
                stocks.add(stock);
            }
        }
        return stocks;
    }

    private static Map<String, Stock> loadStocks() {
        List<StockFileRecord> records = YamlResources.read(
                "stocks.yaml",
                new TypeReference<List<StockFileRecord>>() {
                }
        );
        Map<String, Stock> loaded = new LinkedHashMap<>();
        for (StockFileRecord record : records) {
            if (loaded.containsKey(record.ticker())) {
                throw new InconsistentStockDataException("duplicate stock. ticker=" + record.ticker());
            }
            loaded.put(record.ticker(), new Stock(record.ticker(), record.price(), record.tradable()));
        }
        return Map.copyOf(loaded);
    }
}
