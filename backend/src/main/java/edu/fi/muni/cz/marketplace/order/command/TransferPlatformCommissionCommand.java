package edu.fi.muni.cz.marketplace.order.command;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Value;

@Value
public class TransferPlatformCommissionCommand {

  private UUID orderId;
  private BigDecimal commision;
}
