import service.StockMarketService;
import service.StockMarketSimulator;
import service.StockMarketState;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            // Let the user pick if they want to run with existing data or start with a fresh simulation
            System.out.println("Choose operation mode:\n1. RUN WITH EXISTING DATA\n2. START WITH A FRESH DATABASE");
            int option = new Scanner(System.in).nextInt();

            StockMarketService service;
            if (option == 1) {
                // Start the service with existing data
                service = StockMarketService.getStockMarketService();
            } else {
                // Start the service after a stock market simulation
                StockMarketSimulator simulator = StockMarketSimulator.getInstance();
                StockMarketState state = simulator.simulate_automatic(5, 5, 3, 3, 500, 0.2);
                service = StockMarketService.getStockMarketService(state);
            }

            // Start interactive menu
            service.runService();
        } catch (SQLException e) {
            System.out.println("Could not connect to database: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Could not open the audit file: " + e.getMessage());
        }
    }
}