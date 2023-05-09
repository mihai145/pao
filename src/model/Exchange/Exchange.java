package model.Exchange;

import exceptions.NoDataFoundForCompanyException;
import model.Company.Company;
import model.Order.Order;
import model.Order.OrderAction;
import utils.Utils;

import java.util.*;

class OrderComparator implements java.util.Comparator<Order> {
    @Override
    public int compare(Order o1, Order o2) {
        if (o1.getPrice() > o2.getPrice()) {
            return -1;
        }
        if (o1.getPrice() < o2.getPrice()) {
            return 1;
        }

        return Long.compare(o1.getId(), o2.getId());
    }
}

class InverseOrderComparator implements java.util.Comparator<Order> {
    private static final OrderComparator comp = new OrderComparator();

    @Override
    public int compare(Order o1, Order o2) {
        return -1 * comp.compare(o1, o2);
    }
}

public class Exchange {
    public String name;
    HashMap<String, PriorityQueue<Order>> buyOrders, sellOrders;
    HashMap<String, ArrayList<Transaction>> transactionsFor;

    public Exchange(String name) {
        this.name = name;
        buyOrders = new HashMap<>();
        sellOrders = new HashMap<>();
        transactionsFor = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Transaction> getTransactionsFor(String ticker) {
        if (!transactionsFor.containsKey(ticker)) return new ArrayList<>();
        return transactionsFor.get(ticker);
    }

    public void addOrder(Order order) {
        if (order.getOrderAction() == OrderAction.BUY) {
            if (!buyOrders.containsKey(order.getTicker())) {
                buyOrders.put(order.getTicker(), new PriorityQueue<>(new OrderComparator()));
            }
            buyOrders.get(order.getTicker()).add(order);
        } else {
            if (!sellOrders.containsKey(order.getTicker())) {
                sellOrders.put(order.getTicker(), new PriorityQueue<>(new InverseOrderComparator()));
            }
            sellOrders.get(order.getTicker()).add(order);
        }

        match(order.getTicker(), order.getOrderAction());
    }

    private void match(String ticker, OrderAction oa) {
        if (!buyOrders.containsKey(ticker) || !sellOrders.containsKey(ticker)) return;

        while (buyOrders.get(ticker).size() > 0 && sellOrders.get(ticker).size() > 0) {
            Order buyOrder = buyOrders.get(ticker).peek(), sellOrder = sellOrders.get(ticker).peek();
            if (buyOrder.getPrice() >= sellOrder.getPrice()) {
                // remove orders from the order book
                buyOrders.get(ticker).poll();
                sellOrders.get(ticker).poll();

                // calculate matched price and quantity; generate a transaction
                double price = (oa == OrderAction.BUY) ? sellOrder.getPrice() : buyOrder.getPrice();
                int quantity = Math.min(sellOrder.getQuantity(), buyOrder.getQuantity());
                addTransaction(ticker, sellOrder.getStockTrader().getName(), buyOrder.getStockTrader().getName(), price, quantity);

                // deduct the matched quantity from both orders; reinsert them in the order book if they are not fulfilled
                buyOrder.deductQuantity(quantity);
                sellOrder.deductQuantity(quantity);
                if (buyOrder.getQuantity() > 0) {
                    buyOrders.get(ticker).add(buyOrder);
                } else {
                    buyOrder.notifyCompleted();
                }
                if (sellOrder.getQuantity() > 0) {
                    sellOrders.get(ticker).add(sellOrder);
                } else {
                    sellOrder.notifyCompleted();
                }
            } else {
                break;
            }
        }
    }

    private void addTransaction(String ticker, String from, String to, double price, int quantity) {
        if (!transactionsFor.containsKey(ticker)) {
            transactionsFor.put(ticker, new ArrayList<>());
        }
        transactionsFor.get(ticker).add(new Transaction(new Date(), from, to, price, quantity));
    }

    public void cancel(Order order) {
        if (order.getOrderAction() == OrderAction.BUY) {
            buyOrders.get(order.getTicker()).remove(order);
        } else {
            sellOrders.get(order.getTicker()).remove(order);
        }
    }

    public void showTransactions(Company c) {
        Utils.output_separator();

        if (!c.isListedOn(this)) {
            System.out.println("model.Company.Company " + c.getName() + " is not listed on " + name);
            return;
        }

        if (!transactionsFor.containsKey(c.getTicker())) {
            System.out.println("No transactions for " + c.getName() + " on " + name);
            return;
        }

        ArrayList<Transaction> list = transactionsFor.get(c.getTicker());
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ". " + list.get(i).toString());
        }
    }

    public void showOrders(Company c) {
        Utils.output_separator();

        if (!buyOrders.containsKey(c.getTicker()) || buyOrders.get(c.getTicker()).size() == 0) {
            System.out.println("No buy orders for " + c.getName() + " on " + name);
        } else {
            System.out.println("Buy orders for " + c.getName() + " on " + name + ":");

            int idx = 0;
            for (Order o : buyOrders.get(c.getTicker())) {
                System.out.println((idx + 1) + ". " + o.displayOnExchange());
            }
        }

        if (!sellOrders.containsKey(c.getTicker()) || sellOrders.get(c.getTicker()).size() == 0) {
            System.out.println("No sell orders for " + c.getName() + " on " + name);
        } else {
            System.out.println("Sell orders for " + c.getName() + " on " + name + ":");

            int idx = 0;
            for (Order o : sellOrders.get(c.getTicker())) {
                System.out.println((idx + 1) + ". " + o.displayOnExchange());
            }
        }
    }

    public double getMarketPrice(OrderAction oa, String ticker) throws NoDataFoundForCompanyException {
        if (oa == OrderAction.BUY) {
            if (!buyOrders.containsKey(ticker) || buyOrders.get(ticker).size() == 0) {
                throw new NoDataFoundForCompanyException("No data found for " + ticker);
            }
            return Objects.requireNonNull(buyOrders.get(ticker).peek()).getPrice();
        }

        if (!sellOrders.containsKey(ticker) || sellOrders.get(ticker).size() == 0) {
            throw new NoDataFoundForCompanyException("No data found for " + ticker);
        }
        return Objects.requireNonNull(sellOrders.get(ticker).peek()).getPrice();
    }
}
