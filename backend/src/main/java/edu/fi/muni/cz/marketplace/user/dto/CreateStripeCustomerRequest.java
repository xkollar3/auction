package edu.fi.muni.cz.marketplace.user.dto;

public record CreateStripeCustomerRequest(
    String line1,
    String line2,
    String city,
    String state,
    String postalCode,
    String country
) {}
