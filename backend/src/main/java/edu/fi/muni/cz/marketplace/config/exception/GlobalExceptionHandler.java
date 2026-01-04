package edu.fi.muni.cz.marketplace.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(HttpException.class)
  public ResponseEntity<ErrorResponse> handleHttpException(HttpException ex) {
    return ResponseEntity
        .status(ex.getStatus())
        .body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
    return ResponseEntity
        .status(e.getStatusCode())
        .body(new ErrorResponse(e.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("An unexpected error occurred"));
  }
}
