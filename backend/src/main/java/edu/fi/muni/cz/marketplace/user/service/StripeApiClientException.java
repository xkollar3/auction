package edu.fi.muni.cz.marketplace.user.service;

public class StripeApiClientException extends RuntimeException {

  public StripeApiClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
