package me.jupdyke01.auctions.objects;

public class PriceData {

    private int medianPrice;
    private double volume;

    public PriceData(int medianPrice, double volume) {
        this.medianPrice = medianPrice;
        this.volume = volume;
    }

    public int getMedianPrice() {
        return medianPrice;
    }

    public double getVolume() {
        return volume;
    }
}
