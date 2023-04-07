public class IcebergOrder extends Order {
    IcebergOrder(OrderAction orderAction, StockTrader stockTrader, String ticker, int quantity, double price, Exchange exchange) {
        super(orderAction, stockTrader, ticker, quantity, price, exchange);
    }

    @Override
    public String toString() {
        return "IcebergOrder" + super.toString();
    }

    @Override
    public String displayOnExchange() {
        return getOrderAction().name() + " price: " + getPrice() + " quantity: " + getQuantity();
    }
}
