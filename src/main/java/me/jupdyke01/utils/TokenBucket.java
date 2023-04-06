package me.jupdyke01.utils;

public class TokenBucket {

    private final int capacity;
    private int tokens;
    private long lastRefillTime;

    public TokenBucket(int capacity) {
        this.capacity = capacity;
        this.tokens = capacity;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized void consumeToken() {
        while (tokens == 0) {
            refillTokens();
            try {
                wait(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        tokens--;
    }

    private void refillTokens() {
        int tokensBefore = tokens;
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRefillTime;
        int tokensToAdd = (int) (elapsedTime / 6000) * 10;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        if (tokens != tokensBefore) {
            lastRefillTime = currentTime;
        }
    }
}
