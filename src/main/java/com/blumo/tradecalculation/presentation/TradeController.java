package com.blumo.tradecalculation.presentation;

import com.blumo.tradecalculation.application.CreatedTrade;
import com.blumo.tradecalculation.application.TradeService;
import com.blumo.tradecalculation.domain.model.Holding;
import com.blumo.tradecalculation.domain.model.Order;
import com.blumo.tradecalculation.presentation.dto.CreateTradeRequest;
import com.blumo.tradecalculation.presentation.dto.OrderResponse;
import com.blumo.tradecalculation.presentation.dto.TradeResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/users/{userId}/trades")
    public TradeResponse createTrade(@PathVariable String userId, @Valid @RequestBody CreateTradeRequest request) {
        return toResponse(tradeService.createTrade(userId, request.amount()));
    }

    private TradeResponse toResponse(CreatedTrade createdTrade) {
        Map<String, Integer> targetPortfolio = new LinkedHashMap<>();
        for (Holding holding : createdTrade.targetPortfolio().holdings()) {
            targetPortfolio.put(holding.symbol(), holding.weight());
        }
        List<OrderResponse> orders = new ArrayList<>();
        for (Order order : createdTrade.orders()) {
            orders.add(new OrderResponse(order.symbol(), order.amount(), order.quantity()));
        }
        return new TradeResponse(createdTrade.trade().amount(), targetPortfolio, orders);
    }
}
