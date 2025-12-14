package edu.fi.muni.cz.marketplace.user.query;

import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UserEventHandler {

  private static final Pattern DISCORD_PATTERN = Pattern.compile("^(.+)#(\\d{4})$");

  private final UserNicknameRepository userNicknameRepository;

  @EventHandler
  public void on(UserRegisteredEvent event) {
    Matcher matcher = DISCORD_PATTERN.matcher(event.getUserId());

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid userId format: " + event.getUserId());
    }

    String nickname = matcher.group(1);
    Integer discriminator = Integer.parseInt(matcher.group(2));

    UserNicknameReadModel readModel = UserNicknameReadModel.builder()
        .id(event.getUserId())
        .nickname(nickname)
        .discriminator(discriminator)
        .build();

    userNicknameRepository.save(readModel);
  }
}
