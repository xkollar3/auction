package edu.fi.muni.cz.marketplace.auction_bidding.command;

import java.math.BigDecimal;
import java.util.UUID;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record PlaceBidCommand(
    @TargetAggregateIdentifier UUID auctionItemId,
    UUID bidderId,
    BigDecimal bidAmount
) {

}
