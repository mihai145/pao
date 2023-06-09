package database;

import exceptions.InvalidOrderTypeException;
import model.company.Company;
import model.exchange.Exchange;
import model.order.*;
import model.stocktrader.StockTrader;
import service.StockMarketState;

import java.sql.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;

// Singleton class that handles interactions with the database
public class DatabaseConnection {
    private static DatabaseConnection instance = null;
    private final Connection connection;

    // connection to the database
    private DatabaseConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5555/pao";
        String username = "admin";
        String password = "admin";
        connection = DriverManager.getConnection(url, username, password);
    }

    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // restores the state of the stock market from the database
    public StockMarketState getState() throws SQLException {
        // retrieve entities
        ArrayList<Exchange> exchanges = getExchanges();
        ArrayList<Company> companies = getCompanies();
        ArrayList<StockTrader> stockTraders = getStockTraders();

        // retrieve listings, orders and transactions; rewire references
        getListings(companies, exchanges);
        getOrders(exchanges, stockTraders);
        getTransactions(exchanges);

        // return the restored market state
        return new StockMarketState(exchanges, companies, stockTraders);
    }

    // get all exchanges from the database
    private ArrayList<Exchange> getExchanges() throws SQLException {
        String query = "SELECT * FROM EXCHANGES";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        ArrayList<Exchange> exchanges = new ArrayList<>();
        while (rs.next()) {
            exchanges.add(new Exchange(rs.getString("exchange_name")));
        }
        return exchanges;
    }

    // get all companies from the database
    private ArrayList<Company> getCompanies() throws SQLException {
        String query = "SELECT * FROM COMPANIES";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        ArrayList<Company> companies = new ArrayList<>();
        while (rs.next()) {
            companies.add(new Company(rs.getString("company_name"), rs.getString("company_ticker")));
        }
        return companies;
    }

    // get all stock traders from the database
    private ArrayList<StockTrader> getStockTraders() throws SQLException {
        String query = "SELECT * FROM STOCKTRADERS";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        ArrayList<StockTrader> stockTraders = new ArrayList<>();
        while (rs.next()) {
            stockTraders.add(new StockTrader(rs.getString("stocktrader_name")));
        }
        return stockTraders;
    }

    // get all listing from the database
    private void getListings(ArrayList<Company> companies, ArrayList<Exchange> exchanges) throws SQLException {
        String query = "SELECT * FROM LISTED_ON";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            // read attributes
            String exchangeName = rs.getString("exchange_name");
            String companyTicker = rs.getString("company_ticker");

            // assuming the database is not corrupt, these statements should not fail
            try {
                @SuppressWarnings("OptionalGetWithoutIsPresent") Exchange exch = exchanges.stream().filter(e -> e.getName().equals(exchangeName)).findFirst().get();
                @SuppressWarnings("OptionalGetWithoutIsPresent") Company comp = companies.stream().filter(c -> c.getTicker().equals(companyTicker)).findFirst().get();

                comp.listOn(exch);
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // get all outstanding orders from the database
    private void getOrders(ArrayList<Exchange> exchanges, ArrayList<StockTrader> stockTraders) throws SQLException {
        String query = "SELECT * FROM ORDERS ORDER BY order_type";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            try {
                // read attributes
                int id = rs.getInt("id");
                OrderAction orderAction = getOrderAction(rs.getString("order_action"));
                OrderType orderType = getOrderType(rs.getString("order_type"));
                String stockTraderName = rs.getString("stocktrader_name");
                String companyTicker = rs.getString("company_ticker");
                String exchangeName = rs.getString("exchange_name");
                Timestamp date = rs.getTimestamp("date");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");

                // assuming the database is not corrupt, these statements should not fail
                @SuppressWarnings("OptionalGetWithoutIsPresent") StockTrader st = stockTraders.stream().filter(t -> t.getName().equals(stockTraderName)).findFirst().get();
                @SuppressWarnings("OptionalGetWithoutIsPresent") Exchange exch = exchanges.stream().filter(e -> e.getName().equals(exchangeName)).findFirst().get();

                // instantiate the correct order type
                Order order;
                switch (orderType) {
                    case LIMIT ->
                            order = new LimitOrder(id, orderAction, st, companyTicker, quantity, price, exch, new Date(date.getTime()));
                    case ICEBERG ->
                            order = new IcebergOrder(id, orderAction, st, companyTicker, quantity, price, exch, new Date(date.getTime()));
                    default ->
                            order = new MarketOrder(id, orderAction, st, companyTicker, quantity, price, exch, new Date(date.getTime()));
                }

                st.appendActiveOrder(order);
                exch.addOrder(order);
            } catch (InvalidOrderTypeException | NoSuchElementException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // get all transactions from the databse
    private void getTransactions(ArrayList<Exchange> exchanges) throws SQLException {
        String query = "SELECT * FROM TRANSACTIONS";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            // read attributes
            String companyTicker = rs.getString("company_ticker");
            String exchangeName = rs.getString("exchange_name");
            String stockTraderNameFrom = rs.getString("stocktrader_name_from");
            String stockTraderNameTo = rs.getString("stocktrader_name_to");
            Timestamp date = rs.getTimestamp("date");
            double price = rs.getDouble("price");
            int quantity = rs.getInt("quantity");

            // assuming the database is not corrupt, these statements should not fail
            try {
                @SuppressWarnings("OptionalGetWithoutIsPresent") Exchange exch = exchanges.stream().filter(e -> e.getName().equals(exchangeName)).findFirst().get();
                exch.addTransaction(companyTicker, stockTraderNameFrom, stockTraderNameTo, new Date(date.getTime()), price, quantity);
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // returns the corresponding enum value for a string order action
    private OrderAction getOrderAction(String orderAction) throws InvalidOrderTypeException {
        for (OrderAction oa : OrderAction.values()) {
            if (oa.toString().equals(orderAction)) return oa;
        }
        throw new InvalidOrderTypeException("Order action " + orderAction + " does not exist");
    }

    // returns the corresponding enum value for a string order type
    public OrderType getOrderType(String orderType) throws InvalidOrderTypeException {
        for (OrderType ot : OrderType.values()) {
            if (ot.toString().equals(orderType)) return ot;
        }
        throw new InvalidOrderTypeException("Order type " + orderType + " does not exist");
    }

    // adds a company to the database
    public void addCompany(String company_name, String company_ticker) throws SQLException {
        String query = "INSERT INTO COMPANIES (company_name, company_ticker) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, company_name);
        statement.setString(2, company_ticker);
        statement.executeUpdate();
    }

    // adds an exchange to the database
    public void addExchange(String exchange_name) throws SQLException {
        String query = "INSERT INTO EXCHANGES (exchange_name) VALUES (?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, exchange_name);
        statement.executeUpdate();
    }

    // adds a stock trader to the database
    public void addStockTrader(String stocktrader_name) throws SQLException {
        String query = "INSERT INTO STOCKTRADERS (stocktrader_name) VALUES (?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, stocktrader_name);
        statement.executeUpdate();
    }

    // renames an exchange
    public void renameExchange(String oldName, String newName) throws SQLException {
        String query = "UPDATE EXCHANGES SET exchange_name = ? WHERE exchange_name = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, newName);
        statement.setString(2, oldName);
        statement.executeUpdate();
    }

    // renames a stock trader
    public void renameStockTrader(String oldName, String newName) throws SQLException {
        String query = "UPDATE STOCKTRADERS SET stocktrader_name = ? WHERE stocktrader_name = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, newName);
        statement.setString(2, oldName);
        statement.executeUpdate();
    }

    // add a listing to the databse
    public void listOn(String exchangeName, String companyTicker) throws SQLException {
        String count = "SELECT COUNT(*) AS cnt FROM LISTED_ON WHERE exchange_name=? AND company_ticker=?";
        PreparedStatement pstmt = connection.prepareStatement(count);
        pstmt.setString(1, exchangeName);
        pstmt.setString(2, companyTicker);

        ResultSet rs = pstmt.executeQuery();
        rs.next();
        int cnt = rs.getInt("cnt");

        // add the listing only if it does not exist already
        if (cnt == 0) {
            String query = "INSERT INTO LISTED_ON (exchange_name, company_ticker) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, exchangeName);
            statement.setString(2, companyTicker);
            statement.executeUpdate();
        }
    }

    // add a transaction to the databse
    public void addTransaction(String exchangeName,
                               String companyTicker,
                               Date date,
                               String stockTraderNameFrom,
                               String stockTraderNameTo,
                               double price,
                               int quantity) throws SQLException {
        String query = "INSERT INTO TRANSACTIONS (exchange_name, company_ticker, date, stocktrader_name_from, stocktrader_name_to, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);

        // populate attributes
        statement.setString(1, exchangeName);
        statement.setString(2, companyTicker);
        statement.setDate(3, date);
        statement.setString(4, stockTraderNameFrom);
        statement.setString(5, stockTraderNameTo);
        statement.setDouble(6, price);
        statement.setInt(7, quantity);

        statement.executeUpdate();
    }

    // adds an order to the database
    public void addOrder(Order order) throws SQLException {
        String query = "INSERT INTO ORDERS (id, order_action, order_type, stocktrader_name, company_ticker, exchange_name, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);

        // populate attributes
        statement.setInt(1, (int) order.getId());
        statement.setString(2, order.getOrderAction().toString());
        statement.setString(3, getOrderType(order));
        statement.setString(4, order.getStockTrader().getName());
        statement.setString(5, order.getTicker());
        statement.setString(6, order.getExchange().getName());
        statement.setFloat(7, (float) order.getPrice());
        statement.setInt(8, order.getQuantity());

        statement.executeUpdate();
    }

    // get the order type from an order
    private String getOrderType(Order order) {
        if (order instanceof MarketOrder) return "MARKET";
        if (order instanceof LimitOrder) return "LIMIT";
        return "ICEBERG";
    }

    // remove an order from the database
    public void removeOrder(Order order) throws SQLException {
        String query = "DELETE FROM ORDERS WHERE id=?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, order.getId());
        statement.executeUpdate();
    }

    // reset the database
    public void eraseAll() throws SQLException {
        String[] tables = {"EXCHANGES", "COMPANIES", "STOCKTRADERS", "LISTED_ON", "ORDERS", "TRANSACTIONS"};
        for (String table : tables) {
            @SuppressWarnings("SqlWithoutWhere") String query = "DELETE FROM " + table;
            Statement statement = connection.createStatement();
            statement.execute(query);
        }
    }

    // close the connection
    public void close() throws SQLException {
        connection.close();
    }
}