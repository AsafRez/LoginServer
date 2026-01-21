package org.example;

import java.util.Date;

public class Stock {
    private String Ticker;
    private float Price;
    private double RSI;
    private String Trend;
    private String Pattern;
    private Date timeStape;
    public Stock(String Ticker) {
        this.Ticker = Ticker;
        this.Trend = "-";
        this.Pattern = "-";
    }

    public String getTicker() {
        return Ticker;
    }

    public float getPrice() {
        return Price;
    }

    public double getRSI() {
        return RSI;
    }

    public String getTrend() {
        return Trend;
    }

    public String getPattern() {
        return Pattern;
    }

    public Date getTimeStape() {
        return timeStape;
    }
}