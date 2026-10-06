package com.blumo.tradecalculation.presentation;

import com.blumo.tradecalculation.domain.exception.InconsistentStockDataException;
import com.blumo.tradecalculation.domain.exception.PortfolioNotFoundException;
import com.blumo.tradecalculation.domain.exception.TradeAmountNotAcceptableException;
import com.blumo.tradecalculation.presentation.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({
            TradeAmountNotAcceptableException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationError() {
        return ResponseEntity.badRequest().body(new ErrorResponse("validation_error"));
    }

    @ExceptionHandler(PortfolioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePortfolioNotFound() {
        return ResponseEntity.status(404).body(new ErrorResponse("resource_not_found"));
    }

    @ExceptionHandler(InconsistentStockDataException.class)
    public ResponseEntity<ErrorResponse> handleInconsistentStockData(InconsistentStockDataException exception) {
        log.error("stock master is inconsistent. {}", exception.getMessage());
        return ResponseEntity.internalServerError().body(new ErrorResponse("data_inconsistency"));
    }
}
