package model.order;

import model.exchange.Exchange;
import model.stocktrader.StockTrader;

public class LimitOrder extends Order {
    public LimitOrder(OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange) {
        super(orderAction, stockTrader, ticker, quantity, price, exchange);
    }

    @Override
    public String toString() {
        return "model.Order.LimitOrder" + super.toString();
    }

    @Override
    public String displayOnExchange() {
        return getOrderAction().name() + " price: " + getPrice() + " quantity: " + getQuantity();
    }
}
