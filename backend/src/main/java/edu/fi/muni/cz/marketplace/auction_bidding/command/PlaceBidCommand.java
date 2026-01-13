package edu.fi.muni.cz.marketplace.auction_bidding.command;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class PlaceBidCommand {

  @TargetAggregateIdentifier
  UUID auctionItemId;
  UUID bidId;
  UUID bidderId;
  BigDecimal bidAmount;
}
