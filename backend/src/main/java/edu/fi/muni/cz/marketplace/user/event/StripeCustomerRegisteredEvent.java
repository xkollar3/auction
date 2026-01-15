package edu.fi.muni.cz.marketplace.user.event;

import lombok.Value;

import java.util.UUID;

@Value
public class StripeCustomerRegisteredEvent {
    UUID userId;
    String stripeCustomerId;
}
