package model.order;

import model.exchange.Exchange;
import model.stocktrader.StockTrader;

public class IcebergOrder extends Order {
    public IcebergOrder(OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange) {
        super(orderAction, stockTrader, ticker, quantity, price, exchange);
    }

    @Override
    public String toString() {
        return "model.Order.Order.IcebergOrder" + super.toString();
    }

    @Override
    public String displayOnExchange() {
        return getOrderAction().name() + " price: " + getPrice() + " quantity: " + getQuantity();
    }
}