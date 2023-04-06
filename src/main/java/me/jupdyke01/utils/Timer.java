package me.jupdyke01.utils;

public class Timer {

    private final String name;
    private Long start;
    private Long stop;

    public Timer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void stop() {
        stop = System.currentTimeMillis();
    }

    public Double getSeconds() {
        return (stop - start) / 1000.0;
    }

    public Long getDelta() {
        return (stop - start);
    }

    public void display() {
        System.out.println("Timer ended: " + name + " Took: " + getSeconds() + " seconds!");
    }

}
