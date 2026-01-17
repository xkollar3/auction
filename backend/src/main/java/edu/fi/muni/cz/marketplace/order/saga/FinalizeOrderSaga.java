package edu.fi.muni.cz.marketplace.order.saga;

import edu.fi.muni.cz.marketplace.order.command.CompleteOrderCommand;
import edu.fi.muni.cz.marketplace.order.command.TransferPlatformCommissionCommand;
import edu.fi.muni.cz.marketplace.order.command.TransferSellerPayoutCommand;
import edu.fi.muni.cz.marketplace.order.events.PlatformCommissionTransferredEvent;
import edu.fi.muni.cz.marketplace.order.events.OrderDeliveredEvent;
import edu.fi.muni.cz.marketplace.order.events.SellerPayoutTransferredEvent;
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
    commandGateway.send(new TransferPlatformCommissionCommand(event.getOrderId(), event.getCommission()));
    commandGateway
        .send(
            new TransferSellerPayoutCommand(event.getOrderId(), event.getSellerStripeAccountId(),
                event.getPayoutAmount()));
}

  @SagaEventHandler(associationProperty = "orderId")
  public void handler(PlatformCommissionTransferredEvent event) {
    this.commissionTransferId = event.getTransferId();
    if (this.paymentTransferId != null && !this.paymentTransferId.isEmpty()) {
      commandGateway
          .send(new CompleteOrderCommand(event.getOrderId(), this.paymentTransferId,
              this.commissionTransferId));
      SagaLifecycle.end();
    }
  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handler(SellerPayoutTransferredEvent event) {
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
