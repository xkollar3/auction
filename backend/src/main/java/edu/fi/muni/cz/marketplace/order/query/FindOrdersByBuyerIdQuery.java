package edu.fi.muni.cz.marketplace.order.query;

import java.util.UUID;
import lombok.Value;

@Value
public class FindOrdersByBuyerIdQuery {
  UUID buyerId;
}
