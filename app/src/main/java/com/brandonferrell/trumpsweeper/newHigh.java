package com.brandonferrell.trumpsweeper;

/**
 * Created by Brandon on 4/9/2016.
 */
public class newHigh {
    String name;
    int count;
    double time;

    public newHigh() {}

    public newHigh(String n, int c) {
        name = n;
        count = c;
    }

    public newHigh(String n, double t) {
        name = n;
        time = t;
    }

    public double getTime() {
        return time;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

}