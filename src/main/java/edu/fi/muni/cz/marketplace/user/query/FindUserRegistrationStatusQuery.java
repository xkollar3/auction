package edu.fi.muni.cz.marketplace.user.query;

import lombok.Value;

import java.util.UUID;

@Value
public class FindUserRegistrationStatusQuery {
  UUID id;
}
