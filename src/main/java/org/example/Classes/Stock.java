package org.example.Classes;

import java.sql.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stock {
    @JsonProperty("Ticker")
    private String Ticker;
    @JsonProperty("Price")
    private float Price;
    @JsonProperty("RSI")
    private double RSI;
    @JsonProperty("Trend")
    private String Trend;
    @JsonProperty("Pattern")
    private String Pattern;
    @JsonProperty("SMA50")
    private double SMA50;
    @JsonProperty("SMA150")
    private double SMA150;
    @JsonProperty("timeStape")
    private Date TimeStamp;

    public double getSMA50() {
        return SMA50;
    }

    public void setSMA50(double SMA50) {
        this.SMA50 = SMA50;
    }

    public double getSMA150() {
        return SMA150;
    }

    public void setSMA150(double SMA150) {
        this.SMA150 = SMA150;
    }

    public Stock(String ticker, float price, double RSI, String trend, String pattern, double sma50, double sma150, Date timeStamp) {
        this.Ticker = ticker;
        this.Price = price;
        this.RSI = RSI;
        this.Trend = trend;
        this.Pattern = pattern;
        this.TimeStamp = timeStamp;
        this.SMA150=sma150;
        this.SMA50=sma50;
    }

    public Stock(String Ticker) {
        this.Ticker = Ticker;
        this.Trend = "-";
        this.Pattern = "-";
    }

    public Stock() {
    }

    public Date getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        TimeStamp = timeStamp;
    }

    // Setters - חובה כדי ש-Jackson יוכל להכניס נתונים לאובייקט
    public void setTicker(String ticker) { Ticker = ticker; }
    public void setPrice(float price) { Price = price; }
    public void setRSI(double rsi) { RSI = rsi; }
    public void setTrend(String trend) { Trend = trend; }
    public void setPattern(String pattern) { Pattern = pattern; }

    // Getters
    public String getTicker() { return Ticker; }
    public float getPrice() { return Price; }
    public double getRSI() { return RSI; }
    public String getTrend() { return Trend; }
    public String getPattern() { return Pattern; }
}