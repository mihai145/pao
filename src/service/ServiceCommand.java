package service;

// All commands available to the user
public enum ServiceCommand {
    ADD_EXCHANGE(1),
    ADD_COMPANY(2),
    ADD_STOCK_TRADER(3),
    RENAME_EXCHANGE(4),
    RENAME_STOCK_TRADER(5),
    LIST_COMPANY_ON_EXCHANGE(6),
    PLACE_ORDER(7),
    CANCEL_ORDER(8),
    SHOW_TRANSACTIONS_FOR_COMPANY_ON_EXCHANGE(9),
    SHOW_ACTIVE_ORDERS_FOR_COMPANY_ON_EXCHANGE(10),
    SHOW_ACTIVE_ORDERS_FOR_STOCK_TRADER(11),
    SHOW_MARKET_PRICE_EVOLUTION_FOR_COMPANY(12),
    QUIT(13);
    private final int idx;

    ServiceCommand(int idx) {
        this.idx = idx;
    }

    public static int count() {
        return QUIT.getIdx();
    }

    public int getIdx() {
        return this.idx;
    }
}
