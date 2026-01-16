package edu.fi.muni.cz.marketplace.settlement.events;

import lombok.Value;

import java.util.UUID;

@Value
public class PurchaseRejectedEvent {

    UUID settlementId;

}
