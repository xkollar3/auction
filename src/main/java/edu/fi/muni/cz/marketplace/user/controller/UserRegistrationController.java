package edu.fi.muni.cz.marketplace.user.controller;

import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.dto.RegisterUserRequest;
import edu.fi.muni.cz.marketplace.user.service.UserIdSuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRegistrationController {

  private final CommandGateway commandGateway;
  private final UserIdSuggestionService userIdSuggestionService;

  @GetMapping("/suggest-userid")
  public ResponseEntity<String> suggestUserId(@RequestParam String proposedUserId) {
    String suggestedUserId = userIdSuggestionService.suggestUserId(proposedUserId);
    return ResponseEntity.ok(suggestedUserId);
  }

  @PostMapping("/register")
  public ResponseEntity<String> registerUser(@RequestBody RegisterUserRequest request) {
    log.info("Registering new user");
    String aggregateId = UUID.randomUUID().toString();

    RegisterUserCommand command = RegisterUserCommand.builder()
        .userId(request.userId())
        .firstName(request.firstName())
        .lastName(request.lastName())
        .email(request.email())
        .phoneNumber(request.phoneNumber())
        .build();

    commandGateway.sendAndWait(command);

    return ResponseEntity.status(HttpStatus.CREATED).body(aggregateId);
  }
}
