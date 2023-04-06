package me.jupdyke01.utils;

public class RateLimiter {

    private final int maxRequests;
    private final long interval;
    private long lastRequestTime = System.currentTimeMillis();
    private int numRequests = 0;

    public RateLimiter(int maxRequests, long interval) {
        this.maxRequests = maxRequests;
        this.interval = interval;
    }

    public synchronized void acquire() {
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - lastRequestTime;

        if (timeElapsed < interval) {
            try {
                Thread.sleep(interval - timeElapsed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        lastRequestTime = System.currentTimeMillis();

        if (numRequests == maxRequests) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            numRequests = 0;
            lastRequestTime = System.currentTimeMillis();
        }

        numRequests++;
    }
}
