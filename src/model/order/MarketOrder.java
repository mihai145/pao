package model.order;

import exceptions.NoDataFoundForCompanyException;
import model.exchange.Exchange;
import model.stocktrader.StockTrader;

import java.util.Date;

public class MarketOrder extends Order {
    // Constructor for a market order generated online
    public MarketOrder(OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, Exchange exchange) throws NoDataFoundForCompanyException {
        super(orderAction, stockTrader, ticker, quantity, getMarketPrice(orderAction, ticker, exchange), exchange);
    }

    // Constructor for a market order retrieved from database
    public MarketOrder(long id, OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange, Date date) {
        super(id, orderAction, stockTrader, ticker, quantity, price, exchange, date);
    }

    // Static method to automatically set the price of a market order, based on the current price of the company on the given exchange
    static double getMarketPrice(OrderAction orderAction, String ticker, Exchange exchange) throws NoDataFoundForCompanyException {
        OrderAction inverseAction = (orderAction == OrderAction.BUY) ? OrderAction.SELL : OrderAction.BUY;
        return exchange.getMarketPrice(inverseAction, ticker) + (orderAction == OrderAction.BUY ? 0.000001 : -0.000001);
    }

    @Override
    public void deductQuantity(int q) {
        this.quantity -= q;

        try {
            this.price = getMarketPrice(orderAction, ticker, exchange);
        } catch (Exception ignored) {
            // we should get an exception - if we deduct quantity from this order, a transaction must have had occurred
        }
    }

    @Override
    public String toString() {
        return "MarketOrder" + super.toString();
    }

    @Override
    public void displayOnExchange() {
    }
}
