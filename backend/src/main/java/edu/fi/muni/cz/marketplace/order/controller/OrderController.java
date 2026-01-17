package edu.fi.muni.cz.marketplace.order.controller;

import edu.fi.muni.cz.marketplace.config.exception.HttpException;
import edu.fi.muni.cz.marketplace.order.command.EnterTrackingNumberCommand;
import edu.fi.muni.cz.marketplace.order.dto.EnterTrackingNumberRequest;
import edu.fi.muni.cz.marketplace.order.dto.EnterTrackingNumberResponse;
import edu.fi.muni.cz.marketplace.order.dto.OrderResponse;
import edu.fi.muni.cz.marketplace.order.query.FindOrdersByBuyerIdQuery;
import edu.fi.muni.cz.marketplace.order.query.FindOrdersBySellerIdQuery;
import edu.fi.muni.cz.marketplace.order.query.FindUserIdByKeycloakIdQuery;
import edu.fi.muni.cz.marketplace.order.query.OrderReadModel;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;

  @PostMapping("/{orderId}/tracking-number")
  public ResponseEntity<EnterTrackingNumberResponse> trackingNumber(
      @PathVariable UUID orderId,
      @RequestBody EnterTrackingNumberRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    String keycloakUserId = jwt.getSubject();

    commandGateway.sendAndWait(new EnterTrackingNumberCommand(
        orderId,
        UUID.fromString(keycloakUserId),
        request.trackingNumber()));

    return ResponseEntity.ok(new EnterTrackingNumberResponse(orderId));
  }

  @GetMapping("/my-sales")
  public ResponseEntity<List<OrderResponse>> getMySales(@AuthenticationPrincipal Jwt jwt) {
    String keycloakUserId = jwt.getSubject();
    log.info("Fetching sales for Keycloak user ID: {}", keycloakUserId);

    List<OrderReadModel> orders = queryGateway.query(
        new FindOrdersBySellerIdQuery(UUID.fromString(keycloakUserId)),
        ResponseTypes.multipleInstancesOf(OrderReadModel.class)).join();

    return ResponseEntity.ok(mapToOrderResponses(orders));
  }

  @GetMapping("/my-purchases")
  public ResponseEntity<List<OrderResponse>> getMyPurchases(@AuthenticationPrincipal Jwt jwt) {
    String keycloakUserId = jwt.getSubject();
    log.info("Fetching purchases for Keycloak user ID: {}", keycloakUserId);

    List<OrderReadModel> orders = queryGateway.query(
        new FindOrdersByBuyerIdQuery(UUID.fromString(keycloakUserId)),
        ResponseTypes.multipleInstancesOf(OrderReadModel.class)).join();

    return ResponseEntity.ok(mapToOrderResponses(orders));
  }

  private List<OrderResponse> mapToOrderResponses(List<OrderReadModel> orders) {
    return orders.stream()
        .map(order -> new OrderResponse(
            order.getId(),
            order.getBuyerId(),
            order.getSellerId(),
            order.getStatus(),
            order.getAmount(),
            order.getReservedAt(),
            order.getTrackingNumber(),
            order.getTrackingStatusMilestone(),
            order.getTrackingEventStatus(),
            order.getTrackingLastUpdatedAt(),
            order.getCompletedAt(),
            order.getCancelledAt()))
        .toList();
  }
}
