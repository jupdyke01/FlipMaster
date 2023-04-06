package me.jupdyke01;

import me.jupdyke01.auctions.AuctionManager;
import me.jupdyke01.auctions.AuctionWindow;
import me.jupdyke01.bazzar.BazzarManager;
import me.jupdyke01.bazzar.BazzarWindow;
import me.jupdyke01.gui.GUIManager;

import java.awt.*;
import java.awt.image.BufferStrategy;

public class FlipMaster extends Canvas implements Runnable {

    public static final String version = "v1.1";
    public String status = "On";

    private Thread thread;
    private boolean running;
    private GUIManager guiManager;
    private AuctionManager auctionManager;
    private AuctionWindow auctionWindow;
    private BazzarManager bazzarManager;
    private BazzarWindow bazzarWindow;

    public FlipMaster() {
        setFocusTraversalKeysEnabled(false);
        Dimension d = new Dimension(900, 650);
        setPreferredSize(d);
        auctionWindow = new AuctionWindow(this);
        auctionManager = new AuctionManager(this);
        bazzarManager = new BazzarManager(this);
        bazzarWindow = new BazzarWindow(this);
        guiManager = new GUIManager(this);
        addMouseListener(guiManager);
        addKeyListener(guiManager);
    }



    public void render(long delta) {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(2);
            return;
        }
        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,this.getWidth(), this.getHeight());

        guiManager.render(g, delta);
        auctionWindow.render(g, delta);
        bazzarWindow.render(g, delta);

        g.dispose();
        bs.show();
    }

    public void update() {

    }

    public void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
        status = "On";
    }

    public void stop() {
        try {
            status = "Off";
            thread.join();
            running = false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        long now;
        long lastUpdateTime = System.nanoTime();
        long elapsedUpdateTime;
        long wait;

        final int TARGET_FPS = 120;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

        while (running) {
            now = System.currentTimeMillis();
            elapsedUpdateTime = now - lastUpdateTime;
            lastUpdateTime = now;

            update();
            render(elapsedUpdateTime);

            long loopTime = System.currentTimeMillis() - now;
            wait = (OPTIMAL_TIME - loopTime) / 1000000;

            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public AuctionWindow getAuctionWindow() {
        return auctionWindow;
    }

    public BazzarManager getBazzarManager() {
        return bazzarManager;
    }

    public BazzarWindow getBazzarWindow() {
        return bazzarWindow;
    }
}
