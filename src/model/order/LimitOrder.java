package model.order;

import model.exchange.Exchange;
import model.stocktrader.StockTrader;

import java.util.Date;

public class LimitOrder extends Order {
    public LimitOrder(OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange) {
        super(orderAction, stockTrader, ticker, quantity, price, exchange);
    }

    public LimitOrder(long id, OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange, Date date) {
        super(id, orderAction, stockTrader, ticker, quantity, price, exchange, date);
    }

    @Override
    public String toString() {
        return "LimitOrder" + super.toString();
    }

    @Override
    public String displayOnExchange() {
        return getOrderAction().name() + " price: " + getPrice() + " quantity: " + getQuantity();
    }
}
