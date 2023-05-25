package service;

import audit.Audit;
import database.DatabaseConnection;
import model.company.Company;
import model.exchange.Exchange;
import model.order.Order;
import model.order.OrderAction;
import model.order.OrderType;
import model.stocktrader.StockTrader;
import utils.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

// Singleton class for simulating the stock market
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

    // used for manual testing
    public void simulate_manual() throws SQLException {
        DatabaseConnection.getInstance().eraseAll();

        Company apple = new Company("Apple", "AAPL"),
                google = new Company("Google", "GOOG"),
                tesla = new Company("Tesla", "TLSA");

        Exchange nyse = new Exchange("NYSE"),
                nasdaq = new Exchange("NASDAQ");

        apple.listOn(nyse);
        google.listOn(nyse);
        tesla.listOn(nasdaq);

        StockTrader warrenBuffet = new StockTrader("Warren Buffet"),
                johnPaulson = new StockTrader("John Paulson");

        try {
            warrenBuffet.placeOrder(OrderType.LIMIT, OrderAction.BUY, nyse, apple, 100., 10);
            warrenBuffet.placeOrder(OrderType.LIMIT, OrderAction.BUY, nyse, apple, 102, 20);
            johnPaulson.placeOrder(OrderType.LIMIT, OrderAction.SELL, nyse, apple, 100, 40);
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
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
            System.out.println("Error occurred");
        }
    }

    public StockMarketState simulate_automatic(int cntCompanies, int cntStockTraders, int cntExchanges, int cntListings, int cntOrders, double cancellationProb) throws SQLException, IOException {
        DatabaseConnection.getInstance().eraseAll();

        Audit audit = Audit.getInstance();
        audit.logSimulation();

        // generate entities
        generateRandomCompanies(cntCompanies);
        generateRandomStockTraders(cntStockTraders);
        generateRandomExchanges(cntExchanges);

        // list companies on some exchanges
        for (Company c : companies) {
            for (int i = 0; i < cntListings; i++) {
                int rnd = (int) Math.floor(Math.random() * cntExchanges);
                c.listOn(exchanges.get(rnd));
                audit.logCommand(ServiceCommand.LIST_COMPANY_ON_EXCHANGE);
            }
        }

        // generate a random order
        for (int i = 0; i < cntOrders; i++) {
            generateRandomOrder(cntStockTraders, cntCompanies, cntExchanges);

            // cancel a random order with probability cancellationProb
            if (Math.random() <= cancellationProb) {
                cancelRandomOrder(cntStockTraders);
                audit.logCommand(ServiceCommand.CANCEL_ORDER);
            }
        }

        // show active orders on exchanges
        for (Company c : companies) {
            for (Exchange e : exchanges) {
                if (c.isListedOn(e)) {
                    e.showOrders(c);
                    audit.logCommand(ServiceCommand.SHOW_ACTIVE_ORDERS_FOR_COMPANY_ON_EXCHANGE);
                }
            }
        }

        // exports the current stock market state, to be used in the stock market service class
        return new StockMarketState(exchanges, companies, stockTraders);
    }

    // generates random companies
    private void generateRandomCompanies(int cntCompanies) {
        for (int i = 0; i < cntCompanies; i++) {
            String random_name = Utils.random_string(10), random_ticker = Utils.random_string(4);
            try {
                DatabaseConnection.getInstance().addCompany(random_name, random_ticker);
                companies.add(new Company(random_name, random_ticker));
                Audit.getInstance().logCommand(ServiceCommand.ADD_COMPANY);
            } catch (SQLException | IOException exception) {
                System.out.printf("Simulation warning: " + exception.getMessage());
            }
        }
    }

    // generates random exchanges
    private void generateRandomExchanges(int cntExchanges) {
        for (int i = 0; i < cntExchanges; i++) {
            String random_name = Utils.random_string(10);
            try {
                DatabaseConnection.getInstance().addExchange(random_name);
                exchanges.add(new Exchange(random_name));
                Audit.getInstance().logCommand(ServiceCommand.ADD_EXCHANGE);
            } catch (SQLException | IOException exception) {
                System.out.printf("Simulation warning: " + exception.getMessage());
            }
        }
    }

    // generates random stock traders
    private void generateRandomStockTraders(int cntStockTraders) {
        for (int i = 0; i < cntStockTraders; i++) {
            String random_name = Utils.random_string(10);
            try {
                DatabaseConnection.getInstance().addStockTrader(random_name);
                stockTraders.add(new StockTrader(random_name));
                Audit.getInstance().logCommand(ServiceCommand.ADD_STOCK_TRADER);
            } catch (SQLException | IOException exception) {
                System.out.printf("Simulation warning: " + exception.getMessage());
            }
        }
    }

    // generates random orders
    private void generateRandomOrder(int cntStockTraders, int cntCompanies, int cntExchanges) {
        // choose the order type and action
        OrderType ot = Math.random() < 0.33 ? OrderType.LIMIT : (Math.random() < 0.33 ? OrderType.ICEBERG : OrderType.MARKET);
        OrderAction oa = Math.random() < 0.5 ? OrderAction.BUY : OrderAction.SELL;

        // choose the stock trader, company and exchange involved
        int stockTraderIdx = (int) Math.floor(Math.random() * cntStockTraders);
        int companyIdx = (int) Math.floor(Math.random() * cntCompanies);
        int exchangeIdx = (int) Math.floor(Math.random() * cntExchanges);

        // choose the price and quantity
        double price = Math.random() * 100;
        int quantity = 1 + (int) Math.floor(Math.random() * 100);

        // try to place the order
        // it might fail in case of a market order, when there are no outstanding orders on the exchange
        try {
            stockTraders
                    .get(stockTraderIdx)
                    .placeOrder(ot, oa,
                            exchanges.get(exchangeIdx),
                            companies.get(companyIdx),
                            price,
                            quantity);
            Audit.getInstance().logCommand(ServiceCommand.PLACE_ORDER);
        } catch (Exception e) {
            System.out.println("Simulation warning: " + e.getMessage());
        }
    }

    // cancel a random order
    private void cancelRandomOrder(int cntStockTraders) throws SQLException {
        // choose the stock trader
        int stockTraderIdx = (int) Math.floor(Math.random() * cntStockTraders);

        ArrayList<Order> activeOrders = stockTraders.get(stockTraderIdx).getActiveOrders();
        if (activeOrders.size() == 0) return;

        // cancel one of his orders
        int orderIdx = (int) Math.floor(Math.random() * activeOrders.size());
        stockTraders
                .get(stockTraderIdx)
                .cancelOrder(activeOrders.get(orderIdx));
    }
}
