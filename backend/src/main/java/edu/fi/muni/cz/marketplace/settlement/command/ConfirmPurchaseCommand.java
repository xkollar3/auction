package edu.fi.muni.cz.marketplace.settlement.command;

import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Value
public class ConfirmPurchaseCommand {

    @TargetAggregateIdentifier
    UUID settlementId;

}
