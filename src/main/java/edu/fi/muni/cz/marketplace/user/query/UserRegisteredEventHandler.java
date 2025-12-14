package edu.fi.muni.cz.marketplace.user.query;

import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@ProcessingGroup("user_nicknames")
public class UserRegisteredEventHandler {

  private final UserNicknameRepository userNicknameRepository;

  @EventHandler
  public void on(UserRegisteredEvent event) {
    UserNicknameReadModel readModel = UserNicknameReadModel.builder()
        .id(UUID.randomUUID())
        .nickname(event.getNickname().getNickname())
        .discriminator(event.getNickname().getDiscriminator())
        .build();

    userNicknameRepository.save(readModel);
  }
}
