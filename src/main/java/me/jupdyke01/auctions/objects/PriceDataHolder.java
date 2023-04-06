package me.jupdyke01.auctions.objects;

import java.util.HashMap;

public class PriceDataHolder {

    private long lastUpdated;
    private HashMap<String, PriceData> items;

    public PriceDataHolder(long lastUpdated, HashMap<String,PriceData> items) {
        this.lastUpdated = lastUpdated;
        this.items = items;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setItems(HashMap<String, PriceData> items) {
        this.items = items;
    }

    public HashMap<String, PriceData> getItems() {
        return items;
    }
}
