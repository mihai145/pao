package service;

import audit.Audit;
import database.DatabaseConnection;
import model.company.Company;
import model.exchange.Exchange;
import model.order.Order;
import model.order.OrderAction;
import model.order.OrderType;
import model.stocktrader.StockTrader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.IntStream;

// Singleton service class
public class StockMarketService {
    static private StockMarketService instance = null;
    private final Scanner scanner;
    private final DatabaseConnection connection;
    private final Audit audit;
    private ArrayList<Exchange> exchanges;
    private ArrayList<Company> companies;
    private ArrayList<StockTrader> stockTraders;

    private StockMarketService() throws SQLException, IOException {
        this.scanner = new Scanner(System.in);
        this.connection = DatabaseConnection.getInstance();
        this.audit = Audit.getInstance();
        this.exchanges = new ArrayList<>();
        this.companies = new ArrayList<>();
        this.stockTraders = new ArrayList<>();
    }

    // start the service with current data
    public static StockMarketService getStockMarketService() throws SQLException, IOException {
        if (instance == null) instance = new StockMarketService();
        instance.loadState(instance.connection.getState());
        return instance;
    }

    // start the service with a given state
    public static StockMarketService getStockMarketService(StockMarketState state) throws SQLException, IOException {
        if (instance == null) instance = new StockMarketService();
        instance.loadState(state);
        return instance;
    }

    private void loadState(StockMarketState state) {
        this.exchanges = state.exchanges();
        this.companies = state.companies();
        this.stockTraders = state.stockTraders();
    }

    // parses user command
    private ServiceCommand getCommand() {
        System.out.println("Available commands:");
        for (ServiceCommand c : ServiceCommand.values()) {
            System.out.println(c.getIdx() + ". " + c);
        }

        int choice;
        do {
            System.out.println("Your choice:");
            choice = scanner.nextInt();
        } while (choice <= 0 || choice > ServiceCommand.count());

        for (ServiceCommand c : ServiceCommand.values()) {
            if (c.getIdx() == choice) {
                return c;
            }
        }

        return ServiceCommand.QUIT;
    }

    // main loop
    public void runService() {
        while (true) {
            // parse user command
            ServiceCommand command = getCommand();
            audit.logCommand(command);

            // quit the interactive menu and close the resources
            if (command == ServiceCommand.QUIT) {
                try {
                    connection.close();
                    audit.close();
                } catch (Exception exception) {
                    System.out.println("Exception occurred while closing resources: " + exception.getMessage());
                }

                System.out.println("Service shut down");
                break;
            }

            // handle commands
            switch (command) {
                case ADD_EXCHANGE -> handleAddExchange();
                case ADD_COMPANY -> handleAddCompany();
                case ADD_STOCK_TRADER -> handleAddStockTrader();
                case RENAME_EXCHANGE -> handleRenameExchange();
                case RENAME_STOCK_TRADER -> handleRenameStockTrader();
                case LIST_COMPANY_ON_EXCHANGE -> handleListCompanyOnExchange();
                case PLACE_ORDER -> handlePlaceOrder();
                case CANCEL_ORDER -> handleCancelOrder();
                case SHOW_TRANSACTIONS_FOR_COMPANY_ON_EXCHANGE -> handleShowTransactions();
                case SHOW_ACTIVE_ORDERS_FOR_COMPANY_ON_EXCHANGE -> handleShowActiveOrdersExchange();
                case SHOW_ACTIVE_ORDERS_FOR_STOCK_TRADER -> handleShowActiveOrdersStockTrader();
                case SHOW_MARKET_PRICE_EVOLUTION_FOR_COMPANY -> handleShowMarketPriceEvolution();
            }
        }
    }

    // adds a new exchange
    private void handleAddExchange() {
        System.out.println("Exchange name:");
        String name = scanner.next();

        try {
            connection.addExchange(name);
            exchanges.add(new Exchange(name));
            System.out.println("Exchange added");
        } catch (SQLException exception) {
            System.out.println("Error adding the exchange: " + exception.getMessage());
        }
    }

    // adds a new company
    private void handleAddCompany() {
        System.out.println("Company name:");
        String name = scanner.next();
        System.out.println("Company ticker");
        String ticker = scanner.next();

        try {
            connection.addCompany(name, ticker);
            companies.add(new Company(name, ticker));
            System.out.println("Company added");
        } catch (SQLException exception) {
            System.out.println("Error adding the company: " + exception.getMessage());
        }
    }

