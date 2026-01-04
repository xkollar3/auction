package edu.fi.muni.cz.marketplace.order.client.dto;

import java.math.BigDecimal;

/**
 * Result of a fund reservation operation containing the Stripe PaymentIntent ID and the gross
 * amount charged.
 *
 * @param paymentIntentId the Stripe PaymentIntent ID
 * @param grossAmount     the gross amount charged (in CZK)
 */
public record FundReservationResult(
    String paymentIntentId,
    BigDecimal grossAmount) {
}
