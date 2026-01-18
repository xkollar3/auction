package edu.fi.muni.cz.marketplace.order.query;

import edu.fi.muni.cz.marketplace.order.aggregate.OrderStatus;
import edu.fi.muni.cz.marketplace.order.events.FundsReservedEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderCancelledEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderCompletedEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderDeliveredEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderRefundScheduledEvent;
import edu.fi.muni.cz.marketplace.order.events.TrackingNumberAssignedToOrderEvent;
import edu.fi.muni.cz.marketplace.order.events.TrackingStatusUpdatedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ProcessingGroup("order_read_model")
@RequiredArgsConstructor
public class OrderProjection {

  private final OrderReadModelRepository repository;

  @EventHandler
  public void on(FundsReservedEvent event) {
    log.info("Processing FundsReservedEvent for order ID: {}", event.getOrderId());

    OrderReadModel readModel = new OrderReadModel(
        event.getOrderId(),
        event.getBuyerId(),
        OrderStatus.FUNDS_RESERVED,
        event.getPaymentIntentId(),
        event.getGrossAmount(),
        event.getReservedAt(),
        event.getSellerId(),
        event.getSellerStripeAccountId(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null);

    repository.save(readModel);
    log.info("Created order read model for order ID: {}", event.getOrderId());
  }

  @EventHandler
  public void on(TrackingNumberAssignedToOrderEvent event) {
    log.info("Processing TrackingNumberEnteredEvent for order ID: {}", event.getOrderId());

    repository.findById(event.getOrderId()).ifPresent(order -> {
      order.setStatus(OrderStatus.TRACKING_IN_PROGRESS);
      order.setTrackingNumber(event.getTrackingNumber());
      order.setShip24TrackerId(event.getShip24TrackerId());
      order.setTrackingEnteredAt(event.getEnteredAt());
      repository.save(order);
      log.info("Updated tracking info for order ID: {}", event.getOrderId());
    });
  }

  @EventHandler
  public void on(TrackingStatusUpdatedEvent event) {
    log.info("Processing TrackingStatusUpdatedEvent for order ID: {}", event.getOrderId());

    repository.findById(event.getOrderId()).ifPresent(order -> {
      order.setTrackingStatusMilestone(event.getStatusMilestone());
      order.setTrackingEventStatus(event.getEventStatus());
      order.setTrackingLastUpdatedAt(event.getEventOccurredAt());
      repository.save(order);
      log.info("Updated tracking status to {} for order ID: {}", event.getStatusMilestone(),
          event.getOrderId());
    });
  }

  @EventHandler
  public void on(OrderDeliveredEvent event) {
    log.info("Processing OrderDeliveredEvent for order ID: {}", event.getOrderId());

    repository.findById(event.getOrderId()).ifPresent(order -> {
      order.setStatus(OrderStatus.DELIVERED);
      repository.save(order);
      log.info("Updated order status to DELIVERED for order ID: {}", event.getOrderId());
    });
  }

  @EventHandler
  public void on(OrderRefundScheduledEvent event) {
    log.info("Processing OrderRefundScheduledEvent for order ID: {}", event.getOrderId());

    repository.findById(event.getOrderId()).ifPresent(order -> {
      order.setStatus(OrderStatus.REFUND_PENDING);
      repository.save(order);
      log.info("Updated order status to REFUND_PENDING for order ID: {}", event.getOrderId());
    });
  }

  @EventHandler
  public void on(OrderCancelledEvent event) {
    log.info("Processing OrderCancelledEvent for order ID: {}", event.getOrderId());

    repository.findById(event.getOrderId()).ifPresent(order -> {
      order.setStatus(OrderStatus.CANCELLED);
      order.setCancelledAt(event.getCancelledAt());
      order.setRefundId(event.getRefundId());
      repository.save(order);
      log.info("Updated order status to CANCELLED for order ID: {}", event.getOrderId());
    });
  }

  @EventHandler
  public void on(OrderCompletedEvent event) {
    log.info("Processing OrderCompletedEvent for order ID: {}", event.getOrderId());

    repository.findById(event.getOrderId()).ifPresent(order -> {
      order.setStatus(OrderStatus.COMPLETED);
      order.setCompletedAt(event.getCompletedAt());
      repository.save(order);
      log.info("Updated order status to COMPLETED for order ID: {}", event.getOrderId());
    });
  }

  @QueryHandler
  public List<OrderReadModel> handle(FindOrdersBySellerIdQuery query) {
    log.debug("Retrieving orders for seller ID: {}", query.getSellerId());
    return repository.findBySellerIdOrderByReservedAtDesc(query.getSellerId());
  }

  @QueryHandler
  public List<OrderReadModel> handle(FindOrdersByBuyerIdQuery query) {
    log.debug("Retrieving orders for buyer ID: {}", query.getBuyerId());
    return repository.findByBuyerIdOrderByReservedAtDesc(query.getBuyerId());
  }
}
