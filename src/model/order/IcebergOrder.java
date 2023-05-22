package model.order;

import model.exchange.Exchange;
import model.stocktrader.StockTrader;

import java.util.Date;

public class IcebergOrder extends Order {
    // Constructor for an iceberg order generated online
    public IcebergOrder(OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange) {
        super(orderAction, stockTrader, ticker, quantity, price, exchange);
    }

    // Constructor for an iceberg order retrieved from database
    public IcebergOrder(long id, OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange, Date date) {
        super(id, orderAction, stockTrader, ticker, quantity, price, exchange, date);
    }

    @Override
    public String toString() {
        return "IcebergOrder" + super.toString();
    }

    @Override
    public void displayOnExchange() { // iceberg orders do not show info regarding price and quantity
        System.out.println(orderAction);
    }
}
