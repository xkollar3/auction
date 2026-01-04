package edu.fi.muni.cz.marketplace.order.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class Ship24ClientConfig {

  @Value("${ship24.api-key}")
  private String apiKey;

  @Bean
  public RequestInterceptor ship24AuthInterceptor() {
    return requestTemplate -> requestTemplate.header("Authorization", "Bearer " + apiKey);
  }
}
