package imbuy.lot.application.port.out;

public interface BidPort {
    Long getAuctionWinner(Long lotId);
}
