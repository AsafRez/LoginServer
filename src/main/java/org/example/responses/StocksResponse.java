package org.example.responses;

import org.example.Classes.Stock;

import java.util.List;

public class StocksResponse extends  BasicResponse{
    private List<Stock> stocks;

    public StocksResponse(boolean success, Integer errorCode, List<Stock> stocks) {
        super(success, errorCode);
        this.stocks = stocks;
    }

    public List<Stock> getStocks() {
        return stocks;
    }
}
