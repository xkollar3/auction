package edu.fi.muni.cz.marketplace.order.controller;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.fi.muni.cz.marketplace.order.command.ReserveFundsCommand;
import edu.fi.muni.cz.marketplace.order.dto.ReserveFundsRequest;
import edu.fi.muni.cz.marketplace.order.dto.ReserveFundsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final CommandGateway commandGateway;

  @PostMapping("/reserve-funds")
  public ResponseEntity<ReserveFundsResponse> reserveFunds(@RequestBody ReserveFundsRequest request) {
    UUID orderId = UUID.randomUUID();

    log.info("Reserving funds for new order: {}", orderId);

    commandGateway.sendAndWait(new ReserveFundsCommand(
        orderId,
        request.customerId(),
        request.paymentMethodId(),
        request.amount()));

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ReserveFundsResponse(orderId));
  }
}
