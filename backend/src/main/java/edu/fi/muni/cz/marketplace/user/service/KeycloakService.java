package edu.fi.muni.cz.marketplace.user.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KeycloakService {

    private final String serverUrl;
    private final String realm;
    private final String authRealm;
    private final String clientId;
    private final String username;
    private final String password;

    public KeycloakService(
            @Value("${keycloak.admin.server-url}") String serverUrl,
            @Value("${keycloak.admin.realm}") String realm,
            @Value("${keycloak.admin.auth-realm:master}") String authRealm,
            @Value("${keycloak.admin.client-id}") String clientId,
            @Value("${keycloak.admin.username}") String username,
            @Value("${keycloak.admin.password}") String password) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.authRealm = authRealm;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }

    private Keycloak getKeycloakClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(authRealm)
                .grantType("password")
                .clientId(clientId)
                .username(username)
                .password(password)
                .build();
    }

    public void deleteUser(String keycloakUserId) {
        log.info("Deleting user from Keycloak: {}", keycloakUserId);
        try (Keycloak keycloak = getKeycloakClient()) {
            keycloak.realm(realm).users().delete(keycloakUserId);
            log.info("User deleted from Keycloak successfully.");
        } catch (Exception e) {
            log.error("Failed to delete user from Keycloak: {}", keycloakUserId, e);
            throw new RuntimeException("Failed to delete user from Keycloak", e);
        }
    }
}