    // adds a new stock trader
    private void handleAddStockTrader() {
        System.out.println("Stock trader name:");
        String name = scanner.next();

        try {
            connection.addStockTrader(name);
            stockTraders.add(new StockTrader(name));
            System.out.println("Stock trader added");
        } catch (SQLException exception) {
            System.out.println("Error adding the stock trader: " + exception.getMessage());
        }
    }

    // rename an exchange
    private void handleRenameExchange() {
        if (exchanges.size() == 0) {
            System.out.println("There are no exchanges");
            return;
        }

        // choose the exchange to rename
        Exchange e = chooseExchange();
        System.out.println("New name: ");
        String newName = scanner.next();

        try {
            e.setName(newName);
            System.out.println("Exchange renamed!");
        } catch (SQLException exception) {
            System.out.println("Error while renaming exchange: " + exception.getMessage());
        }
    }

    // rename a stock trader
    private void handleRenameStockTrader() {
        if (stockTraders.size() == 0) {
            System.out.println("There are no stock traders");
            return;
        }

        // choose the stocktrader to rename
        StockTrader t = chooseStockTrader();
        System.out.println("New name: ");
        String newName = scanner.next();

        try {
            t.setName(newName);
            System.out.println("Stocktrader renamed!");
        } catch (SQLException exception) {
            System.out.println("Error while renaming stock trader: " + exception.getMessage());
        }
    }

    // lists a company on an exchange
    private void handleListCompanyOnExchange() {
        if (companies.size() == 0) {
            System.out.println("There are no companies");
            return;
        }
        if (exchanges.size() == 0) {
            System.out.println("There are no exchanges");
            return;
        }

        // choose the company you want to list and the desired exchange
        Company c = chooseCompany();
        Exchange e = chooseExchange();

        try {
            c.listOn(e);
            System.out.println(c.getName() + " listed on " + e.getName());
        } catch (SQLException exception) {
            System.out.println("Error while listing on exchange: " + exception.getMessage());
        }
    }

    // places an order
    private void handlePlaceOrder() {
        if (stockTraders.size() == 0) {
            System.out.println("There are no stock traders");
            return;
        }
        if (companies.size() == 0) {
            System.out.println("There are no companies");
            return;
        }
        if (exchanges.size() == 0) {
            System.out.println("There are no exchanges");
            return;
        }

        // choose the trader, company and exchange on which to place the order
        StockTrader t = chooseStockTrader();
        Company c = chooseCompany();
        Exchange e = chooseExchange();

        // configure the order type and action
        OrderType ot = chooseOrderType();
        OrderAction oa = chooseOrderAction();

        // configure the order price and quantity
        double price = 0.;
        if (ot != OrderType.MARKET) price = choosePrice();
        int quantity = chooseQuantity();

        try {
            t.placeOrder(ot, oa, e, c, price, quantity);
            System.out.println("Order placed");
        } catch (Exception exception) {
            System.out.println("Error while adding order: " + exception.getMessage());
        }
    }

    // cancels an order
    private void handleCancelOrder() {
        if (stockTraders.size() == 0) {
            System.out.println("There are no stock traders");
            return;
        }

        // choose the stock trader whose order you want to cancel
        StockTrader t = chooseStockTrader();
        if (t.getActiveOrders().size() == 0) {
            System.out.println(t.getName() + " has no active orders");
            return;
        }

        // choose the desired order amongst the stock trader's orders
        Order o = chooseOrder(t);
        try {
            t.cancelOrder(o);
            System.out.println("model.Order.Order cancelled");
        } catch (SQLException exception) {
            System.out.println("Error while cancelling order: " + exception.getMessage());
        }
    }

    // shows all transactions for a company on an exchange
    private void handleShowTransactions() {
        if (companies.size() == 0) {
            System.out.println("There are no companies");
            return;
        }
        if (exchanges.size() == 0) {
            System.out.println("There are no exchanges");
            return;
        }

        // choose the desired company and exchange
        Company c = chooseCompany();
        Exchange e = chooseExchange();

        e.showTransactions(c);
    }

