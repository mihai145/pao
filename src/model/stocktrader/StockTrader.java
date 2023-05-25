package model.stocktrader;

import database.DatabaseConnection;
import exceptions.CompanyNotListedOnExchangeException;
import exceptions.InvalidOrderTypeException;
import exceptions.NoDataFoundForCompanyException;
import model.company.Company;
import model.exchange.Exchange;
import model.order.*;

import java.sql.SQLException;
import java.util.ArrayList;

public class StockTrader {
    private final ArrayList<Order> activeOrders;
    private String name;

    public StockTrader(String name) {
        this.name = name;
        activeOrders = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws SQLException {
        DatabaseConnection.getInstance().renameStockTrader(this.name, name);
        this.name = name;
    }

    public ArrayList<Order> getActiveOrders() {
        return activeOrders;
    }

    public void appendActiveOrder(Order order) {
        activeOrders.add(order);
    }

    // place a new order
    public void placeOrder(OrderType ot, OrderAction oa, Exchange e, Company c, double price, int quantity) throws CompanyNotListedOnExchangeException, InvalidOrderTypeException, NoDataFoundForCompanyException, SQLException {
        if (!c.isListedOn(e)) {
            throw new CompanyNotListedOnExchangeException(c.getName() + " is not listed on " + e.getName());
        }

        // instantiate the correct order type
        Order order;
        switch (ot) {
            case LIMIT -> order = new LimitOrder(oa, this, c.getTicker(), quantity, price, e);
            case ICEBERG -> order = new IcebergOrder(oa, this, c.getTicker(), quantity, price, e);
            case MARKET -> order = new MarketOrder(oa, this, c.getTicker(), quantity, e);
            default -> throw new InvalidOrderTypeException("Order type " + ot + " does not exist");
        }

        DatabaseConnection.getInstance().addOrder(order);
        activeOrders.add(order);
        e.addOrder(order);
    }

    // show active orders for this stock trader
    public void showActiveOrders() {
        if (activeOrders.size() == 0) {
            System.out.println(name + " has no active orders");
            return;
        }

        System.out.println(name + "'s active orders:");
        for (int i = 0; i < activeOrders.size(); i++) {
            System.out.println((i + 1) + ". " + activeOrders.get(i).toString());
        }
    }

    // cancel an order
    public void cancelOrder(Order order) throws SQLException {
        if (!activeOrders.contains(order)) {
            System.out.println("Invalid order");
            return;
        }

        DatabaseConnection.getInstance().removeOrder(order);
        order.cancel();
        activeOrders.remove(order);
    }

    // mark an order as complete
    public void completeOrder(Order order) throws SQLException {
        DatabaseConnection.getInstance().removeOrder(order);
        activeOrders.remove(order);
    }
}
