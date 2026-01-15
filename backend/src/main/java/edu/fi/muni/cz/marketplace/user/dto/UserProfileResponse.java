package edu.fi.muni.cz.marketplace.user.dto;

import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String keycloakUserId,
    String stripeCustomerId,
    String stripeSellerAccountId,
    String stripePaymentMethodId,
    boolean sellerAccountEnabled
) {}
