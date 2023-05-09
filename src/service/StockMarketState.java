package service;

import model.Company.Company;
import model.Exchange.Exchange;
import model.StockTrader.StockTrader;

import java.util.ArrayList;

public record StockMarketState(ArrayList<Exchange> exchanges, ArrayList<Company> companies,
                               ArrayList<StockTrader> stockTraders) {
}
