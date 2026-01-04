package edu.fi.muni.cz.marketplace.order.dto;

import java.util.UUID;

public record EnterTrackingNumberResponse(
    UUID orderId
) {
}
