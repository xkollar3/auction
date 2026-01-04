package edu.fi.muni.cz.marketplace.order.saga;

import edu.fi.muni.cz.marketplace.order.command.CompleteOrderCommand;
import edu.fi.muni.cz.marketplace.order.command.DeductCommissionCommand;
import edu.fi.muni.cz.marketplace.order.command.TransferPaymentCommand;
import edu.fi.muni.cz.marketplace.order.events.CommissionDeductedEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderDeliveredEvent;
import edu.fi.muni.cz.marketplace.order.events.PaymentTransferredEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@Slf4j
public class FinalizeOrderSaga {

  private String commissionTransferId;
  private String paymentTransferId;

  private transient CommandGateway commandGateway;

  @StartSaga
  @SagaEventHandler(associationProperty = "orderId")
  public void handler(OrderDeliveredEvent event) {
    log.info("Order finalization saga started, order id: {}", event.getOrderId());
    commandGateway.send(new DeductCommissionCommand(event.getOrderId(), event.getCommission()));
    commandGateway
        .send(
            new TransferPaymentCommand(event.getOrderId(), event.getSellerStripeAccountId(),
                event.getPayoutAmount()));
  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handler(CommissionDeductedEvent event) {
    this.commissionTransferId = event.getTransferId();
    if (this.paymentTransferId != null && !this.paymentTransferId.isEmpty()) {
      commandGateway
          .send(new CompleteOrderCommand(event.getOrderId(), this.paymentTransferId,
              this.commissionTransferId));
      SagaLifecycle.end();
    }
  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handler(PaymentTransferredEvent event) {
    this.paymentTransferId = event.getTransferId();
    if (this.commissionTransferId != null && !this.commissionTransferId.isEmpty()) {
      commandGateway
          .send(new CompleteOrderCommand(event.getOrderId(), this.paymentTransferId,
              this.commissionTransferId));
      SagaLifecycle.end();
    }
  }

  @Autowired
  public void setCommandGateway(CommandGateway commandGateway) {
    this.commandGateway = commandGateway;
  }
}
