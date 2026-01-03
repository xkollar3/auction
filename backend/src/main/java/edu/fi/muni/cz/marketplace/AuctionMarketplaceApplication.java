package edu.fi.muni.cz.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AuctionMarketplaceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuctionMarketplaceApplication.class, args);
  }

}
