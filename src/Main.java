public class Main {
    private static final long MEGABYTE = 1024L * 1024;

    public static void main(String[] args) {
        StockMarketSimulator simulator = StockMarketSimulator.getInstance();
        StockMarketState state = simulator.simulate_automatic(10, 10, 3, 3, 5000, 0.2);
        StockMarketService service = StockMarketService.getStockMarketService(state);
        service.runService();

        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory: " + memory / MEGABYTE + "MB");

        runtime.gc();
        memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory after ~GC: " + memory / MEGABYTE + "MB");
    }
}