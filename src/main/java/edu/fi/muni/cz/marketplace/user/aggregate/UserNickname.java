package edu.fi.muni.cz.marketplace.user.aggregate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

@Getter
public class UserNickname {

  private static final Pattern NICKNAME_PATTERN = Pattern.compile("^(.+)\\+(\\d{4})$");

  private final String nickname;

  private final Integer discriminator;

  public UserNickname(String fullNickname) {
    Matcher matcher = NICKNAME_PATTERN.matcher(fullNickname);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid nickname format: " + fullNickname);
    }

    this.nickname = matcher.group(1);
    this.discriminator = Integer.parseInt(matcher.group(2));
  }

  public String toFullString() {
    return nickname + "+" + String.format("%04d", discriminator);
  }
}
