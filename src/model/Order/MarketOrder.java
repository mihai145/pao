package model.Order;

import exceptions.NoDataFoundForCompanyException;
import model.Exchange.Exchange;
import model.StockTrader.StockTrader;

public class MarketOrder extends Order {
    public MarketOrder(OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, Exchange exchange) throws NoDataFoundForCompanyException {
        super(orderAction, stockTrader, ticker, quantity, getMarketPrice(orderAction, ticker, exchange), exchange);
    }

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
        return "model.Order.MarketOrder" + super.toString();
    }

    @Override
    public String displayOnExchange() {
        return getOrderAction().name() + " price: ~" + getPrice() + " quantity: " + getQuantity();
    }
}
