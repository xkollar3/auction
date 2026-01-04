package edu.fi.muni.cz.marketplace.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReserveFundsRequest(
    String customerId,
    String paymentMethodId,
    BigDecimal amount,
    UUID sellerId,
    String sellerStripeAccountId) {
}
