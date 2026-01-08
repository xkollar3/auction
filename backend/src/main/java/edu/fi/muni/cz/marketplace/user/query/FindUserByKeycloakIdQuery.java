package edu.fi.muni.cz.marketplace.user.query;

import lombok.Value;

@Value
public class FindUserByKeycloakIdQuery {
    String keycloakUserId;
}
