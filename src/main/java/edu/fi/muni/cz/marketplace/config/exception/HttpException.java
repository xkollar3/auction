package edu.fi.muni.cz.marketplace.config.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class HttpException extends RuntimeException {

  private final HttpStatus status;

  public HttpException(Integer status, String message) {
    super(message);
    this.status = HttpStatus.valueOf(status);
  }
}
