package edu.fi.muni.cz.marketplace.config;

import edu.fi.muni.cz.marketplace.order.client.Ship24ApiClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {Ship24ApiClient.class})
public class FeignClientConfig {
}
