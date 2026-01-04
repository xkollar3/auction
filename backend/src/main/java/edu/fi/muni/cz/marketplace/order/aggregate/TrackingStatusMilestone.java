package edu.fi.muni.cz.marketplace.order.aggregate;

public enum TrackingStatusMilestone {
  INFO_RECEIVED,
  IN_TRANSIT,
  OUT_FOR_DELIVERY,
  FAILED_ATTEMPT,
  AVAILABLE_FOR_PICKUP,
  DELIVERED,
  EXCEPTION,
  PENDING
}
