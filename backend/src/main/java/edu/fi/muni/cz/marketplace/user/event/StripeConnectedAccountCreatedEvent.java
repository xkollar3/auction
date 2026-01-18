package edu.fi.muni.cz.marketplace.user.event;

import lombok.Value;

import java.util.UUID;

@Value
public class StripeConnectedAccountCreatedEvent {
    UUID userId;
    String stripeAccountId;
    String stripeOnboardingLink;
}
