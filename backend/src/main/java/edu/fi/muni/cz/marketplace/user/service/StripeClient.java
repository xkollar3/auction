package edu.fi.muni.cz.marketplace.user.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;

import edu.fi.muni.cz.marketplace.user.aggregate.Address;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StripeClient {

  public StripeClient(@Value("${stripe.api-key}") String stripeApiKey) {
    Stripe.apiKey = stripeApiKey;
    log.info("Stripe client initialized with API key");
  }

  /**
   * Creates a Stripe customer with full details using the aggregate ID as
   * idempotency key.
   *
   * @param aggregateId     the aggregate ID to use as idempotency key
   * @param email           customer email from Keycloak
   * @param name            customer name from Keycloak
   * @param phone           customer phone from Keycloak
   * @param shippingAddress customer shipping address from request
   * @return the Stripe customer ID
   * @throws StripeCustomerCreationException if customer creation fails
   */
  public String createCustomer(
      UUID aggregateId,
      String email,
      String name,
      String phone,
      Address shippingAddress) {

    log.info("Creating Stripe customer with idempotency key: {}", aggregateId);

    try {
      CustomerCreateParams.Builder builder = CustomerCreateParams.builder()
          .setEmail(email)
          .setName(name)
          .setPhone(phone);

      if (shippingAddress != null) {
        CustomerCreateParams.Shipping shipping = CustomerCreateParams.Shipping.builder()
            .setName(name)
            .setPhone(phone)
            .setAddress(CustomerCreateParams.Shipping.Address.builder()
                .setLine1(shippingAddress.getLine1())
                .setLine2(shippingAddress.getLine2())
                .setCity(shippingAddress.getCity())
                .setState(shippingAddress.getState())
                .setPostalCode(shippingAddress.getPostalCode())
                .setCountry(shippingAddress.getCountry())
                .build())
            .build();

        builder.setShipping(shipping);
      }

      CustomerCreateParams params = builder.build();

      RequestOptions requestOptions = RequestOptions.builder()
          .setIdempotencyKey(aggregateId.toString())
          .build();

      Customer customer = Customer.create(params, requestOptions);

      log.info("Successfully created Stripe customer: {}", customer.getId());
      return customer.getId();

    } catch (StripeException e) {
      throw new StripeCustomerCreationException(
          "Failed to create Stripe customer: " + e.getMessage(), e);
    }
  }
}
