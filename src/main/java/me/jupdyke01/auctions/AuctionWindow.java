package me.jupdyke01.auctions;

import me.jupdyke01.FlipMaster;
import me.jupdyke01.auctions.objects.AuctionData;
import me.jupdyke01.auctions.objects.BINAuction;
import me.jupdyke01.generics.Pair;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionWindow {

    private FlipMaster main;

    private boolean ready = true;
    private LinkedHashMap<String, AuctionData> renderItems = new LinkedHashMap<>();
    private double renderSleep = 0.0;

    public AuctionWindow(FlipMaster main) {
        this.main = main;
    }

    public void render(Graphics2D g, long delta) {
        if (renderSleep > 0) {
            renderSleep = Math.max(0.0, renderSleep - (delta/1000.0));
        }
        //Width of auction box = 900/2 = 450

        // 450 - 60 = 390
        // height = 60
        // spacing = 20
        //Startings at y = 150
        if (!ready) {
            return;
        }
        int i = 1;
        for (String item : renderItems.keySet()) {
            AuctionData bins = renderItems.get(item);
            BINAuction lowestAuction = bins.getFirst();
            BINAuction secondAuction = bins.getSecond();
            int lowestPrice = lowestAuction.getPrice();
            int priceDif = secondAuction.getPrice() - lowestPrice;
            int x = 30;
            int y = 170 + ((i-1) * 70);
            g.setColor(Color.GRAY);
            g.fillRect(x, y,390, 50);

            NumberFormat formatter = NumberFormat.getCurrencyInstance();

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));

            g.drawString("Name: " + item, x + 5, y + 15);
            g.drawString("Cost: " + formatter.format(lowestPrice), x + 5, y + 30);
            g.drawString("Profit: " + formatter.format(priceDif), x + 5, y + 45);
            i++;
        }
    }

    public void clicked(int x, int y) {
        if (x > 30 && x < 30 + 390) {
            y -= 170;
            y /= 70;
            int i = 0;
            for (String item : renderItems.keySet()) {
                if (i == y) {
                    String auctionUuid = "/viewauction " + renderItems.get(item).getFirst().getAuctionId();
                    StringSelection selection = new StringSelection(auctionUuid);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, null);
                    break;
                }
                i++;
            }
        }
    }

    public void updateItems(ConcurrentHashMap<String, AuctionData> items) {
        ready = false;
        renderItems.clear();
        LinkedList<Map.Entry<String, AuctionData>> list = new LinkedList<>(items.entrySet());
        list.sort(new Comparator<Map.Entry<String, AuctionData>>() {
            @Override
            public int compare(Map.Entry<String, AuctionData> o1, Map.Entry<String, AuctionData> o2) {
                return (
                         o1.getValue().getSecond().getPrice() - o1.getValue().getFirst().getPrice()) -
                        (o2.getValue().getSecond().getPrice() - o2.getValue().getFirst().getPrice());
            }
        }.reversed());

        for (Map.Entry<String, AuctionData> item : list) {
            renderItems.put(item.getKey(), item.getValue());
        }
        ready = true;
    }

    public String getRenderSleep() {
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(renderSleep);
    }

    public void updateSleep(double sleepTime) {
        this.renderSleep = (sleepTime/1000.0);
    }

}
