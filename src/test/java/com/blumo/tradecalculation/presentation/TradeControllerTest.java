package com.blumo.tradecalculation.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TradeControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllocateUser1TradeOf10000() throws Exception {
        ResponseEntity<String> response = post("1", 10_000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertEquals(10_000, body.get("amount").asInt());
        assertEquals(40, body.get("target_portfolio").get("A").asInt());
        assertEquals(60, body.get("target_portfolio").get("B").asInt());
        assertEquals(4_000, order(body, "A").get("amount").asInt());
        assertEquals(0, new BigDecimal("4.000").compareTo(order(body, "A").get("quantity").decimalValue()));
        assertEquals(6_000, order(body, "B").get("amount").asInt());
        assertEquals(0, new BigDecimal("38.709").compareTo(order(body, "B").get("quantity").decimalValue()));
        assertQuantityDoesNotExceedAmount(body);
    }

    @Test
    void shouldReturnEmptyOrdersWhenAllStocksAreNotTradable() throws Exception {
        ResponseEntity<String> response = post("2", 10_000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertEquals(100, body.get("target_portfolio").get("E").asInt());
        assertEquals(0, body.get("orders").size());
    }

    @Test
    void shouldRedistributeAfterExcludingNonTradableStock() throws Exception {
        ResponseEntity<String> response = post("3", 10_000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertEquals(4_366, order(body, "A").get("amount").asInt());
        assertEquals(5_633, order(body, "B").get("amount").asInt());
        assertEquals(2, body.get("orders").size());
        assertQuantityDoesNotExceedAmount(body);
    }

    @Test
    void shouldRedistributeAfterExcludingOrderBelowMinimumAmount() throws Exception {
        ResponseEntity<String> response = post("4", 10_000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertEquals(5_050, order(body, "B").get("amount").asInt());
        assertEquals(4_949, order(body, "C").get("amount").asInt());
        assertEquals(0, new BigDecimal("32.580").compareTo(order(body, "B").get("quantity").decimalValue()));
        assertEquals(0, new BigDecimal("2.227").compareTo(order(body, "C").get("quantity").decimalValue()));
        assertEquals(2, body.get("orders").size());
        assertQuantityDoesNotExceedAmount(body);
    }

    @Test
    void shouldRejectTradeAmountBelowMinimum() throws Exception {
        ResponseEntity<String> response = post("1", 999);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("validation_error", objectMapper.readTree(response.getBody()).get("error").asText());
    }

    @Test
    void shouldAcceptTradeAmountAtMinimum() throws Exception {
        ResponseEntity<String> response = post("1", 1_000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertEquals(400, order(body, "A").get("amount").asInt());
        assertEquals(600, order(body, "B").get("amount").asInt());
    }

    @Test
    void shouldReturnNotFoundWhenPortfolioDoesNotExist() throws Exception {
        ResponseEntity<String> response = post("99", 10_000);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("resource_not_found", objectMapper.readTree(response.getBody()).get("error").asText());
    }

    private ResponseEntity<String> post(String userId, int amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"amount\":" + amount + "}", headers);
        return restTemplate.postForEntity("/users/" + userId + "/trades", request, String.class);
    }

    private static JsonNode order(JsonNode body, String symbol) {
        for (JsonNode order : body.get("orders")) {
            if (symbol.equals(order.get("symbol").asText())) {
                return order;
            }
        }
        throw new AssertionError("order was not returned. symbol=" + symbol);
    }

    private static void assertQuantityDoesNotExceedAmount(JsonNode body) {
        for (JsonNode order : body.get("orders")) {
            BigDecimal quantity = order.get("quantity").decimalValue();
            BigDecimal amount = order.get("amount").decimalValue();
            String symbol = order.get("symbol").asText();
            BigDecimal price = switch (symbol) {
                case "A" -> new BigDecimal("1000");
                case "B" -> new BigDecimal("155");
                case "C" -> new BigDecimal("2222");
                case "D" -> new BigDecimal("467");
                default -> throw new AssertionError("unknown symbol " + symbol);
            };
            assertTrue(quantity.multiply(price).compareTo(amount) <= 0);
        }
    }
}
