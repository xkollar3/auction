package edu.fi.muni.cz.marketplace.order.saga;

import static org.axonframework.test.matchers.Matchers.andNoMore;
import static org.axonframework.test.matchers.Matchers.exactSequenceOf;
import static org.axonframework.test.matchers.Matchers.messageWithPayload;
import static org.hamcrest.Matchers.instanceOf;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.fi.muni.cz.marketplace.order.command.CompleteOrderCommand;
import edu.fi.muni.cz.marketplace.order.command.DeductCommissionCommand;
import edu.fi.muni.cz.marketplace.order.command.TransferPaymentCommand;
import edu.fi.muni.cz.marketplace.order.events.CommissionDeductedEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderDeliveredEvent;
import edu.fi.muni.cz.marketplace.order.events.PaymentTransferredEvent;

class FinalizeOrderSagaTest {

  private SagaTestFixture<FinalizeOrderSaga> fixture;

  @BeforeEach
  void setUp() {
    fixture = new SagaTestFixture<>(FinalizeOrderSaga.class);
  }

  @Test
  void handleOrderDeliveredEvent_noPriorActivity_startsSagaAndDispatchesBothCommands() {
    UUID orderId = UUID.randomUUID();
    String sellerStripeAccountId = "acct_seller123";
    BigDecimal payoutAmount = new BigDecimal("90.00");
    BigDecimal commission = new BigDecimal("10.00");
    Instant deliveredAt = Instant.now();

    fixture.givenNoPriorActivity()
        .whenPublishingA(new OrderDeliveredEvent(
            orderId,
            sellerStripeAccountId,
            payoutAmount,
            commission,
            deliveredAt))
        .expectActiveSagas(1)
        .expectDispatchedCommands(
            new DeductCommissionCommand(orderId, commission),
            new TransferPaymentCommand(orderId, sellerStripeAccountId, payoutAmount));
  }

  @Test
  void handleCommissionDeductedEvent_sagaStarted_storesTransferIdAndKeepsSagaActive() {
    UUID orderId = UUID.randomUUID();
    String sellerStripeAccountId = "acct_seller123";
    BigDecimal payoutAmount = new BigDecimal("90.00");
    BigDecimal commission = new BigDecimal("10.00");
    Instant deliveredAt = Instant.now();
    String commissionTransferId = "transfer-commission-123";

    fixture.givenAPublished(new OrderDeliveredEvent(
        orderId,
        sellerStripeAccountId,
        payoutAmount,
        commission,
        deliveredAt))
        .whenPublishingA(new CommissionDeductedEvent(orderId, commissionTransferId))
        .expectActiveSagas(1)
        .expectNoDispatchedCommands();
  }

  @Test
  void handlePaymentTransferredEvent_sagaStarted_storesTransferIdAndKeepsSagaActive() {
    UUID orderId = UUID.randomUUID();
    String sellerStripeAccountId = "acct_seller123";
    BigDecimal payoutAmount = new BigDecimal("90.00");
    BigDecimal commission = new BigDecimal("10.00");
    Instant deliveredAt = Instant.now();
    String paymentTransferId = "transfer-payout-456";

    fixture.givenAPublished(new OrderDeliveredEvent(
        orderId,
        sellerStripeAccountId,
        payoutAmount,
        commission,
        deliveredAt))
        .whenPublishingA(new PaymentTransferredEvent(orderId, paymentTransferId))
        .expectActiveSagas(1)
        .expectNoDispatchedCommands();
  }

  @Test
  void handlePaymentTransferredEvent_commissionAlreadyDeducted_completesOrderAndEndsSaga() {
    UUID orderId = UUID.randomUUID();
    String sellerStripeAccountId = "acct_seller123";
    BigDecimal payoutAmount = new BigDecimal("90.00");
    BigDecimal commission = new BigDecimal("10.00");
    Instant deliveredAt = Instant.now();
    String commissionTransferId = "transfer-commission-123";
    String paymentTransferId = "transfer-payout-456";

    fixture.givenAPublished(new OrderDeliveredEvent(
            orderId,
            sellerStripeAccountId,
            payoutAmount,
            commission,
            deliveredAt))
        .andThenAPublished(new CommissionDeductedEvent(orderId, commissionTransferId))
        .whenPublishingA(new PaymentTransferredEvent(orderId, paymentTransferId))
        .expectActiveSagas(0)
        .expectDispatchedCommands(
            new CompleteOrderCommand(orderId, paymentTransferId, commissionTransferId));
  }

