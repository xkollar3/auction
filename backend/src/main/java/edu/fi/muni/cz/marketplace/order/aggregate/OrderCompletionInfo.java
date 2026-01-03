package edu.fi.muni.cz.marketplace.order.aggregate;

import java.time.Instant;

import javax.annotation.Nonnull;

import lombok.Value;

@Value
public class OrderCompletionInfo {

  @Nonnull
  private Instant completedAt;

  @Nonnull
  private String payoutTransferId;

  @Nonnull
  private String commissionTransferId;
}
