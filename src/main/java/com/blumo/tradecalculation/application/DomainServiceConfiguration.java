package com.blumo.tradecalculation.application;

import com.blumo.tradecalculation.domain.service.OrderCalculationService;
import com.blumo.tradecalculation.domain.service.PortfolioAllocationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfiguration {

    @Bean
    PortfolioAllocationService portfolioAllocationService() {
        return new PortfolioAllocationService();
    }

    @Bean
    OrderCalculationService orderCalculationService() {
        return new OrderCalculationService();
    }
}
