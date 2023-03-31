public enum ServiceCommand {
    ADD_EXCHANGE(1),
    ADD_COMPANY(2),
    ADD_STOCK_TRADER(3),
    LIST_COMPANY_ON_EXCHANGE(4),
    PLACE_ORDER(5),
    CANCEL_ORDER(6),
    SHOW_TRANSACTIONS_FOR_COMPANY_ON_EXCHANGE(7),
    SHOW_ACTIVE_ORDERS_FOR_COMPANY_ON_EXCHANGE(8),
    SHOW_ACTIVE_ORDERS_FOR_STOCK_TRADER(9),
    SHOW_MARKET_PRICE_EVOLUTION_FOR_COMPANY(10),
    QUIT(11);
    private final int idx;
    ServiceCommand(int idx) {
        this.idx = idx;
    }

    public int getIdx() {
        return this.idx;
    }

    public static int count() {
        return QUIT.getIdx();
    }
}
