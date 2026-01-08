package edu.fi.muni.cz.marketplace.user.command;

import java.util.UUID;

import lombok.Value;

@Value
public class CreateStripeConnectedAccountCommand {
    UUID id;
    String email;
}
