package com.brandonferrell.trumpsweeper.models;

import android.view.View;

/**
 * Created by Brandon on 4/9/2016.
 */
public class newHigh {
    String name;
    int count;
    double time;
    long rank;

    public newHigh() {}

    public newHigh(String n, int c) {
        name = n;
        count = c;
        time = 0;
    }

    public newHigh(String n, double t) {
        name = n;
        time = t;
        count = 0;
    }

    public double getTime() {
        return time;
    }

    public long getRank() { return rank; }

    public void setRank(long r) { rank = r; }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public String getTimeFormatted() {
        int minutes = (int) (time / (1000 * 60));
        int seconds = (int) ((time / 1000) % 60);
        int seconds100 = (int) ((time / 10) % 100);
        return String.format("%d:%02d.%02d", minutes, seconds, seconds100);
    }

}