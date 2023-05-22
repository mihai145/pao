package model.exchange.ordercomparator;

import model.order.Order;

// Sort orders ascending by price
public class OrderComparator implements java.util.Comparator<Order> {
    @Override
    public int compare(Order o1, Order o2) {
        if (o1.getPrice() > o2.getPrice()) {
            return -1;
        }
        if (o1.getPrice() < o2.getPrice()) {
            return 1;
        }

        return Long.compare(o1.getId(), o2.getId());
    }
}