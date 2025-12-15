package edu.fi.muni.cz.marketplace.user.exception;

import edu.fi.muni.cz.marketplace.config.exception.HttpException;
import lombok.Getter;

public class KeycloakRegistrationFailedException extends HttpException {

  public KeycloakRegistrationFailedException(Integer status, String message) {
    super(status, message);
  }
}
