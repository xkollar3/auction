package edu.fi.muni.cz.marketplace.order.controller;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.fi.muni.cz.marketplace.order.aggregate.TrackingStatusMilestone;
import edu.fi.muni.cz.marketplace.order.command.UpdateTrackingStatusCommand;
import edu.fi.muni.cz.marketplace.order.dto.Ship24WebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/orders")
@RequiredArgsConstructor
public class OrderWebHookController {

  private final CommandGateway commandGateway;

  @Value("${ship24.webhook-secret}")
  private String webhookSecret;

  @PostMapping("/ship24")
  public ResponseEntity<String> handleShip24Webhook(
      @RequestHeader("Authorization") String authHeader,
      @RequestBody Ship24WebhookPayload payload) {
    if (!isValidWebhookSecret(authHeader)) {
      log.warn("Invalid webhook secret received");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid webhook secret");
    }

    log.info("Received Ship24 webhook with {} tracking updates", payload.trackings().size());

    payload.trackings().forEach(trackingUpdate -> {
      try {
        UUID orderId = UUID.fromString(trackingUpdate.tracker().shipmentReference());

        trackingUpdate.events().forEach(event -> {
          TrackingStatusMilestone milestone = event.statusMilestone().toTrackingStatusMilestone();

          log.info("Processing tracking event {} for order {}: {} - {}",
              event.eventId(), orderId, milestone, event.status());

          commandGateway.send(new UpdateTrackingStatusCommand(
              orderId,
              event.eventId(),
              milestone,
              event.status(),
              event.occurrenceDatetime()));
        });
      } catch (IllegalArgumentException e) {
        log.error("Invalid shipmentReference (not a valid UUID): {}",
            trackingUpdate.tracker().shipmentReference());
      }
    });

    return ResponseEntity.ok("Webhook processed successfully");
  }

  private boolean isValidWebhookSecret(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return false;
    }

    final int BEARER_PREFIX_LENGTH = 7;
    String providedSecret = authHeader.substring(BEARER_PREFIX_LENGTH);
    return webhookSecret.equals(providedSecret);
  }
}
