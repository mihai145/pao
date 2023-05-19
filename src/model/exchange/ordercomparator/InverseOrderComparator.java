package model.exchange.ordercomparator;

import model.order.Order;

public class InverseOrderComparator implements java.util.Comparator<Order> {
    private static final OrderComparator comp = new OrderComparator();

    @Override
    public int compare(Order o1, Order o2) {
        return -1 * comp.compare(o1, o2);
    }
}