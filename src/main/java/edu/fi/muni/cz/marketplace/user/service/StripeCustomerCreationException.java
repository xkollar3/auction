package edu.fi.muni.cz.marketplace.user.service;

public class StripeCustomerCreationException extends RuntimeException {

  public StripeCustomerCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
