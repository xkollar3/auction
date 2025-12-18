package edu.fi.muni.cz.marketplace.user.controller;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.dto.RegisterUserRequest;
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
  public ResponseEntity<UserRegistrationResponse> registerUser(@RequestBody RegisterUserRequest request) {
    log.info("Registering new user");
    UUID aggregateId = UUID.randomUUID();
    commandGateway.sendAndWait(new RegisterUserCommand(aggregateId, request.keycloakUserId(), request.phoneNumber()));

    return ResponseEntity.status(HttpStatus.CREATED).body(new UserRegistrationResponse(aggregateId));
  }
}
