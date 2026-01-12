package edu.fi.muni.cz.marketplace.user.controller;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.fi.muni.cz.marketplace.config.exception.HttpException;
import edu.fi.muni.cz.marketplace.user.command.AddPaymentInformationCommand;
import edu.fi.muni.cz.marketplace.user.command.CreateStripeConnectedAccountCommand;
import edu.fi.muni.cz.marketplace.user.command.CreateStripeCustomerCommand;
import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.command.RemoveUserCommand;
import edu.fi.muni.cz.marketplace.user.dto.AddPaymentMethodRequest;
import edu.fi.muni.cz.marketplace.user.dto.CreateStripeConnectedAccountResponse;
import edu.fi.muni.cz.marketplace.user.dto.CreateStripeCustomerRequest;
import edu.fi.muni.cz.marketplace.user.dto.UserRegistrationResponse;
import edu.fi.muni.cz.marketplace.user.query.FindUserByKeycloakIdQuery;
import edu.fi.muni.cz.marketplace.user.query.UserReadModel;
import edu.fi.muni.cz.marketplace.user.service.StripeApiClient;
import edu.fi.muni.cz.marketplace.user.service.dto.SetupIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import edu.fi.muni.cz.marketplace.user.dto.Address;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;
  private final StripeApiClient stripeApiClient;

  @PostMapping("/register")
  public ResponseEntity<UserRegistrationResponse> registerUser(@AuthenticationPrincipal Jwt jwt) {
    String keycloakUserId = Optional.ofNullable(jwt.getSubject())
        .orElseThrow(() -> new HttpException(401, "No subject in token"));

    log.info("Registering new user with Keycloak user ID from JWT: {}", keycloakUserId);

    UUID aggregateId = UUID.randomUUID();
    commandGateway.sendAndWait(new RegisterUserCommand(aggregateId, keycloakUserId));

    return ResponseEntity.status(HttpStatus.CREATED).body(new UserRegistrationResponse(aggregateId));
  }

  @PostMapping("/me/create-stripe-customer")
  public ResponseEntity<Void> createStripeCustomer(
      @RequestBody CreateStripeCustomerRequest request,
      @AuthenticationPrincipal Jwt jwt) {

    validateJwtClaims(jwt, "email", "name", "phone_number");

    String keycloakUserId = jwt.getSubject();
    UserReadModel user = findUserByKeycloakId(keycloakUserId);

    String email = jwt.getClaimAsString("email");
    String name = jwt.getClaimAsString("name");
    String phone = jwt.getClaimAsString("phone_number");

    log.info("Creating Stripe customer for user aggregate: {}", user.getId());

    Address shippingAddress = new Address(
        request.line1(),
        request.line2(),
        request.city(),
        request.state(),
        request.postalCode(),
        request.country());

    commandGateway.send(new CreateStripeCustomerCommand(
        user.getId(),
        email,
        name,
        phone,
        shippingAddress));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @PostMapping("/me/create-seller-account")
  public ResponseEntity<CreateStripeConnectedAccountResponse> createStripeConnectedAccount(
      @AuthenticationPrincipal Jwt jwt) {
    validateJwtClaims(jwt, "email");

    String keycloakUserId = jwt.getSubject();
    UserReadModel user = findUserByKeycloakId(keycloakUserId);
    String email = jwt.getClaimAsString("email");

    log.info("Creating Stripe seller account for user aggregate: {}", user.getId());

    CompletableFuture<String> future = commandGateway.send(new CreateStripeConnectedAccountCommand(
        user.getId(),
        email));

    // We wait for the result to get the onboarding URL
    String onboardingUrl = future.join();

    return ResponseEntity.status(HttpStatus.CREATED).body(new CreateStripeConnectedAccountResponse(onboardingUrl));
  }

  @PostMapping("/me/setup-payment-intent")
  public ResponseEntity<SetupIntentResponse> createSetupIntent(@AuthenticationPrincipal Jwt jwt) {
    String keycloakUserId = jwt.getSubject();
    UserReadModel user = findUserByKeycloakId(keycloakUserId);

    log.info("Creating SetupIntent for user aggregate: {}", user.getId());

    if (user.getStripeCustomerId() == null) {
      throw new HttpException(400, "User does not have a Stripe Customer ID");
    }

    // We use a random UUID for idempotency here because a user might try to setup
    // multiple payment methods
    SetupIntentResponse response = stripeApiClient.createSetupIntent(UUID.randomUUID(), user.getStripeCustomerId());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/me/payment-methods")
  public ResponseEntity<Void> addPaymentMethod(
      @RequestBody AddPaymentMethodRequest request,
      @AuthenticationPrincipal Jwt jwt) {

    String keycloakUserId = jwt.getSubject();
    UserReadModel user = findUserByKeycloakId(keycloakUserId);

    log.info("Adding payment method {} to user aggregate: {}", request.paymentMethodId(), user.getId());

    commandGateway.sendAndWait(new AddPaymentInformationCommand(
        user.getId(),
        request.paymentMethodId()));

    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/me")
  public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Jwt jwt) {
    String keycloakUserId = jwt.getSubject();
    UserReadModel user = findUserByKeycloakId(keycloakUserId);
    log.info("Deleting user with aggregate ID: {}", user.getId());
    commandGateway.sendAndWait(new RemoveUserCommand(user.getId()));
    return ResponseEntity.noContent().build();
  }

  private UserReadModel findUserByKeycloakId(String keycloakUserId) {
    CompletableFuture<UserReadModel> future = queryGateway.query(
        new FindUserByKeycloakIdQuery(keycloakUserId),
        ResponseTypes.instanceOf(UserReadModel.class));

    UserReadModel user = future.join();
    if (user == null) {
      throw new HttpException(404, "User not found");
    }
    return user;
  }

  private void validateJwtClaims(Jwt jwt, String... requiredClaims) {
    for (String claim : requiredClaims) {
      String value = jwt.getClaimAsString(claim);
      if (value == null || value.isBlank()) {
        log.warn("Missing or empty required claim '{}' in JWT", claim);
        throw new HttpException(401, "Missing required claim in token: " + claim);
      }
    }
  }
}
