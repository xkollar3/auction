package edu.fi.muni.cz.marketplace.order.aggregate;

import static org.axonframework.test.matchers.Matchers.exactSequenceOf;
import static org.axonframework.test.matchers.Matchers.messageWithPayload;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.fi.muni.cz.marketplace.order.command.AssignFundReservationCommand;
import edu.fi.muni.cz.marketplace.order.command.AssignTrackingInfoCommand;
import edu.fi.muni.cz.marketplace.order.command.EnterTrackingNumberCommand;
import edu.fi.muni.cz.marketplace.order.command.CompleteOrderCommand;
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

class OrderTest {

  private static final Long REFUND_DEADLINE_DAYS = 7L;
  private static final Instant FIXED_TIME = Instant.parse("2026-01-10T12:00:00Z");
  private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);

  private FixtureConfiguration<Order> fixture;

  @BeforeEach
  void setUp() {
    fixture = new AggregateTestFixture<>(Order.class);
    fixture.registerInjectableResource(REFUND_DEADLINE_DAYS);
    fixture.registerInjectableResource(FIXED_CLOCK);
  }

  @Test
  void assignFundReservation_aggregateNotInitialized_shouldEmitEventScheduleDeadlineAndSetState() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = Instant.now();
    String sellerId = "seller123";
    String sellerStripeAccountId = "acct_seller123";

    fixture.givenCurrentTime(FIXED_TIME)
        .when(new AssignFundReservationCommand(
            orderId,
            paymentIntentId,
            paymentMethodId,
            amount,
            reservedAt,
            sellerId,
            sellerStripeAccountId))
        .expectSuccessfulHandlerExecution()
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(FundsReservedEvent.class))))
        .expectScheduledDeadline(
            Duration.ofDays(REFUND_DEADLINE_DAYS),
            new ShippingDeadlineNotMetPayload(orderId, paymentIntentId))
        .expectState(order -> {
          assertEquals(orderId, order.getId());
          assertEquals(OrderStatus.FUNDS_RESERVED, order.getStatus());

          FundReservation reservation = order.getFundReservation();
          assertNotNull(reservation);
          assertEquals(paymentIntentId, reservation.getPaymentIntentId());
          assertEquals(paymentMethodId, reservation.getPaymentMethodId());
          assertEquals(amount, reservation.getNetAmount());
          assertEquals(reservedAt, reservation.getReservedAt());
          assertEquals(sellerId, reservation.getSellerId());
          assertEquals(sellerStripeAccountId, reservation.getSellerStripeAccountId());
          assertNotNull(reservation.getDeadlineId(), "Deadline ID should be stored in reservation");
        });
  }

  @Test
  void shippingDeadline_orderInFundsReservedState_shouldScheduleRefundAndUpdateState() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerStripeAccountId = "acct_seller123";

    fixture.givenCurrentTime(FIXED_TIME)
        .andGivenCommands(new AssignFundReservationCommand(
            orderId,
            paymentIntentId,
            paymentMethodId,
            amount,
            reservedAt,
            sellerId,
            sellerStripeAccountId))
        .whenTimeElapses(Duration.ofDays(REFUND_DEADLINE_DAYS))
        .expectTriggeredDeadlinesWithName(ShippingDeadline.SHIPING_DEADLINE_NOT_MET)
        .expectEvents(new OrderRefundScheduledEvent(orderId, paymentIntentId))
        .expectState(order -> {
          assertEquals(orderId, order.getId());
          assertEquals(OrderStatus.REFUND_PENDING, order.getStatus());
        });
  }

  @Test
  void finishRefund_orderInRefundPendingState_shouldCancelOrderAndEmitEvent() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    String deadlineId = "deadline-123";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerAccountId = "acct_seller123";
    String refundId = "refund-456";

    fixture.given(
        new FundsReservedEvent(
            orderId,
            paymentIntentId,
            paymentMethodId,
            deadlineId,
            amount,
            reservedAt,
            sellerId,
            sellerAccountId),
        new OrderRefundScheduledEvent(orderId, paymentIntentId))
        .when(new FinishRefundCommand(orderId, refundId))
        .expectSuccessfulHandlerExecution()
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(OrderCancelledEvent.class))))
        .expectState(order -> {
          assertEquals(orderId, order.getId());
          assertEquals(OrderStatus.CANCELLED, order.getStatus());
          assertEquals(refundId, order.getRefundId());
        });
  }

  @Test
  void shippingDeadline_orderStateAlreadyChanged_shouldRejectAndNotScheduleRefund() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerStripeAccountId = "acct_seller123";
    String trackingNumber = "TRACK123";

    fixture.givenCurrentTime(FIXED_TIME)
        .andGivenCommands(new AssignFundReservationCommand(
            orderId,
            paymentIntentId,
            paymentMethodId,
            amount,
            reservedAt,
            sellerId,
            sellerStripeAccountId))
        .andGiven(new TrackingNumberProvidedEvent(orderId, trackingNumber))
        .whenTimeElapses(Duration.ofDays(REFUND_DEADLINE_DAYS))
        .expectSuccessfulHandlerExecution()
        .expectNoEvents()
        .expectState(order -> {
          assertEquals(OrderStatus.TRACKING_NUMBER_PROVIDED, order.getStatus());
        });
  }

  @Test
  void finishRefund_orderNotInRefundPendingState_shouldThrowException() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    String deadlineId = "deadline-123";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerAccountId = "acct_seller123";
    String refundId = "refund-456";

    fixture.given(
        new FundsReservedEvent(
            orderId,
            paymentIntentId,
            paymentMethodId,
            deadlineId,
            amount,
            reservedAt,
            sellerId,
            sellerAccountId))
        .when(new FinishRefundCommand(orderId, refundId))
        .expectException(IllegalStateException.class)
        .expectNoEvents();
  }

  @Test
  void enterTrackingNumber_orderInFundsReservedState_shouldEmitEventAndCancelDeadline() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerStripeAccountId = "acct_seller123";
    String trackingNumber = "TRACK123456";

    fixture.givenCurrentTime(FIXED_TIME)
        .andGivenCommands(
            new AssignFundReservationCommand(
                orderId,
                paymentIntentId,
                paymentMethodId,
                amount,
                reservedAt,
                sellerId,
                sellerStripeAccountId),
            new EnterTrackingNumberCommand(orderId, trackingNumber))
        .whenTimeElapses(Duration.ofDays(REFUND_DEADLINE_DAYS))
        .expectNoScheduledDeadlines()
        .expectNoEvents()
        .expectState(order -> {
          assertEquals(orderId, order.getId());
          assertEquals(OrderStatus.TRACKING_NUMBER_PROVIDED, order.getStatus());
        });
  }

  @Test
  void enterTrackingNumber_orderNotInFundsReservedState_shouldThrowException() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    String deadlineId = "deadline-123";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerAccountId = "acct_seller123";
    String trackingNumber = "TRACK123456";

    fixture.given(
        new FundsReservedEvent(
            orderId,
            paymentIntentId,
            paymentMethodId,
            deadlineId,
            amount,
            reservedAt,
            sellerId,
            sellerAccountId),
        new OrderRefundScheduledEvent(orderId, paymentIntentId))
        .when(new EnterTrackingNumberCommand(orderId, trackingNumber))
        .expectException(IllegalStateException.class)
        .expectNoEvents();
  }

  @Test
  void assignTrackingInfo_orderInTrackingNumberProvidedState_shouldEmitEventAndUpdateState() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    String deadlineId = "deadline-123";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerAccountId = "acct_seller123";
    String trackingNumber = "TRACK123456";
    String ship24TrackerId = "ship24-tracker-789";
    Instant enteredAt = FIXED_TIME.plusSeconds(3600);

    fixture.given(
        new FundsReservedEvent(
            orderId,
            paymentIntentId,
            paymentMethodId,
            deadlineId,
            amount,
            reservedAt,
            sellerId,
            sellerAccountId),
        new TrackingNumberProvidedEvent(orderId, trackingNumber))
        .when(new AssignTrackingInfoCommand(orderId, trackingNumber, ship24TrackerId, enteredAt))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new TrackingNumberEnteredEvent(orderId, trackingNumber, ship24TrackerId, enteredAt))
        .expectState(order -> {
          assertEquals(orderId, order.getId());
          assertEquals(OrderStatus.TRACKING_IN_PROGRESS, order.getStatus());
          assertNotNull(order.getTrackingInfo());
          assertEquals(trackingNumber, order.getTrackingInfo().getTrackingNumber());
          assertEquals(ship24TrackerId, order.getTrackingInfo().getShip24TrackerId());
        });
  }

  @Test
  void assignTrackingInfo_orderNotInTrackingNumberProvidedState_shouldThrowException() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    String deadlineId = "deadline-123";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerAccountId = "acct_seller123";
    String trackingNumber = "TRACK123456";
    String ship24TrackerId = "ship24-tracker-789";
    Instant enteredAt = FIXED_TIME.plusSeconds(3600);

    fixture.given(
        new FundsReservedEvent(
            orderId,
            paymentIntentId,
            paymentMethodId,
            deadlineId,
            amount,
            reservedAt,
            sellerId,
            sellerAccountId))
        .when(new AssignTrackingInfoCommand(orderId, trackingNumber, ship24TrackerId, enteredAt))
        .expectException(IllegalStateException.class)
        .expectNoEvents();
  }

  @Test
  void updateTrackingStatus_simpleUpdate_shouldEmitEventAndUpdateState() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    String deadlineId = "deadline-123";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerAccountId = "acct_seller123";
    String trackingNumber = "TRACK123456";
    String ship24TrackerId = "ship24-tracker-789";
    Instant enteredAt = FIXED_TIME.plusSeconds(3600);
    String eventId = "event-001";
    Instant eventOccurredAt = FIXED_TIME.plusSeconds(7200);

    fixture.given(
        new FundsReservedEvent(
            orderId, paymentIntentId, paymentMethodId, deadlineId,
            amount, reservedAt, sellerId, sellerAccountId),
        new TrackingNumberProvidedEvent(orderId, trackingNumber),
        new TrackingNumberEnteredEvent(orderId, trackingNumber, ship24TrackerId, enteredAt))
        .when(new UpdateTrackingStatusCommand(
            orderId, eventId, TrackingStatusMilestone.IN_TRANSIT, "In transit", eventOccurredAt))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new TrackingStatusUpdatedEvent(
            orderId, eventId, TrackingStatusMilestone.IN_TRANSIT, "In transit", eventOccurredAt))
        .expectState(order -> {
          assertEquals(orderId, order.getId());
          assertEquals(OrderStatus.TRACKING_IN_PROGRESS, order.getStatus());
          assertEquals(TrackingStatusMilestone.IN_TRANSIT, order.getTrackingInfo().getStatusMilestone());
        });
  }

  @Test
  void updateTrackingStatus_delivered_shouldEmitEventsAndTransitionToDelivered() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    String deadlineId = "deadline-123";
    BigDecimal amount = new BigDecimal(100);
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerAccountId = "acct_seller123";
    String trackingNumber = "TRACK123456";
    String ship24TrackerId = "ship24-tracker-789";
    Instant enteredAt = FIXED_TIME.plusSeconds(3600);
    String eventId = "event-002";
    Instant eventOccurredAt = FIXED_TIME.plusSeconds(7200);

    fixture.given(
        new FundsReservedEvent(
            orderId, paymentIntentId, paymentMethodId, deadlineId,
            amount, reservedAt, sellerId, sellerAccountId),
        new TrackingNumberProvidedEvent(orderId, trackingNumber),
        new TrackingNumberEnteredEvent(orderId, trackingNumber, ship24TrackerId, enteredAt))
        .when(new UpdateTrackingStatusCommand(
            orderId, eventId, TrackingStatusMilestone.DELIVERED, "Delivered", eventOccurredAt))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            new TrackingStatusUpdatedEvent(
                orderId, eventId, TrackingStatusMilestone.DELIVERED, "Delivered", eventOccurredAt),
            new OrderDeliveredEvent(
                orderId, sellerAccountId,
                amount.multiply(BigDecimal.valueOf(0.9)),
                amount.multiply(BigDecimal.valueOf(0.1)),
                eventOccurredAt))
        .expectState(order -> {
          assertEquals(orderId, order.getId());
          assertEquals(OrderStatus.DELIVERED, order.getStatus());
        });
  }

  @Test
  void updateTrackingStatus_exception_shouldEmitEventsAndScheduleRefund() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    String deadlineId = "deadline-123";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerAccountId = "acct_seller123";
    String trackingNumber = "TRACK123456";
    String ship24TrackerId = "ship24-tracker-789";
    Instant enteredAt = FIXED_TIME.plusSeconds(3600);
    String eventId = "event-003";
    Instant eventOccurredAt = FIXED_TIME.plusSeconds(7200);

    fixture.given(
        new FundsReservedEvent(
            orderId, paymentIntentId, paymentMethodId, deadlineId,
            amount, reservedAt, sellerId, sellerAccountId),
        new TrackingNumberProvidedEvent(orderId, trackingNumber),
        new TrackingNumberEnteredEvent(orderId, trackingNumber, ship24TrackerId, enteredAt))
        .when(new UpdateTrackingStatusCommand(
            orderId, eventId, TrackingStatusMilestone.EXCEPTION, "Delivery exception", eventOccurredAt))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            new TrackingStatusUpdatedEvent(
                orderId, eventId, TrackingStatusMilestone.EXCEPTION, "Delivery exception", eventOccurredAt),
            new OrderRefundScheduledEvent(orderId, paymentIntentId))
        .expectState(order -> {
          assertEquals(orderId, order.getId());
          assertEquals(OrderStatus.REFUND_PENDING, order.getStatus());
        });
  }

  @Test
  void completeOrder_orderInDeliveredState_shouldEmitEventAndSetCompletionInfo() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    String deadlineId = "deadline-123";
    BigDecimal amount = new BigDecimal(100);
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerAccountId = "acct_seller123";
    Instant deliveredAt = FIXED_TIME.plusSeconds(7200);
    String paymentTransferId = "transfer-payout-123";
    String commissionTransferId = "transfer-commission-456";

    fixture.given(
        new FundsReservedEvent(
            orderId, paymentIntentId, paymentMethodId, deadlineId,
            amount, reservedAt, sellerId, sellerAccountId),
        new OrderDeliveredEvent(
            orderId, sellerAccountId,
            amount.multiply(BigDecimal.valueOf(0.9)),
            amount.multiply(BigDecimal.valueOf(0.1)),
            deliveredAt))
        .when(new CompleteOrderCommand(orderId, paymentTransferId, commissionTransferId))
        .expectSuccessfulHandlerExecution()
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(OrderCompletedEvent.class))))
        .expectState(order -> {
          assertEquals(orderId, order.getId());
          assertNotNull(order.getCompletionInfo());
          assertEquals(paymentTransferId, order.getCompletionInfo().getPayoutTransferId());
          assertEquals(commissionTransferId, order.getCompletionInfo().getCommissionTransferId());
        });
  }

  @Test
  void completeOrder_orderNotInDeliveredState_shouldThrowException() {
    UUID orderId = UUID.randomUUID();
    String paymentIntentId = "pi_test123";
    String paymentMethodId = "pm_test456";
    String deadlineId = "deadline-123";
    BigDecimal amount = new BigDecimal("100.00");
    Instant reservedAt = FIXED_TIME;
    String sellerId = "seller123";
    String sellerAccountId = "acct_seller123";
    String paymentTransferId = "transfer-payout-123";
    String commissionTransferId = "transfer-commission-456";

    fixture.given(
        new FundsReservedEvent(
            orderId, paymentIntentId, paymentMethodId, deadlineId,
            amount, reservedAt, sellerId, sellerAccountId))
        .when(new CompleteOrderCommand(orderId, paymentTransferId, commissionTransferId))
        .expectException(IllegalStateException.class)
        .expectNoEvents();
  }
}
