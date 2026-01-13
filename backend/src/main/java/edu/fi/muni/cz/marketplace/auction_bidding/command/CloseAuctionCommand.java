package edu.fi.muni.cz.marketplace.auction_bidding.command;

import java.util.UUID;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class CloseAuctionCommand {

  @TargetAggregateIdentifier
  UUID auctionItemId;
}
