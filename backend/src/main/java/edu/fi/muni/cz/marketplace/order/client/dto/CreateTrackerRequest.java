package edu.fi.muni.cz.marketplace.order.client.dto;

public record CreateTrackerRequest(
    String trackingNumber,
    String shipmentReference
) {
}
