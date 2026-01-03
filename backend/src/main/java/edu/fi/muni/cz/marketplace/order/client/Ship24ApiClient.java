package edu.fi.muni.cz.marketplace.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import edu.fi.muni.cz.marketplace.order.client.dto.CreateTrackerRequest;
import edu.fi.muni.cz.marketplace.order.client.dto.CreateTrackerResponse;

@FeignClient(name = "ship24", url = "${ship24.base-url}", configuration = Ship24ClientConfig.class)
public interface Ship24ApiClient {

  @PostMapping("/trackers")
  CreateTrackerResponse createTracker(@RequestBody CreateTrackerRequest request);
}