    // show active orders for a given company on a given exchange
    private void handleShowActiveOrdersExchange() {
        if (companies.size() == 0) {
            System.out.println("There are no companies");
            return;
        }
        if (exchanges.size() == 0) {
            System.out.println("There are no exchanges");
            return;
        }

        // choose the desired company and exchange
        Company c = chooseCompany();
        Exchange e = chooseExchange();

        e.showOrders(c);
    }

    // show active orders for a given stock trader
    private void handleShowActiveOrdersStockTrader() {
        if (stockTraders.size() == 0) {
            System.out.println("There are no stock traders");
            return;
        }

        // choose the desired stock trader
        StockTrader t = chooseStockTrader();

        t.showActiveOrders();
    }

    // show the market price evolution of a given company
    private void handleShowMarketPriceEvolution() {
        if (companies.size() == 0) {
            System.out.println("There are no companies");
            return;
        }

        // choose the desired company
        Company c = chooseCompany();

        c.graphMarketPriceEvolution();
    }

    // parse user's choice of exchange
    private Exchange chooseExchange() {
        System.out.println("Choose an exchange from:");
        IntStream.range(0, exchanges.size())
                .forEach(idx -> System.out.println((idx + 1) + ". " + exchanges.get(idx).getName()));

        int exchange;
        do {
            System.out.println("Your choice:");
            exchange = scanner.nextInt();
        } while (exchange <= 0 || exchange > exchanges.size());

        return exchanges.get(exchange - 1);
    }

    // parse user's choice of company
    private Company chooseCompany() {
        System.out.println("Choose a company from:");
        IntStream.range(0, companies.size())
                .forEach(idx -> System.out.println((idx + 1) + ". " + companies.get(idx).getName()));

        int company;
        do {
            System.out.println("Your choice:");
            company = scanner.nextInt();
        } while (company <= 0 || company > companies.size());

        return companies.get(company - 1);
    }

    // parse user's choice of stock trader
    private StockTrader chooseStockTrader() {
        System.out.println("Choose a stock trader from:");
        IntStream.range(0, stockTraders.size())
                .forEach(idx -> System.out.println((idx + 1) + ". " + stockTraders.get(idx).getName()));

        int stockTrader;
        do {
            System.out.println("Your choice:");
            stockTrader = scanner.nextInt();
        } while (stockTrader <= 0 || stockTrader > stockTraders.size());

        return stockTraders.get(stockTrader - 1);
    }

    // parse user's choice of order from a given stock trader
    private Order chooseOrder(StockTrader t) {
        System.out.println("Choose an order from:");
        IntStream.range(0, t.getActiveOrders().size())
                .forEach(idx -> System.out.println((idx + 1) + ". " + t.getActiveOrders().get(idx).toString()));

        int order;
        do {
            System.out.println("Your choice:");
            order = scanner.nextInt();
        } while (order <= 0 || order > t.getActiveOrders().size());

        return t.getActiveOrders().get(order - 1);
    }

    // parse user's choice of order type
    private OrderType chooseOrderType() {
        System.out.println("Choose order type from:");
        for (OrderType ot : OrderType.values()) {
            System.out.println(ot);
        }

        String choice;
        do {
            System.out.println("Your choice:");
            choice = scanner.next();
            for (OrderType ot : OrderType.values()) {
                if (ot.toString().equals(choice)) return ot;
            }
        } while (true);
    }

    // parse user's choice of order action
    private OrderAction chooseOrderAction() {
        System.out.println("Choose order action from:");
        for (OrderAction oa : OrderAction.values()) {
            System.out.println(oa);
        }

        String choice;
        do {
            System.out.println("Your choice:");
            choice = scanner.next();
            for (OrderAction oa : OrderAction.values()) {
                if (oa.toString().equals(choice)) return oa;
            }
        } while (true);
    }

    // parse user's choice of price
    private double choosePrice() {
        double price;
        do {
            System.out.println("Price (>0): ");
            price = scanner.nextDouble();
        } while (price <= 0);

        return price;
    }

    // parse user's choice of quantity
    private int chooseQuantity() {
        int quantity;
        do {
            System.out.println("Quantity (>=1): ");
            quantity = scanner.nextInt();
        } while (quantity < 1);

        return quantity;
    }
}
