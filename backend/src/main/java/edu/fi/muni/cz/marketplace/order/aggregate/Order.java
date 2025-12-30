package edu.fi.muni.cz.marketplace.order.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

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
import edu.fi.muni.cz.marketplace.order.command.FinishRefundCommand;
import edu.fi.muni.cz.marketplace.order.deadline.ShippingDeadline;
import edu.fi.muni.cz.marketplace.order.deadline.ShippingDeadlineNotMetPayload;
import edu.fi.muni.cz.marketplace.order.events.FundsReservedEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderCancelledEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderRefundScheduledEvent;
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
  private FundReservation fundReservation;

  @Nullable
  private String refundId;

  @CommandHandler
  public Order(AssignFundReservationCommand command,
      @Autowired DeadlineManager deadlineManager,
      @Autowired @Value("${policy.refund-deadline-days}") long refundPeriodDays) {
    Instant shippingDeadlineTime = Instant.now().plus(refundPeriodDays,
        ChronoUnit.DAYS);
    String deadlineId = deadlineManager.schedule(shippingDeadlineTime,
        ShippingDeadline.SHIPING_DEADLINE_NOT_MET,
        new ShippingDeadlineNotMetPayload(command.getOrderId(), command.getPaymentIntentId()));

    apply(new FundsReservedEvent(
        command.getOrderId(),
        command.getPaymentIntentId(),
        command.getPaymentMethodId(),
        deadlineId,
        command.getAmount(),
        command.getReservedAt()));
  }

  @EventSourcingHandler
  public void on(FundsReservedEvent event) {
    this.id = event.getOrderId();
    this.status = OrderStatus.PAID;
    this.fundReservation = new FundReservation(
        event.getPaymentIntentId(),
        event.getPaymentMethodId(),
        event.getDeadlineId(),
        event.getAmount(),
        event.getReservedAt());
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

  @DeadlineHandler(deadlineName = ShippingDeadline.SHIPING_DEADLINE_NOT_MET)
  public void onShippingDeadlineNotMet(ShippingDeadlineNotMetPayload payload) {
    if (status != OrderStatus.PAID) {
      throw new IllegalStateException("Trying to cancel an unpaid order: " + this.id);
    }

    log.info("Deadline for shipping order: {} was not met. Cancelling order.", payload.getOrderId());
    apply(new OrderRefundScheduledEvent(payload.getOrderId(), payload.getPaymentIntentId()));
  }
}
