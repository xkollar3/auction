package edu.fi.muni.cz.marketplace.user.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Customer;
import com.stripe.model.SetupIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SetupIntentCreateParams;

import edu.fi.muni.cz.marketplace.user.dto.Address;
import edu.fi.muni.cz.marketplace.user.service.dto.ConnectedAccountResponse;
import edu.fi.muni.cz.marketplace.user.service.dto.SetupIntentResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StripeApiClient {

  public StripeApiClient(@Value("${stripe.api-key}") String stripeApiKey) {
    Stripe.apiKey = stripeApiKey;
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
   * @throws StripeApiClientException if customer creation fails
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
      throw new StripeApiClientException(
          "Failed to create Stripe customer: " + e.getMessage(), e);
    }
  }

  /**
   * Creates a Stripe SetupIntent for saving payment method details.
   *
   * @param idempotencyKey unique key to ensure idempotent creation
   * @return SetupIntentResponse containing the intent ID and client secret
   * @throws StripeApiClientException if setup intent creation fails
   */
  public SetupIntentResponse createSetupIntent(UUID idempotencyKey) {
    log.info("Creating Stripe SetupIntent with idempotency key: {}", idempotencyKey);

    try {
      SetupIntentCreateParams params = SetupIntentCreateParams.builder()
          .build();

      RequestOptions requestOptions = RequestOptions.builder()
          .setIdempotencyKey(idempotencyKey.toString())
          .build();

      SetupIntent setupIntent = SetupIntent.create(params, requestOptions);

      log.info("Successfully created Stripe SetupIntent: {}", setupIntent.getId());
      return new SetupIntentResponse(setupIntent.getId(), setupIntent.getClientSecret());

    } catch (StripeException e) {
      throw new StripeApiClientException(
          "Failed to create Stripe SetupIntent: " + e.getMessage(), e);
    }
  }

  /**
   * Creates a Stripe Express Connected Account for sellers in Czech Republic.
   *
   * @param idempotencyKey unique key to ensure idempotent creation
   * @param email          the seller's email address
   * @return ConnectedAccountResponse containing the account ID
   * @throws StripeApiClientException if account creation fails
   */
  public ConnectedAccountResponse createConnectedAccount(UUID idempotencyKey, String email) {
    log.info("Creating Stripe Connected Account with idempotency key: {}", idempotencyKey);

    try {
      AccountCreateParams params = AccountCreateParams.builder()
          .setType(AccountCreateParams.Type.EXPRESS)
          .setCountry("CZ")
          .setEmail(email)
          .setCapabilities(AccountCreateParams.Capabilities.builder()
              .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                  .setRequested(true)
                  .build())
              .build())
          .build();

      RequestOptions requestOptions = RequestOptions.builder()
          .setIdempotencyKey(idempotencyKey.toString())
          .build();

      Account account = Account.create(params, requestOptions);

      log.info("Successfully created Stripe Connected Account: {}", account.getId());
      return new ConnectedAccountResponse(account.getId());

    } catch (StripeException e) {
      throw new StripeApiClientException(
          "Failed to create Stripe Connected Account: " + e.getMessage(), e);
    }
  }
}
