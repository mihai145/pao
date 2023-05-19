package service;

import model.company.Company;
import model.exchange.Exchange;
import model.order.Order;
import model.order.OrderAction;
import model.order.OrderType;
import model.stocktrader.StockTrader;

import java.util.ArrayList;
import java.util.Scanner;

public class StockMarketService {
    static private StockMarketService instance = null;
    private final Scanner scanner;
    private ArrayList<Exchange> exchanges;
    private ArrayList<Company> companies;
    private ArrayList<StockTrader> stockTraders;

    private StockMarketService() {
        this.scanner = new Scanner(System.in);
        this.exchanges = new ArrayList<>();
        this.companies = new ArrayList<>();
        this.stockTraders = new ArrayList<>();
    }

    static StockMarketService getStockMarketService() {
        if (instance == null) instance = new StockMarketService();
        instance.clearState();
        return instance;
    }

    public static StockMarketService getStockMarketService(StockMarketState state) {
        if (instance == null) instance = new StockMarketService();
        instance.loadSimulation(state);
        return instance;
    }

    private void clearState() {
        this.exchanges.clear();
        this.companies.clear();
        this.stockTraders.clear();
    }

    private void loadSimulation(StockMarketState state) {
        this.exchanges = state.exchanges();
        this.companies = state.companies();
        this.stockTraders = state.stockTraders();
    }

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

    public void runService() {
        while (true) {
            ServiceCommand command = getCommand();

            if (command == ServiceCommand.QUIT) {
                System.out.println("Service shut down");
                break;
            }

            switch (command) {
                case ADD_EXCHANGE -> handleAddExchange();
                case ADD_COMPANY -> handleAddCompany();
                case ADD_STOCK_TRADER -> handleAddStockTrader();
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

    private void handleAddExchange() {
        System.out.println("model.Exchange.Exchange name:");
        String name = scanner.next();
        exchanges.add(new Exchange(name));
        System.out.println("model.Exchange.Exchange added");
    }

    private void handleAddCompany() {
        System.out.println("model.Company.Company name:");
        String name = scanner.next();
        System.out.println("model.Company.Company ticker");
        String ticker = scanner.next();
        companies.add(new Company(name, ticker));
        System.out.println("model.Company.Company added");
    }

    private void handleAddStockTrader() {
        System.out.println("Stock trader name:");
        String name = scanner.next();
        stockTraders.add(new StockTrader(name));
        System.out.println("Stock trader added");
    }

    private void handleListCompanyOnExchange() {
        if (companies.size() == 0) {
            System.out.println("There are no companies");
            return;
        }
        if (exchanges.size() == 0) {
            System.out.println("There are no exchanges");
            return;
        }

        Company c = chooseCompany();
        Exchange e = chooseExchange();
        c.listOn(e);
        System.out.println(c.getName() + " listed on " + e.getName());
    }

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

        StockTrader t = chooseStockTrader();

        OrderType ot = chooseOrderType();
        OrderAction oa = chooseOrderAction();
        Company c = chooseCompany();
        Exchange e = chooseExchange();

        double price = 0.;
        if (ot != OrderType.MARKET) {
            price = choosePrice();
        }
        int quantity = chooseQuantity();

        try {
            t.placeOrder(ot, oa, e, c, price, quantity);
            System.out.println("model.Order.Order placed");
        } catch (Exception err) {
            System.out.println("Error: " + err);
        }
    }

    private void handleCancelOrder() {
        if (stockTraders.size() == 0) {
            System.out.println("There are no stock traders");
            return;
        }

        StockTrader t = chooseStockTrader();
        if (t.getActiveOrders().size() == 0) {
            System.out.println(t.getName() + " has no active orders");
            return;
        }

        Order o = chooseOrder(t);
        t.cancelOrder(o);
        System.out.println("model.Order.Order cancelled");
    }

    private void handleShowTransactions() {
        if (companies.size() == 0) {
            System.out.println("There are no companies");
            return;
        }
        if (exchanges.size() == 0) {
            System.out.println("There are no exchanges");
            return;
        }

        Company c = chooseCompany();
        Exchange e = chooseExchange();
        e.showTransactions(c);
    }

    private void handleShowActiveOrdersExchange() {
        if (companies.size() == 0) {
            System.out.println("There are no companies");
            return;
        }
        if (exchanges.size() == 0) {
            System.out.println("There are no exchanges");
            return;
        }

        Company c = chooseCompany();
        Exchange e = chooseExchange();
        e.showOrders(c);
    }

    private void handleShowActiveOrdersStockTrader() {
        if (stockTraders.size() == 0) {
            System.out.println("There are no stock traders");
            return;
        }

        StockTrader t = chooseStockTrader();
        t.showActiveOrders();
    }

    private void handleShowMarketPriceEvolution() {
        if (companies.size() == 0) {
            System.out.println("There are no companies");
            return;
        }

        Company c = chooseCompany();
        c.graphMarketPriceEvolution();
    }

    private Exchange chooseExchange() {
        System.out.println("Choose an exchange from:");
        for (int i = 0; i < exchanges.size(); i++) {
            System.out.println((i + 1) + ". " + exchanges.get(i).getName());
        }

        int exchange;
        do {
            System.out.println("Your choice:");
            exchange = scanner.nextInt();
        } while (exchange <= 0 || exchange > exchanges.size());

        return exchanges.get(exchange - 1);
    }

    private Company chooseCompany() {
        System.out.println("Choose a company from:");
        for (int i = 0; i < companies.size(); i++) {
            System.out.println((i + 1) + ". " + companies.get(i).getName() + " - " + companies.get(i).getTicker());
        }

        int company;
        do {
            System.out.println("Your choice:");
            company = scanner.nextInt();
        } while (company <= 0 || company > companies.size());

        return companies.get(company - 1);
    }

    private StockTrader chooseStockTrader() {
        System.out.println("Choose a stock trader from:");
        for (int i = 0; i < stockTraders.size(); i++) {
            System.out.println((i + 1) + ". " + stockTraders.get(i).getName());
        }

        int stockTrader;
        do {
            System.out.println("Your choice:");
            stockTrader = scanner.nextInt();
        } while (stockTrader <= 0 || stockTrader > stockTraders.size());

        return stockTraders.get(stockTrader - 1);
    }

    private Order chooseOrder(StockTrader t) {
        System.out.println("Choose an order from:");
        for (int i = 0; i < t.getActiveOrders().size(); i++) {
            System.out.println((i + 1) + ". " + t.getActiveOrders().get(i).toString());
        }

        int order;
        do {
            System.out.println("Your choice:");
            order = scanner.nextInt();
        } while (order <= 0 || order > t.getActiveOrders().size());

        return t.getActiveOrders().get(order - 1);
    }

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

    private double choosePrice() {
        double price;
        do {
            System.out.println("Price (>0): ");
            price = scanner.nextDouble();
        } while (price <= 0);

        return price;
    }

    private int chooseQuantity() {
        int quantity;
        do {
            System.out.println("Quantity (>=1): ");
            quantity = scanner.nextInt();
        } while (quantity < 1);

        return quantity;
    }
}
