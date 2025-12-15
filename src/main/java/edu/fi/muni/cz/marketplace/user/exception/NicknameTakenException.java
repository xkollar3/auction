package edu.fi.muni.cz.marketplace.user.exception;

import org.springframework.http.HttpStatus;

import edu.fi.muni.cz.marketplace.config.exception.HttpException;

public class NicknameTakenException extends HttpException {

  public NicknameTakenException(String message) {
    super(HttpStatus.CONFLICT.value(), message);
  }
}
