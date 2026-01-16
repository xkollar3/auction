package edu.fi.muni.cz.marketplace.settlement.aggregate;

import java.math.BigDecimal;
import java.util.UUID;

public record PotentialBuyer(
        UUID bidderId,
        BigDecimal bidAmount
) {
}
