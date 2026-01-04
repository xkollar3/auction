package edu.fi.muni.cz.marketplace.order.service;

public class Ship24ServiceException extends RuntimeException {

  public Ship24ServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public Ship24ServiceException(String message) {
    super(message);
  }
}
