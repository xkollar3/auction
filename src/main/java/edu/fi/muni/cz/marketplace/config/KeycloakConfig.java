package edu.fi.muni.cz.marketplace.config;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

  private final KeycloakAdminProperties properties;

  @Bean
  public Keycloak keycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(properties.getServerUrl())
        .realm("master")
        .clientId(properties.getClientId())
        .username(properties.getUsername())
        .password(properties.getPassword())
        .build();
  }
}
