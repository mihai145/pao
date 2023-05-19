package model.stocktrader;

import database.DatabaseConnection;
import exceptions.CompanyNotListedOnExchangeException;
import exceptions.InvalidOrderTypeException;
import exceptions.NoDataFoundForCompanyException;
import model.company.Company;
import model.exchange.Exchange;
import model.order.*;
import utils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;

public class StockTrader {
    private final String name;
    private final ArrayList<Order> activeOrders;

    public StockTrader(String name) {
        this.name = name;
        activeOrders = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Order> getActiveOrders() {
        return activeOrders;
    }

    public void appendActiveOrder(Order order) {
        activeOrders.add(order);
    }

    public void placeOrder(OrderType ot, OrderAction oa, Exchange e, Company c, double price, int quantity) throws CompanyNotListedOnExchangeException, InvalidOrderTypeException, NoDataFoundForCompanyException, SQLException {
        if (!c.isListedOn(e)) {
            throw new CompanyNotListedOnExchangeException(c.getName() + " is not listed on " + e.getName());
        }

        Order order;
        if (ot == OrderType.LIMIT) {
            order = new LimitOrder(oa, this, c.getTicker(), quantity, price, e);
        } else if (ot == OrderType.ICEBERG) {
            order = new IcebergOrder(oa, this, c.getTicker(), quantity, price, e);
        } else if (ot == OrderType.MARKET) {
            order = new MarketOrder(oa, this, c.getTicker(), quantity, e);
        } else {
            throw new InvalidOrderTypeException("Order type " + ot.toString() + " does not exist");
        }

        DatabaseConnection.getInstance().addOrder(order);
        activeOrders.add(order);
        e.addOrder(order);
    }

    public void showActiveOrders() {
        Utils.output_separator();

        if (activeOrders.size() == 0) {
            System.out.println(name + " has no active orders");
            return;
        }

        System.out.println(name + "'s active orders:");
        for (int i = 0; i < activeOrders.size(); i++) {
            System.out.println((i + 1) + ". " + activeOrders.get(i).toString());
        }
    }

    public void cancelOrder(Order order) throws SQLException {
        if (!activeOrders.contains(order)) {
            System.out.println("Invalid order");
            return;
        }

        DatabaseConnection.getInstance().removeOrder(order);
        order.cancel();
        activeOrders.remove(order);
    }

    public void completeOrder(Order order) throws SQLException {
        DatabaseConnection.getInstance().removeOrder(order);
        activeOrders.remove(order);
    }
}
