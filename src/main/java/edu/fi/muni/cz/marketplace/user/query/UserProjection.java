package edu.fi.muni.cz.marketplace.user.query;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProjection {

    String id;
    String userId;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
}
