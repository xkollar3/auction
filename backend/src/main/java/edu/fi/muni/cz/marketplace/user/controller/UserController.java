package edu.fi.muni.cz.marketplace.user.controller;

import java.util.Optional;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.fi.muni.cz.marketplace.config.exception.HttpException;
import edu.fi.muni.cz.marketplace.user.command.CreateStripeCustomerCommand;
import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.dto.CreateStripeCustomerRequest;
import edu.fi.muni.cz.marketplace.user.dto.UserRegistrationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import edu.fi.muni.cz.marketplace.user.dto.Address;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final CommandGateway commandGateway;

  @PostMapping("/register")
  public ResponseEntity<UserRegistrationResponse> registerUser(@AuthenticationPrincipal Jwt jwt) {
    String keycloakUserId = Optional.ofNullable(jwt.getSubject())
        .orElseThrow(() -> new HttpException(401, "No subject in token"));

    log.info("Registering new user with Keycloak user ID from JWT: {}", keycloakUserId);

    UUID aggregateId = UUID.randomUUID();
    commandGateway.sendAndWait(new RegisterUserCommand(aggregateId, keycloakUserId));

    return ResponseEntity.status(HttpStatus.CREATED).body(new UserRegistrationResponse(aggregateId));
  }

  @PostMapping("/{id}/create-stripe-customer")
  public ResponseEntity<Void> createStripeCustomer(
      @PathVariable UUID id,
      @RequestBody CreateStripeCustomerRequest request,
      @AuthenticationPrincipal Jwt jwt) {

    validateJwtClaims(jwt, "email", "name", "phone_number");

    String email = jwt.getClaimAsString("email");
    String name = jwt.getClaimAsString("name");
    String phone = jwt.getClaimAsString("phone_number");

    log.info("Creating Stripe customer for user aggregate: {}", id);

    Address shippingAddress = new Address(
        request.line1(),
        request.line2(),
        request.city(),
        request.state(),
        request.postalCode(),
        request.country());

    commandGateway.send(new CreateStripeCustomerCommand(
        id,
        email,
        name,
        phone,
        shippingAddress));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
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
