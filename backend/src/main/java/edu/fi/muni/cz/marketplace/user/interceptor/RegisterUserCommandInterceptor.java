package edu.fi.muni.cz.marketplace.user.interceptor;

import java.util.List;
import java.util.function.BiFunction;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.config.exception.HttpException;
import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.persistence.KeycloakUserLookupRepository;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RegisterUserCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

  private final KeycloakUserLookupRepository keycloakUserLookupRepository;

  @Autowired
  public RegisterUserCommandInterceptor(KeycloakUserLookupRepository keycloakUserLookupRepository) {
    this.keycloakUserLookupRepository = keycloakUserLookupRepository;
  }

  @Override
  public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> list) {
    return (i, m) -> {
      if (RegisterUserCommand.class.equals(m.getPayloadType())) {
        final RegisterUserCommand command = (RegisterUserCommand) m.getPayload();
        if (keycloakUserLookupRepository.existsById(command.getKeycloakUserId())) {
          log.debug("User already registered");
          throw new HttpException(HttpStatus.CONFLICT.value(),
              "User with already exists: " + command.getKeycloakUserId());
        }
      }
      return m;
    };
  }
}
