import java.util.ArrayList;

record StockMarketState (ArrayList<Exchange> exchanges, ArrayList<Company> companies, ArrayList<StockTrader> stockTraders) {}
