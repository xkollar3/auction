package edu.fi.muni.cz.marketplace.user.exception;

public class NicknameTakenException extends RuntimeException {

  public NicknameTakenException(String name) {
    super("Nickname already taken: " + name);
  }
}
