package edu.fi.muni.cz.marketplace.user.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SetupIntentCreateParams;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountLinkCreateParams;

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
   * Attaches a payment method to a customer.
   *
   * @param customerId      the Stripe customer ID
   * @param paymentMethodId the Stripe payment method ID
   * @throws StripeApiClientException if attachment fails
   */
  public void attachPaymentMethod(String customerId, String paymentMethodId) {
    log.info("Attaching payment method {} to customer {}", paymentMethodId, customerId);

    try {
      PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

      PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
          .setCustomer(customerId)
          .build();

      paymentMethod.attach(params);

      log.info("Successfully attached payment method {} to customer {}", paymentMethodId, customerId);

    } catch (StripeException e) {
      throw new StripeApiClientException(
          "Failed to attach payment method: " + e.getMessage(), e);
    }
  }

  /**
   * Creates a Stripe SetupIntent for saving payment method details.
   *
   * @param idempotencyKey unique key to ensure idempotent creation
   * @param customerId     the Stripe customer ID to attach the method to
   * @return SetupIntentResponse containing the intent ID and client secret
   * @throws StripeApiClientException if setup intent creation fails
   */
  public SetupIntentResponse createSetupIntent(UUID idempotencyKey, String customerId) {
    log.info("Creating Stripe SetupIntent with idempotency key: {} for customer: {}", idempotencyKey, customerId);

    try {
      SetupIntentCreateParams params = SetupIntentCreateParams.builder()
          .setCustomer(customerId)
          .setAutomaticPaymentMethods(
              SetupIntentCreateParams.AutomaticPaymentMethods.builder()
                  .setEnabled(true)
                  .build())
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
          .setIdempotencyKey(idempotencyKey.toString() + "_account") // Must be unique from user's stripe id
          .build();

      Account account = Account.create(params, requestOptions);

      log.info("Successfully created Stripe Connected Account: {}", account.getId());
      return new ConnectedAccountResponse(account.getId());

    } catch (StripeException e) {
      throw new StripeApiClientException(
          "Failed to create Stripe Connected Account: " + e.getMessage(), e);
    }
  }

  /**
   * Creates an Account Link to onboard the user for Stripe Connect.
   *
   * @param accountId  the connected account ID
   * @param refreshUrl URL to redirect if the link expires
   * @param returnUrl  URL to redirect after successful onboarding
   * @return the account link URL
   * @throws StripeApiClientException if link creation fails
   */
  public String createAccountLink(String accountId, String refreshUrl, String returnUrl) {
    log.info("Creating Account Link for account: {}", accountId);

    try {
      AccountLinkCreateParams params = AccountLinkCreateParams.builder()
          .setAccount(accountId)
          .setRefreshUrl(refreshUrl)
          .setReturnUrl(returnUrl)
          .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
          .build();

      AccountLink accountLink = AccountLink.create(params);

      log.info("Successfully created Account Link for account: {}", accountId);
      return accountLink.getUrl();

    } catch (StripeException e) {
      throw new StripeApiClientException(
          "Failed to create Account Link: " + e.getMessage(), e);
    }
  }
}
