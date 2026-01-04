package edu.fi.muni.cz.marketplace.order.client.dto;

import java.math.BigDecimal;

/**
 * Result of a fund reservation operation containing the Stripe PaymentIntent ID and the net amount
 * after Stripe fees.
 *
 * @param paymentIntentId the Stripe PaymentIntent ID
 * @param netAmount       the amount after Stripe fees deduction (in CZK)
 */
public record FundReservationResult(
    String paymentIntentId,
    BigDecimal netAmount) {
}
