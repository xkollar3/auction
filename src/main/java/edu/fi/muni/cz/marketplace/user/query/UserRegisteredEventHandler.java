package edu.fi.muni.cz.marketplace.user.query;

import edu.fi.muni.cz.marketplace.user.event.UserRegistrationInitiatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ProcessingGroup("user_nicknames")
public class UserRegisteredEventHandler {

  private final UserNicknameRepository userNicknameRepository;

  @EventHandler
  public void on(UserRegistrationInitiatedEvent event) {
    UserNicknameReadModel readModel = new UserNicknameReadModel(UUID.randomUUID(), event.getNickname().getNickname(),
        event.getNickname().getDiscriminator());

    userNicknameRepository.save(readModel);
  }
}
