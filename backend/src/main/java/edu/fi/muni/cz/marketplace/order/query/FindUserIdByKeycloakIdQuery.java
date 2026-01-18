package edu.fi.muni.cz.marketplace.order.query;

import lombok.Value;

@Value
public class FindUserIdByKeycloakIdQuery {
  String keycloakUserId;
}
