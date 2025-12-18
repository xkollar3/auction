package edu.fi.muni.cz.marketplace.user.command;

import java.util.List;
import java.util.function.BiFunction;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.config.exception.HttpException;
import edu.fi.muni.cz.marketplace.user.query.KeycloakUserIdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUserCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

  private final KeycloakUserIdRepository keycloakUserIdRepository;

  @Override
  public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
      List<? extends CommandMessage<?>> messages) {

    return (index, command) -> {
      if (command.getPayload() instanceof RegisterUserCommand registerCommand) {
        log.info("Intercepting RegisterUserCommand for Keycloak user ID: {}",
            registerCommand.getKeycloakUserId());

        if (keycloakUserIdRepository.existsByKeycloakUserId(registerCommand.getKeycloakUserId())) {
          log.warn("Duplicate Keycloak user ID detected: {}", registerCommand.getKeycloakUserId());
          throw new HttpException(409,
              String.format("User with Keycloak user ID '%s' already exists",
                  registerCommand.getKeycloakUserId()));
        }

        log.info("Keycloak user ID validation passed: {}", registerCommand.getKeycloakUserId());
      }

      return command;
    };
  }
}
