package model.stocktrader;

import exceptions.CompanyNotListedOnExchangeException;
import exceptions.InvalidOrderTypeException;
import exceptions.NoDataFoundForCompanyException;
import model.company.Company;
import model.exchange.Exchange;
import model.order.*;
import utils.Utils;

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

    public void placeOrder(OrderType ot, OrderAction oa, Exchange e, Company c, double price, int quantity) throws CompanyNotListedOnExchangeException, InvalidOrderTypeException, NoDataFoundForCompanyException {
        if (!c.isListedOn(e))
            throw new CompanyNotListedOnExchangeException(c.getName() + " is not listed on " + e.getName());

        Order order;
        if (ot == OrderType.LIMIT) {
            order = new LimitOrder(oa, this, c.getTicker(), quantity, price, e);
        } else if (ot == OrderType.ICEBERG) {
            order = new IcebergOrder(oa, this, c.getTicker(), quantity, price, e);
        } else if (ot == OrderType.MARKET) {
            order = new MarketOrder(oa, this, c.getTicker(), quantity, e);
        } else {
            throw new InvalidOrderTypeException("model.Order.Order type " + ot.toString() + " does not exist");
        }

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

    public void cancelOrder(Order order) {
        if (!activeOrders.contains(order)) {
            System.out.println("Invalid order");
            return;
        }

        order.cancel();
        activeOrders.remove(order);
    }

    public void completeOrder(Order order) {
        boolean res = activeOrders.remove(order);
        if (!res) throw new AssertionError();
    }
}