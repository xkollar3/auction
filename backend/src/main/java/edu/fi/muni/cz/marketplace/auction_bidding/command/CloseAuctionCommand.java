package edu.fi.muni.cz.marketplace.auction_bidding.command;

import java.util.UUID;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CloseAuctionCommand(
    @TargetAggregateIdentifier
    UUID auctionItemId
) {

}
