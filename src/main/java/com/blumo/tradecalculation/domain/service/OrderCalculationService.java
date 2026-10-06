package com.blumo.tradecalculation.domain.service;

import com.blumo.tradecalculation.domain.exception.InconsistentStockDataException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderCalculationService {

    public static final int QUANTITY_SCALE = 3;

    public BigDecimal calculateQuantity(long orderAmount, BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new InconsistentStockDataException("stock price is not positive");
        }
        // amount / price を小数第3位で切り捨て、quantity * price が注文金額を超えないようにする。
        BigDecimal scaledAmount = BigDecimal.valueOf(orderAmount).movePointRight(QUANTITY_SCALE);
        return scaledAmount.divide(price, 0, RoundingMode.FLOOR).movePointLeft(QUANTITY_SCALE);
    }
}
