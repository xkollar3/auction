package edu.fi.muni.cz.marketplace.user.controller;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.fi.muni.cz.marketplace.user.aggregate.Address;
import edu.fi.muni.cz.marketplace.user.command.CreateStripeCustomerCommand;
import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.dto.CreateStripeCustomerRequest;
import edu.fi.muni.cz.marketplace.user.dto.UserRegistrationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final CommandGateway commandGateway;

  @PostMapping("/register")
  public ResponseEntity<UserRegistrationResponse> registerUser(@AuthenticationPrincipal Jwt jwt) {
    String keycloakUserId = jwt.getSubject();
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
        request.country()
    );

    commandGateway.sendAndWait(new CreateStripeCustomerCommand(
        id,
        email,
        name,
        phone,
        shippingAddress
    ));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
