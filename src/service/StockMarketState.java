package service;

import model.company.Company;
import model.exchange.Exchange;
import model.stocktrader.StockTrader;

import java.util.ArrayList;

public record StockMarketState(ArrayList<Exchange> exchanges, ArrayList<Company> companies,
                               ArrayList<StockTrader> stockTraders) {
}
