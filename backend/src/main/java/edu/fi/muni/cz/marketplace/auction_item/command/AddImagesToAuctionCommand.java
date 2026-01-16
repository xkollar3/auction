package edu.fi.muni.cz.marketplace.auction_item.command;

import java.util.List;
import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class AddImagesToAuctionCommand {

    @TargetAggregateIdentifier
    UUID auctionItemId;
    List<String> imageUrls;
}
