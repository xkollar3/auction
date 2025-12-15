package edu.fi.muni.cz.marketplace.user.command;

import java.util.List;
import java.util.function.BiFunction;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.user.aggregate.UserNickname;
import edu.fi.muni.cz.marketplace.user.exception.NicknameTakenException;
import edu.fi.muni.cz.marketplace.user.query.UserNicknameRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRegistrationDispatchInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

  private final UserNicknameRepository repository;

  @Override
  public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> messages) {
    return (_, m) -> {
      if (RegisterUserCommand.class.equals(m.getPayloadType())) {
        final RegisterUserCommand command = (RegisterUserCommand) m.getPayload();
        UserNickname nickname = new UserNickname(command.getNickname());
        if (repository.existsByNicknameAndDiscriminator(nickname.getNickname(), nickname.getDiscriminator())) {
          throw new NicknameTakenException("Nickname already taken: " + nickname.toFullString());
        }

      }

      return m;
    };
  }
}
