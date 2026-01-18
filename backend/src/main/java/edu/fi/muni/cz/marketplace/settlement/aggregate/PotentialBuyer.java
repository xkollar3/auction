package edu.fi.muni.cz.marketplace.settlement.aggregate;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Value;

@Value
public class PotentialBuyer {
  UUID bidderId;
  BigDecimal bidAmount;
}
