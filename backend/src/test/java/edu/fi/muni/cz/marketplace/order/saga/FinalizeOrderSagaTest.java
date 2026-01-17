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
import edu.fi.muni.cz.marketplace.order.command.TransferPlatformCommissionCommand;
import edu.fi.muni.cz.marketplace.order.command.TransferSellerPayoutCommand;
import edu.fi.muni.cz.marketplace.order.events.PlatformCommissionTransferredEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderDeliveredEvent;
import edu.fi.muni.cz.marketplace.order.events.SellerPayoutTransferredEvent;

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
            new TransferPlatformCommissionCommand(orderId, commission),
            new TransferSellerPayoutCommand(orderId, sellerStripeAccountId, payoutAmount));
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
        .whenPublishingA(new PlatformCommissionTransferredEvent(orderId, commissionTransferId))
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
        .whenPublishingA(new SellerPayoutTransferredEvent(orderId, paymentTransferId))
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
        .andThenAPublished(new PlatformCommissionTransferredEvent(orderId, commissionTransferId))
        .whenPublishingA(new SellerPayoutTransferredEvent(orderId, paymentTransferId))
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
        .andThenAPublished(new SellerPayoutTransferredEvent(orderId, paymentTransferId))
        .whenPublishingA(new PlatformCommissionTransferredEvent(orderId, commissionTransferId))
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
            messageWithPayload(instanceOf(TransferPlatformCommissionCommand.class)),
            messageWithPayload(instanceOf(TransferSellerPayoutCommand.class)),
            andNoMore()));

    fixture.whenPublishingA(new PlatformCommissionTransferredEvent(orderId, commissionTransferId))
        .expectActiveSagas(1)
        .expectNoDispatchedCommands();

    fixture.whenPublishingA(new SellerPayoutTransferredEvent(orderId, paymentTransferId))
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
            messageWithPayload(instanceOf(TransferPlatformCommissionCommand.class)),
            messageWithPayload(instanceOf(TransferSellerPayoutCommand.class)),
            andNoMore()));

    fixture.whenPublishingA(new SellerPayoutTransferredEvent(orderId, paymentTransferId))
        .expectActiveSagas(1)
        .expectNoDispatchedCommands();

    fixture.whenPublishingA(new PlatformCommissionTransferredEvent(orderId, commissionTransferId))
        .expectActiveSagas(0)
        .expectDispatchedCommandsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(CompleteOrderCommand.class)),
            andNoMore()));
  }
}
