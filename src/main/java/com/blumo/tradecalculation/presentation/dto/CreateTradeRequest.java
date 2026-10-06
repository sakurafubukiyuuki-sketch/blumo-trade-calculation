package com.blumo.tradecalculation.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record CreateTradeRequest(@NotNull Integer amount) {
}
