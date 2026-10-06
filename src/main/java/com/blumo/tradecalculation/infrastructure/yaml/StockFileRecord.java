package com.blumo.tradecalculation.infrastructure.yaml;

import java.math.BigDecimal;

record StockFileRecord(String ticker, BigDecimal price, boolean tradable) {
}
