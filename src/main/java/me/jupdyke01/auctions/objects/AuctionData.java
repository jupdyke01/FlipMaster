package me.jupdyke01.auctions.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AuctionData {

    private List<BINAuction> auctions;

    public AuctionData() {
        auctions = Collections.synchronizedList(new ArrayList<>());
    }

    public void addAuction(BINAuction auction) {
        auctions.add(auction);
    }

    public List<BINAuction> getAuctions() {
        return auctions;
    }

    public void sortAuctions() {
        auctions.sort(Comparator.comparingInt(BINAuction::getPrice));
    }

    public BINAuction getFirst() {
        if (auctions.isEmpty()) {
            return null;
        }
        return auctions.get(0);
    }

    public BINAuction getSecond() {
        if (auctions.size() < 2) {
            return null;
        }
        return auctions.get(1);
    }


}
