package edu.fi.muni.cz.marketplace.auction_bidding.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class AddAuctionItemCommand {

  @TargetAggregateIdentifier
  UUID auctionItemId;
  UUID sellerId;
  String title;
  String description;
  BigDecimal startingPrice;
  Instant auctionEndTime;
}
