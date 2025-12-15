package edu.fi.muni.cz.marketplace.user.controller;

import edu.fi.muni.cz.marketplace.config.exception.ErrorResponse;
import edu.fi.muni.cz.marketplace.user.aggregate.RegistrationStatus;
import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.dto.RegisterUserRequest;
import edu.fi.muni.cz.marketplace.user.dto.UserRegistrationResponse;
import edu.fi.muni.cz.marketplace.user.query.FindUserRegistrationStatusQuery;
import edu.fi.muni.cz.marketplace.user.query.UserRegistrationStatusReadModel;
import edu.fi.muni.cz.marketplace.user.service.NicknameSuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRegistrationController {

  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;
  private final NicknameSuggestionService nicknameSuggestionService;

  @GetMapping("/suggest-userid")
  public ResponseEntity<String> suggestUserId(@RequestParam String proposedUserId) {
    String suggestedUserId = nicknameSuggestionService.suggestNickname(proposedUserId);
    return ResponseEntity.ok(suggestedUserId);
  }

  @PostMapping("/register")
  public ResponseEntity<UserRegistrationResponse> registerUser(@RequestBody RegisterUserRequest request) {
    log.info("Registering new user");
    UUID aggregateId = UUID.randomUUID();

    RegisterUserCommand command = new RegisterUserCommand(
        aggregateId,
        request.nickname(),
        request.firstName(),
        request.lastName(),
        request.email(),
        request.phoneNumber(),
        request.password());

    FindUserRegistrationStatusQuery query = new FindUserRegistrationStatusQuery(aggregateId);

    try (
        SubscriptionQueryResult<UserRegistrationStatusReadModel, UserRegistrationStatusReadModel> subscriptionQuery = queryGateway
            .subscriptionQuery(
                query,
                ResponseTypes.instanceOf(UserRegistrationStatusReadModel.class),
                ResponseTypes.instanceOf(UserRegistrationStatusReadModel.class))) {

      commandGateway.sendAndWait(command);

      UserRegistrationStatusReadModel result = subscriptionQuery.updates()
          .blockFirst(Duration.ofSeconds(30));

      if (result == null) {
        log.error("Registration timed out for user {}", aggregateId);
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
      }

      if (result.getStatus() == RegistrationStatus.FAILED) {
        throw new ResponseStatusException(HttpStatus.valueOf(result.getHttpStatus()), result.getErrorMessage());
      }

      UserRegistrationResponse response = new UserRegistrationResponse(
          result.getId(),
          result.getNickname(),
          result.getKeycloakUserId(),
          result.getStatus(),
          result.getErrorMessage());

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
  }
}
