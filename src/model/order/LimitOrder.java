package model.order;

import model.exchange.Exchange;
import model.stocktrader.StockTrader;
import utils.Utils;

import java.util.Date;

public class LimitOrder extends Order {
    // Constructor for a limit order generated online
    public LimitOrder(OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange) {
        super(orderAction, stockTrader, ticker, quantity, price, exchange);
    }

    // Constructor for a limit order retrieved from database
    public LimitOrder(long id, OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange, Date date) {
        super(id, orderAction, stockTrader, ticker, quantity, price, exchange, date);
    }

    @Override
    public String toString() {
        return "LimitOrder" + super.toString();
    }

    @Override
    public void displayOnExchange() {
        System.out.println(orderAction + " price=" + Utils.format_decimal(price, "#.###") + " quantity=" + quantity);
    }
}
