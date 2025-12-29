package edu.fi.muni.cz.marketplace.order.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StripeFundsApiClient {

  public StripeFundsApiClient(@Value("${stripe.api-key}") String stripeApiKey) {
    Stripe.apiKey = stripeApiKey;
  }

  /**
   * Reserves funds from a payment method by creating a PaymentIntent.
   * Uses CZK currency and automatically captures the payment.
   *
   * @param customerId      the Stripe customer ID
   * @param paymentMethodId the Stripe payment method ID to charge
   * @param amount          the amount to reserve in CZK
   * @param idempotencyKey  unique key to ensure idempotent creation
   * @return the Stripe PaymentIntent ID
   * @throws StripeFundsApiClientException if payment intent creation fails
   */
  public String reserveFunds(String customerId, String paymentMethodId, BigDecimal amount, UUID idempotencyKey) {
    log.info("Reserving funds for customer {} with payment method {} and amount {} CZK",
        customerId, paymentMethodId, amount);

    long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

    try {
      PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
          .setAmount(amountInCents)
          .setCurrency("czk")
          .setCustomer(customerId)
          .setPaymentMethod(paymentMethodId)
          .setConfirm(true)
          .setAutomaticPaymentMethods(
              PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                  .setEnabled(true)
                  .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                  .build())
          .build();

      RequestOptions requestOptions = RequestOptions.builder()
          .setIdempotencyKey("pay_" + idempotencyKey.toString())
          .build();

      PaymentIntent paymentIntent = PaymentIntent.create(params, requestOptions);

      log.info("Successfully reserved funds with PaymentIntent: {}", paymentIntent.getId());
      return paymentIntent.getId();
    } catch (StripeException e) {
      throw new StripeFundsApiClientException(
          "Failed to reserve funds: " + e.getMessage(), e);
    }
  }

  /**
   * Refunds a payment intent.
   *
   * @param paymentIntentId the Stripe payment intent ID to refund
   * @param orderId         the order ID used for idempotency key
   * @return the Stripe Refund ID
   * @throws StripeFundsApiClientException if refund creation fails
   */
  public String refundPayment(String paymentIntentId, UUID orderId) {
    log.info("Refunding payment intent {} for order {}", paymentIntentId, orderId);

    try {
      RefundCreateParams params = RefundCreateParams.builder()
          .setPaymentIntent(paymentIntentId)
          .build();

      RequestOptions requestOptions = RequestOptions.builder()
          .setIdempotencyKey("refund_" + orderId.toString())
          .build();

      Refund refund = Refund.create(params, requestOptions);

      log.info("Successfully refunded payment with Refund: {}", refund.getId());
      return refund.getId();
    } catch (StripeException e) {
      throw new StripeFundsApiClientException(
          "Failed to refund payment: " + e.getMessage(), e);
    }
  }
}
