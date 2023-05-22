package service;

import model.company.Company;
import model.exchange.Exchange;
import model.stocktrader.StockTrader;

import java.util.ArrayList;

// Record class for a given state of the stock market
public record StockMarketState(ArrayList<Exchange> exchanges, ArrayList<Company> companies,
                               ArrayList<StockTrader> stockTraders) {
}
