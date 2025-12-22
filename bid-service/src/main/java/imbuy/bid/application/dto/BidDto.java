package imbuy.bid.application.dto;

import java.math.BigDecimal;

public record BidDto(
        Long id,
        BigDecimal amount,
        Long bidder_id,
        String bidder_username
) {}
