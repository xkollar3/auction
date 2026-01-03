package edu.fi.muni.cz.marketplace.order.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import edu.fi.muni.cz.marketplace.order.command.AssignFundReservationCommand;
import edu.fi.muni.cz.marketplace.order.command.AssignTrackingInfoCommand;
import edu.fi.muni.cz.marketplace.order.command.CompleteOrderCommand;
import edu.fi.muni.cz.marketplace.order.command.EnterTrackingNumberCommand;
import edu.fi.muni.cz.marketplace.order.command.FinishRefundCommand;
import edu.fi.muni.cz.marketplace.order.command.UpdateTrackingStatusCommand;
import edu.fi.muni.cz.marketplace.order.deadline.ShippingDeadline;
import edu.fi.muni.cz.marketplace.order.deadline.ShippingDeadlineNotMetPayload;
import edu.fi.muni.cz.marketplace.order.events.FundsReservedEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderCancelledEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderCompletedEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderDeliveredEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderRefundScheduledEvent;
import edu.fi.muni.cz.marketplace.order.events.TrackingNumberEnteredEvent;
import edu.fi.muni.cz.marketplace.order.events.TrackingNumberProvidedEvent;
import edu.fi.muni.cz.marketplace.order.events.TrackingStatusUpdatedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Aggregate
@NoArgsConstructor
public class Order {

  @Nonnull
  @AggregateIdentifier
  private UUID id;

  @Nonnull
  private OrderStatus status;

  @Nonnull
  private BigDecimal commissionMultiplier;

  @Nonnull
  private FundReservation fundReservation;

  @Nullable
  private TrackingInfo trackingInfo;

  @Nullable
  private OrderCompletionInfo completionInfo;

  @Nullable
  private String refundId;

  @CommandHandler
  public Order(AssignFundReservationCommand command,
      @Autowired DeadlineManager deadlineManager,
      @Autowired Clock clock,
      @Autowired @Value("${policy.refund-deadline-days}") Long refundPeriodDays) {
    Instant shippingDeadlineTime = clock.instant().plus(refundPeriodDays,
        ChronoUnit.DAYS);
    String deadlineId = deadlineManager.schedule(shippingDeadlineTime,
        ShippingDeadline.SHIPING_DEADLINE_NOT_MET,
        new ShippingDeadlineNotMetPayload(command.getOrderId(), command.getPaymentIntentId()));

    apply(new FundsReservedEvent(
        command.getOrderId(),
        command.getPaymentIntentId(),
        command.getPaymentMethodId(),
        deadlineId,
        command.getNetAmount(),
        command.getReservedAt(),
        command.getSellerId(),
        command.getSellerStripeAccountId()));
  }

  @EventSourcingHandler
  public void on(FundsReservedEvent event,
      @Autowired @Value("${policy.commission-percentage}") String commissionPercentage) {
    this.id = event.getOrderId();
    this.status = OrderStatus.FUNDS_RESERVED;
    this.commissionMultiplier = new BigDecimal(commissionPercentage);
    this.fundReservation = new FundReservation(
        event.getPaymentIntentId(),
        event.getPaymentMethodId(),
        event.getDeadlineId(),
        event.getAmount(),
        event.getReservedAt(),
        event.getSellerId(),
        event.getSellerAccountId());
  }

  @DeadlineHandler(deadlineName = ShippingDeadline.SHIPING_DEADLINE_NOT_MET)
  public void onShippingDeadlineNotMet(ShippingDeadlineNotMetPayload payload) {
    if (status != OrderStatus.FUNDS_RESERVED) {
      // deadlines run in a separate thread they should not interrupt it
      log.warn("Deadline fired but order {} already in state {}. Ignoring.", this.id, this.status);
      return;
    }

    log.info("Deadline for shipping order: {} was not met. Cancelling order.", payload.getOrderId());
    apply(new OrderRefundScheduledEvent(payload.getOrderId(), payload.getPaymentIntentId()));
  }

  @EventSourcingHandler
  public void on(OrderRefundScheduledEvent event) {
    this.status = OrderStatus.REFUND_PENDING;
  }

  @CommandHandler
  public void on(FinishRefundCommand command) {
    if (status != OrderStatus.REFUND_PENDING) {
      throw new IllegalStateException("Trying to cancel a unrefunded order: " + this.id);
    }

    apply(new OrderCancelledEvent(command.getOrderId(), command.getRefundId(), Instant.now()));
  }

