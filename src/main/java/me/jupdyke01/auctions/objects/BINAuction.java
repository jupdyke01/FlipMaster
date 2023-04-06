package me.jupdyke01.auctions.objects;

public class BINAuction {

    private final String auctionId;
    private final int price;

    public BINAuction(String auctionId, int price) {
        this.auctionId = auctionId;
        this.price = price;
    }

    public BINAuction(String auctionId, int price, int medianPrice, double volume) {
        this.auctionId = auctionId;
        this.price = price;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public int getPrice() {
        return price;
    }
}
