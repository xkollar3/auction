package edu.fi.muni.cz.marketplace.user.event;

import java.util.UUID;

import lombok.Value;

@Value
public class StripeSellerAccountCreatedEvent {
    UUID id;
    String stripeSellerAccountId;
}
