import java.util.Date;

public abstract class Order {
    private static long stamp; // unique auto-incrementing id assigned to all orders
    private final OrderAction orderAction;
    private final StockTrader stockTrader;
    private final String ticker;
    private int quantity; // may be modified by splitting the order
    private final double price;
    private final Exchange exchange;
    private final Date date;
    private final long id;

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

    public OrderAction getOrderAction() {
        return orderAction;
    }

    public StockTrader getStockTrader() { return stockTrader; }

    public String getTicker() {
        return ticker;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public long getId() { return id; }

    public void deductQuantity(int q) { this.quantity -= q; }

    public void cancel() {
        exchange.cancel(this);
    }

    public void notifyCompleted() {
        stockTrader.completeOrder(this);
    }

    @Override
    public String toString() {
        return "[date=" + date + ", trader=" + stockTrader.getName() + ", ticker=" + ticker
                + ", price=" + price + ", quantity" + quantity
                + ", listed on=" + exchange.getName() + "]";
    }

    public abstract String displayOnExchange();
}