  @Test
  void handleCommissionDeductedEvent_paymentAlreadyTransferred_completesOrderAndEndsSaga() {
    UUID orderId = UUID.randomUUID();
    String sellerStripeAccountId = "acct_seller123";
    BigDecimal payoutAmount = new BigDecimal("90.00");
    BigDecimal commission = new BigDecimal("10.00");
    Instant deliveredAt = Instant.now();
    String commissionTransferId = "transfer-commission-123";
    String paymentTransferId = "transfer-payout-456";

    fixture.givenAPublished(new OrderDeliveredEvent(
            orderId,
            sellerStripeAccountId,
            payoutAmount,
            commission,
            deliveredAt))
        .andThenAPublished(new PaymentTransferredEvent(orderId, paymentTransferId))
        .whenPublishingA(new CommissionDeductedEvent(orderId, commissionTransferId))
        .expectActiveSagas(0)
        .expectDispatchedCommands(
            new CompleteOrderCommand(orderId, paymentTransferId, commissionTransferId));
  }

  @Test
  void fullSagaFlow_commissionThenPayment_completesSuccessfully() {
    UUID orderId = UUID.randomUUID();
    String sellerStripeAccountId = "acct_seller123";
    BigDecimal payoutAmount = new BigDecimal("90.00");
    BigDecimal commission = new BigDecimal("10.00");
    Instant deliveredAt = Instant.now();
    String commissionTransferId = "transfer-commission-123";
    String paymentTransferId = "transfer-payout-456";

    fixture.givenNoPriorActivity()
        .whenPublishingA(new OrderDeliveredEvent(
            orderId,
            sellerStripeAccountId,
            payoutAmount,
            commission,
            deliveredAt))
        .expectDispatchedCommandsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(DeductCommissionCommand.class)),
            messageWithPayload(instanceOf(TransferPaymentCommand.class)),
            andNoMore()));

    fixture.whenPublishingA(new CommissionDeductedEvent(orderId, commissionTransferId))
        .expectActiveSagas(1)
        .expectNoDispatchedCommands();

    fixture.whenPublishingA(new PaymentTransferredEvent(orderId, paymentTransferId))
        .expectActiveSagas(0)
        .expectDispatchedCommandsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(CompleteOrderCommand.class)),
            andNoMore()));
  }

  @Test
  void fullSagaFlow_paymentThenCommission_completesSuccessfully() {
    UUID orderId = UUID.randomUUID();
    String sellerStripeAccountId = "acct_seller123";
    BigDecimal payoutAmount = new BigDecimal("90.00");
    BigDecimal commission = new BigDecimal("10.00");
    Instant deliveredAt = Instant.now();
    String commissionTransferId = "transfer-commission-123";
    String paymentTransferId = "transfer-payout-456";

    fixture.givenNoPriorActivity()
        .whenPublishingA(new OrderDeliveredEvent(
            orderId,
            sellerStripeAccountId,
            payoutAmount,
            commission,
            deliveredAt))
        .expectDispatchedCommandsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(DeductCommissionCommand.class)),
            messageWithPayload(instanceOf(TransferPaymentCommand.class)),
            andNoMore()));

    fixture.whenPublishingA(new PaymentTransferredEvent(orderId, paymentTransferId))
        .expectActiveSagas(1)
        .expectNoDispatchedCommands();

    fixture.whenPublishingA(new CommissionDeductedEvent(orderId, commissionTransferId))
        .expectActiveSagas(0)
        .expectDispatchedCommandsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(CompleteOrderCommand.class)),
            andNoMore()));
  }
}
