package com.blumo.tradecalculation.domain.repository;

import com.blumo.tradecalculation.domain.model.Stock;

import java.util.List;

public interface StockRepository {

    List<Stock> findAllByTickers(List<String> tickers);
}