  @EventSourcingHandler
  public void on(OrderCancelledEvent event) {
    this.status = OrderStatus.CANCELLED;
    this.refundId = event.getRefundId();
  }

  @CommandHandler
  public void on(EnterTrackingNumberCommand command, @Autowired DeadlineManager deadlineManager) {
    if (status != OrderStatus.FUNDS_RESERVED) {
      throw new IllegalStateException(
          "Cannot enter tracking number for order " + this.id + " in state: " + status);
    }

    log.info("Cancelling order deadline, the tracking info is now provided: " + this.id);
    deadlineManager.cancelSchedule(ShippingDeadline.SHIPING_DEADLINE_NOT_MET, this.fundReservation.getDeadlineId());

    apply(new TrackingNumberProvidedEvent(command.getOrderId(), command.getTrackingNumber()));
  }

  @EventSourcingHandler
  public void on(TrackingNumberProvidedEvent event) {
    this.status = OrderStatus.TRACKING_NUMBER_PROVIDED;
  }

  @CommandHandler
  public void on(AssignTrackingInfoCommand command) {
    if (status != OrderStatus.TRACKING_NUMBER_PROVIDED) {
      throw new IllegalStateException(
          "Cannot assign tracking info for order " + this.id + " in state: " + status);
    }

    apply(new TrackingNumberEnteredEvent(
        command.getOrderId(),
        command.getTrackingNumber(),
        command.getShip24TrackerId(),
        command.getEnteredAt()));
  }

  @EventSourcingHandler
  public void on(TrackingNumberEnteredEvent event) {
    this.status = OrderStatus.TRACKING_IN_PROGRESS;
    this.trackingInfo = new TrackingInfo(
        event.getTrackingNumber(),
        event.getShip24TrackerId(),
        event.getEnteredAt(),
        TrackingStatusMilestone.PENDING,
        null,
        event.getEnteredAt());
  }

  @CommandHandler
  public void on(UpdateTrackingStatusCommand command) {
    if (status != OrderStatus.TRACKING_IN_PROGRESS) {
      throw new IllegalStateException(
          "Cannot update tracking status for order " + this.id + " in state: " + status);
    }

    apply(new TrackingStatusUpdatedEvent(
        command.getOrderId(),
        command.getEventId(),
        command.getStatusMilestone(),
        command.getEventStatus(),
        command.getEventOccurredAt()));

    if (command.getStatusMilestone() == TrackingStatusMilestone.EXCEPTION) {
      apply(new OrderRefundScheduledEvent(
          command.getOrderId(),
          this.fundReservation.getPaymentIntentId()));
    }

    if (command.getStatusMilestone() == TrackingStatusMilestone.DELIVERED) {
      apply(new OrderDeliveredEvent(
          command.getOrderId(),
          this.getFundReservation().getSellerStripeAccountId(),
          this.payout(),
          this.commission(),
          command.getEventOccurredAt()));
    }
  }

  @EventSourcingHandler
  public void on(TrackingStatusUpdatedEvent event) {
    this.trackingInfo = new TrackingInfo(
        this.trackingInfo.getTrackingNumber(),
        this.trackingInfo.getShip24TrackerId(),
        this.trackingInfo.getCreatedAt(),
        event.getStatusMilestone(),
        event.getEventStatus(),
        event.getEventOccurredAt());
  }

  @EventSourcingHandler
  public void on(OrderDeliveredEvent event) {
    this.status = OrderStatus.DELIVERED;
  }

  @CommandHandler
  public void on(CompleteOrderCommand command) {
    if (status != OrderStatus.DELIVERED) {
      throw new IllegalStateException("Cannot complete an order id: " + this.id + " in state: " + status);
    }

    apply(new OrderCompletedEvent(command.getPaymentTransferId(), command.getCommssionTransferId(), Instant.now()));
  }

  @EventSourcingHandler
  public void on(OrderCompletedEvent event) {
    this.status = OrderStatus.COMPLETED;
    this.completionInfo = new OrderCompletionInfo(
        event.getCompletedAt(), event.getPayoutTransferId(), event.getCommissionTransferId());
  }

  private BigDecimal commission() {
    return this.fundReservation.getNetAmount().multiply(this.commissionMultiplier);
  }

  private BigDecimal payout() {
    return this.fundReservation.getNetAmount().multiply(BigDecimal.ONE.subtract(this.commissionMultiplier));
  }
}
