package edu.fi.muni.cz.marketplace.user.query;

import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserQueryHandler {

  private final EventStore eventStore;

  @QueryHandler
  public Optional<UserProjection> handle(SingleUserQuery query) {
    var events = eventStore.readEvents(query.id())
        .asStream()
        .map(eventMessage -> eventMessage.getPayload())
        .toList();

    UserProjection projection = null;

    for (Object event : events) {
      if (event instanceof UserRegisteredEvent registeredEvent) {
        projection = UserProjection.builder()
            .userId(registeredEvent.getUserId())
            .firstName(registeredEvent.getFirstName())
            .lastName(registeredEvent.getLastName())
            .email(registeredEvent.getEmail())
            .phoneNumber(registeredEvent.getPhoneNumber())
            .build();
      }
    }

    return Optional.ofNullable(projection);
  }
}
