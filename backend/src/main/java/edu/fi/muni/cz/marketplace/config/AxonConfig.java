package edu.fi.muni.cz.marketplace.config;

import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.event.axon.AxonServerEventScheduler;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

  @Bean
  public EventScheduler eventScheduler(AxonServerConnectionManager connectionManager, Serializer axonSerializer) {
    return AxonServerEventScheduler.builder()
        .connectionManager(connectionManager)
        .eventSerializer(axonSerializer)
        .build();
  }
}
