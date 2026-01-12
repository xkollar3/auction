package edu.fi.muni.cz.marketplace.user.service;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.user.event.UserRemovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ProcessingGroup("user_read_model")
@RequiredArgsConstructor
public class UserManagementEventHandler {

    private final KeycloakService keycloakService;

    @EventHandler
    public void on(UserRemovedEvent event) {
        log.info("Handling UserRemovedEvent for user: {}", event.getKeycloakUserId());
        try {
            keycloakService.deleteUser(event.getKeycloakUserId());
        } catch (Exception e) {
            log.warn("Failed to delete user from Keycloak during event handling", e);
        }
    }
}
