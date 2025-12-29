package edu.fi.muni.cz.marketplace.order.dto;

import java.math.BigDecimal;

public record ReserveFundsRequest(
    String customerId,
    String paymentMethodId,
    BigDecimal amount) {
}
