package edu.fi.muni.cz.marketplace.user.service;

import edu.fi.muni.cz.marketplace.config.KeycloakAdminProperties;
import edu.fi.muni.cz.marketplace.user.exception.KeycloakRegistrationFailedException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

  private final Keycloak keycloak;
  private final KeycloakAdminProperties properties;

  public String createUser(String email, String firstName, String lastName, String username, String password) {
    log.info("Creating Keycloak user for email: {}", email);

    RealmResource realmResource = keycloak.realm(properties.getRealm());
    UsersResource usersResource = realmResource.users();

    UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername(username);
    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmailVerified(false);

    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(password);
    credential.setTemporary(false);

    user.setCredentials(Collections.singletonList(credential));

    try (Response response = usersResource.create(user)) {
      if (response.getStatus() == 201) {
        String locationHeader = response.getHeaderString("Location");
        String keycloakUserId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
        log.info("User created successfully with Keycloak ID: {}", keycloakUserId);
        return keycloakUserId;
      } else {
        String errorMessage = response.readEntity(String.class);
        log.error("Failed to create Keycloak user. Status: {}, Error: {}", response.getStatus(), errorMessage);
        throw new KeycloakRegistrationFailedException(response.getStatus(), errorMessage);
      }
    } catch (KeycloakRegistrationFailedException e) {
      throw e;
    } catch (Exception e) {
      throw new KeycloakRegistrationFailedException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unknown erorr");
    }
  }
}
