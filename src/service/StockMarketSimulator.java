package service;

import model.company.Company;
import model.exchange.Exchange;
import model.order.Order;
import model.order.OrderAction;
import model.order.OrderType;
import model.stocktrader.StockTrader;
import utils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;

public class StockMarketSimulator {
    private static StockMarketSimulator instance;
    private final ArrayList<Company> companies;
    private final ArrayList<Exchange> exchanges;
    private final ArrayList<StockTrader> stockTraders;

    private StockMarketSimulator() {
        companies = new ArrayList<>();
        exchanges = new ArrayList<>();
        stockTraders = new ArrayList<>();
    }

    public static StockMarketSimulator getInstance() {
        if (instance == null) instance = new StockMarketSimulator();
        return instance;
    }

    public void simulate_manual() {
        Company apple = new Company("Apple", "AAPL"),
                google = new Company("Google", "GOOG"),
                tesla = new Company("Tesla", "TLSA");

        Exchange nyse = new Exchange("NYSE"),
                nasdaq = new Exchange("NASDAQ");

//        apple.listOn(nyse);
//        google.listOn(nyse);
//        tesla.listOn(nasdaq);

        StockTrader warrenBuffet = new StockTrader("Warren Buffet"),
                johnPaulson = new StockTrader("John Paulson");

        try {
            warrenBuffet.placeOrder(OrderType.LIMIT, OrderAction.BUY, nyse, apple, 100., 10);
            warrenBuffet.placeOrder(OrderType.LIMIT, OrderAction.BUY, nyse, apple, 102, 20);
            johnPaulson.placeOrder(OrderType.LIMIT, OrderAction.SELL, nyse, apple, 100, 40);
        } catch (Exception e) {
            System.out.println("Error occured: " + e);
        }

        nyse.showTransactions(tesla);
        nyse.showTransactions(google);
        nyse.showTransactions(apple);

        nyse.showOrders(apple);

        johnPaulson.showActiveOrders();

        try {
            johnPaulson.cancelOrder(johnPaulson.getActiveOrders().get(0));
            johnPaulson.showActiveOrders();
        } catch (Exception ignored) {
            System.out.println("Error occured");
        }
    }

    public StockMarketState simulate_automatic(int cntCompanies, int cntStockTraders, int cntExchanges, int cntListings, int cntOrders, double cancellationProb) {
        // generate random companies
        for (int i = 0; i < cntCompanies; i++) {
            companies.add(new Company(Utils.random_string(10), Utils.random_string(4)));
        }

        // generate random stock traders
        for (int i = 0; i < cntStockTraders; i++) {
            stockTraders.add(new StockTrader(Utils.random_string(10)));
        }

        // generate random exchanges
        for (int i = 0; i < cntExchanges; i++) {
            exchanges.add(new Exchange(Utils.random_string(10)));
        }

        // list companies on some exchanges
        for (Company c : companies) {
            for (int i = 0; i < cntListings; i++) {
                int rnd = (int) Math.floor(Math.random() * cntExchanges);
                // c.listOn(exchanges.get(rnd));
            }
        }

        for (int i = 0; i < cntOrders; i++) {
            // generate a random order
            generateRandomOrder(cntStockTraders, cntCompanies, cntExchanges);

            // cancel a random order with probability cancellationProb
            if (Math.random() <= cancellationProb) {
                // cancelRandomOrder(cntStockTraders);
            }
        }

        // show transactions for all companies
        for (Company c : companies) {
            for (Exchange e : exchanges) {
                if (c.isListedOn(e)) {
                    e.showTransactions(c);
                }
            }
        }

        // show active orders on exchanges
        for (Company c : companies) {
            for (Exchange e : exchanges) {
                if (c.isListedOn(e)) {
                    e.showOrders(c);
                }
            }
        }

        // show active orders for stock traders
        for (StockTrader t : stockTraders) {
            t.showActiveOrders();
        }

        // show market price evolution for companies
        for (Company c : companies) {
            c.graphMarketPriceEvolution();
        }

        return new StockMarketState(exchanges, companies, stockTraders);
    }

    private void generateRandomOrder(int cntStockTraders, int cntCompanies, int cntExchanges) {
        OrderType ot = Math.random() < 0.33 ? OrderType.LIMIT : (Math.random() < 0.33 ? OrderType.ICEBERG : OrderType.MARKET);
        OrderAction oa = Math.random() < 0.5 ? OrderAction.BUY : OrderAction.SELL;

        int stockTraderIdx = (int) Math.floor(Math.random() * cntStockTraders);
        int companyIdx = (int) Math.floor(Math.random() * cntCompanies);
        int exchangeIdx = (int) Math.floor(Math.random() * cntExchanges);

        double price = Math.random() * 100;
        int quantity = 1 + (int) Math.floor(Math.random() * 100);

        try {
            stockTraders
                    .get(stockTraderIdx)
                    .placeOrder(ot, oa,
                            exchanges.get(exchangeIdx),
                            companies.get(companyIdx),
                            price,
                            quantity);
        } catch (Exception ignored) {
            System.out.println("Error occured");
        }
    }

    private void cancelRandomOrder(int cntStockTraders) throws SQLException {
        int stockTraderIdx = (int) Math.floor(Math.random() * cntStockTraders);

        ArrayList<Order> activeOrders = stockTraders.get(stockTraderIdx).getActiveOrders();
        if (activeOrders.size() == 0) return;

        int orderIdx = (int) Math.floor(Math.random() * activeOrders.size());
        stockTraders
                .get(stockTraderIdx)
                .cancelOrder(activeOrders.get(orderIdx));
    }
}
