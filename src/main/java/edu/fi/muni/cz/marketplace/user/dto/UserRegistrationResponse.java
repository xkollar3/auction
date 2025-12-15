package edu.fi.muni.cz.marketplace.user.dto;

import java.util.UUID;

import edu.fi.muni.cz.marketplace.user.aggregate.RegistrationStatus;

public record UserRegistrationResponse(
    UUID id,
    String nickname,
    String keycloakUserId,
    RegistrationStatus status,
    String errorMessage
) {
}
