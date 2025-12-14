package edu.fi.muni.cz.marketplace.user.service;

import edu.fi.muni.cz.marketplace.user.query.UserNicknameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NicknameSuggestionService {

  private static final Pattern DISCORD_PATTERN = Pattern.compile("^(.+)#(\\d{4})$");
  private static final int MAX_DISCRIMINATOR = 9999;

  private final UserNicknameRepository userNicknameRepository;

  public String suggestNickname(String proposedNickname) {
    String nickname = extractNickname(proposedNickname);

    log.info("Suggesting nickname for: {}", nickname);

    for (int discriminator = 0; discriminator <= MAX_DISCRIMINATOR; discriminator++) {
      if (!userNicknameRepository.existsByNicknameAndDiscriminator(nickname, discriminator)) {
        return String.format("%s#%04d", nickname, discriminator);
      }
    }

    throw new IllegalStateException("All discriminators exhausted for nickname: " + nickname);
  }

  private String extractNickname(String proposedNickname) {
    Matcher matcher = DISCORD_PATTERN.matcher(proposedNickname);

    if (matcher.matches()) {
      return matcher.group(1);
    }

    return proposedNickname;
  }
}
