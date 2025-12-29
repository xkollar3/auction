package edu.fi.muni.cz.marketplace.order.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import edu.fi.muni.cz.marketplace.order.command.AssignFundReservationCommand;
import edu.fi.muni.cz.marketplace.order.command.FinishRefundCommand;
import edu.fi.muni.cz.marketplace.order.events.FundsReservedEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderCancelledEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderRefundScheduledEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Aggregate
@NoArgsConstructor
public class Order {

  @AggregateIdentifier
  private UUID id;

  private OrderStatus status;

  private FundReservation fundReservation;

  private String refundId;

  @CommandHandler
  public Order(AssignFundReservationCommand command) {
    apply(new FundsReservedEvent(
        command.getOrderId(),
        command.getPaymentIntentId(),
        command.getPaymentMethodId(),
        command.getAmount(),
        command.getReservedAt()));
  }

  @EventSourcingHandler
  public void on(FundsReservedEvent event, @Autowired EventScheduler eventScheduler,
      @Autowired @Value("${policy.refund-deadline-days}") long refundPeriodDays) {
    this.id = event.getOrderId();
    this.status = OrderStatus.PAID;
    this.fundReservation = new FundReservation(
        event.getPaymentIntentId(),
        event.getPaymentMethodId(),
        event.getAmount(),
        event.getReservedAt());

    Instant scheduleTime = event.getReservedAt().plus(Duration.ofDays(refundPeriodDays));
    eventScheduler.schedule(scheduleTime,
        new OrderRefundScheduledEvent(event.getOrderId(), event.getPaymentIntentId()));
  }

  @EventSourcingHandler
  public void on(OrderRefundScheduledEvent event) {
    if (status != OrderStatus.PAID) {
      throw new IllegalStateException("Trying to refund an unpaid order: " + this.id);
    }
    this.status = OrderStatus.REFUND_PENDING;
  }

  @CommandHandler
  public void on(FinishRefundCommand command) {
    apply(new OrderCancelledEvent(command.getOrderId(), command.getRefundId(), Instant.now()));
  }

  @EventSourcingHandler
  public void on(OrderCancelledEvent event) {
    if (status != OrderStatus.REFUND_PENDING) {
      throw new IllegalStateException("Trying to cancel a unrefunded order: " + this.id);
    }

    this.status = OrderStatus.CANCELLED;
    this.refundId = event.getRefundId();
  }
}
