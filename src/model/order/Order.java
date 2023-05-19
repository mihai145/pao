package model.order;

import model.exchange.Exchange;
import model.stocktrader.StockTrader;

import java.sql.SQLException;
import java.util.Date;

public abstract class Order {
    protected static long stamp; // unique auto-incrementing id assigned to all orders
    protected final OrderAction orderAction;
    protected final StockTrader stockTrader;
    protected final String ticker;
    protected final Exchange exchange;
    protected final Date date;
    protected final long id;
    protected double price;
    protected int quantity; // may be modified by splitting the order

    Order(OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange) {
        this.orderAction = orderAction;
        this.stockTrader = stockTrader;
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.exchange = exchange;
        this.date = new Date();
        this.id = ++Order.stamp;
    }

    Order(long id, OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange, Date date) {
        this.id = id;
        this.orderAction = orderAction;
        this.stockTrader = stockTrader;
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.exchange = exchange;
        this.date = date;
        Order.stamp = Math.max(Order.stamp, id);
    }

    public OrderAction getOrderAction() {
        return orderAction;
    }

    public StockTrader getStockTrader() {
        return stockTrader;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public String getTicker() {
        return ticker;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public long getId() {
        return id;
    }

    public void deductQuantity(int q) {
        this.quantity -= q;
    }

    public void cancel() {
        exchange.cancel(this);
    }

    public void notifyCompleted() throws SQLException {
        stockTrader.completeOrder(this);
    }

    @Override
    public String toString() {
        return "[date=" + date + ", trader=" + stockTrader.getName() + ", ticker=" + ticker
                + ", price=" + price + ", quantity=" + quantity
                + ", listed on=" + exchange.getName() + "]";
    }

    public abstract String displayOnExchange();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Order) {
            return id == ((Order) obj).getId();
        }
        return false;
    }
}
