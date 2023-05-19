import service.StockMarketService;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // StockMarketSimulator simulator = StockMarketSimulator.getInstance();
        // StockMarketState state = simulator.simulate_automatic(10, 10, 3, 3, 5000, 0.2);

        try {
            StockMarketService service = StockMarketService.getStockMarketService();
            service.runService();
        } catch (SQLException e) {
            System.out.println("Could not connect to database: " + e.getMessage());
        }
    }
}