package database;

import exceptions.InvalidOrderTypeException;
import exceptions.NoDataFoundForCompanyException;
import model.company.Company;
import model.exchange.Exchange;
import model.order.*;
import model.stocktrader.StockTrader;
import service.StockMarketState;

import java.sql.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class DatabaseConnection {
    private static DatabaseConnection instance = null;
    private final Connection connection;

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

    public StockMarketState getState() throws SQLException {
        ArrayList<Exchange> exchanges = getExchanges();
        ArrayList<Company> companies = getCompanies();
        ArrayList<StockTrader> stockTraders = getStockTraders();

        getListings(companies, exchanges);
        getOrders(exchanges, stockTraders);
        getTransactions(exchanges);

        return new StockMarketState(exchanges, companies, stockTraders);
    }

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

    private void getListings(ArrayList<Company> companies, ArrayList<Exchange> exchanges) throws SQLException {
        String query = "SELECT * FROM LISTED_ON";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            String exchangeName = rs.getString("exchange_name");
            String companyTicker = rs.getString("company_ticker");

            try {
                Exchange exch = exchanges.stream().filter(e -> e.getName().equals(exchangeName)).findFirst().get();
                Company comp = companies.stream().filter(c -> c.getTicker().equals(companyTicker)).findFirst().get();

                comp.listOn(exch);
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void getOrders(ArrayList<Exchange> exchanges, ArrayList<StockTrader> stockTraders) throws SQLException {
        String query = "SELECT * FROM ORDERS ORDER BY order_type";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            try {
                int id = rs.getInt("id");
                OrderAction orderAction = getOrderAction(rs.getString("order_action"));
                OrderType orderType = getOrderType(rs.getString("order_type"));
                String stockTraderName = rs.getString("stocktrader_name");
                String companyTicker = rs.getString("company_ticker");
                String exchangeName = rs.getString("exchange_name");
                Date date = rs.getDate("date");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");

                StockTrader st = stockTraders.stream().filter(t -> t.getName().equals(stockTraderName)).findFirst().get();
                Exchange exch = exchanges.stream().filter(e -> e.getName().equals(exchangeName)).findFirst().get();

                Order order;
                if (orderType == OrderType.LIMIT) {
                    order = new LimitOrder(id, orderAction, st, companyTicker, quantity, price, exch, date);
                } else if (orderType == OrderType.ICEBERG) {
                    order = new IcebergOrder(id, orderAction, st, companyTicker, quantity, price, exch, date);
                } else {
                    order = new MarketOrder(id, orderAction, st, companyTicker, quantity, price, exch, date);
                }

                st.appendActiveOrder(order);
                exch.addOrder(order);
            } catch (InvalidOrderTypeException | NoSuchElementException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void getTransactions(ArrayList<Exchange> exchanges) throws SQLException {
        String query = "SELECT * FROM TRANSACTIONS";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            String companyTicker = rs.getString("company_ticker");
            String exchangeName = rs.getString("exchange_name");
            String stockTraderNameFrom = rs.getString("stocktrader_name_from");
            String stockTraderNameTo = rs.getString("stocktrader_name_to");
            Date date = rs.getDate("date");
            double price = rs.getDouble("price");
            int quantity = rs.getInt("quantity");

            try {
                Exchange exch = exchanges.stream().filter(e -> e.getName().equals(exchangeName)).findFirst().get();
                exch.addTransaction(companyTicker, stockTraderNameFrom, stockTraderNameTo, date, price, quantity);
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private OrderAction getOrderAction(String orderAction) throws InvalidOrderTypeException {
        for (OrderAction oa : OrderAction.values()) {
            if (oa.toString().equals(orderAction)) return oa;
        }
        throw new InvalidOrderTypeException("Order action " + orderAction + " does not exist");
    }

    public OrderType getOrderType(String orderType) throws InvalidOrderTypeException {
        for (OrderType ot : OrderType.values()) {
            if (ot.toString().equals(orderType)) return ot;
        }
        throw new InvalidOrderTypeException("Order type " + orderType + " does not exist");
    }

    public void addCompany(String company_name, String company_ticker) throws SQLException {
        String query = "INSERT INTO COMPANIES (company_name, company_ticker) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, company_name);
        statement.setString(2, company_ticker);
        statement.executeUpdate();
    }

    public void addExchange(String exchange_name) throws SQLException {
        String query = "INSERT INTO EXCHANGES (exchange_name) VALUES (?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, exchange_name);
        statement.executeUpdate();
    }

    public void addStockTrader(String stocktrader_name) throws SQLException {
        String query = "INSERT INTO STOCKTRADERS (stocktrader_name) VALUES (?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, stocktrader_name);
        statement.executeUpdate();
    }

    public void listOn(String exchangeName, String companyTicker) throws SQLException {
        String count = "SELECT COUNT(*) AS cnt FROM LISTED_ON WHERE exchange_name=? AND company_ticker=?";
        PreparedStatement pstmt = connection.prepareStatement(count);
        pstmt.setString(1, exchangeName);
        pstmt.setString(2, companyTicker);

        ResultSet rs = pstmt.executeQuery();
        rs.next();
        int cnt = rs.getInt("cnt");

        if (cnt == 0) {
            String query = "INSERT INTO LISTED_ON (exchange_name, company_ticker) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, exchangeName);
            statement.setString(2, companyTicker);
            statement.executeUpdate();
        }
    }

    public void addTransaction(String exchangeName,
                               String companyTicker,
                               Date date,
                               String stockTraderNameFrom,
                               String stockTraderNameTo,
                               double price,
                               int quantity) throws SQLException {
        String count = "SELECT COUNT(*) AS cnt FROM transactions WHERE date=? AND company_ticker=?";
        PreparedStatement pstmt = connection.prepareStatement(count);
        pstmt.setDate(1, date);
        pstmt.setString(2, companyTicker);

        ResultSet rs = pstmt.executeQuery();
        rs.next();
        int cnt = rs.getInt("cnt");

        if (cnt == 0) {
            String query = "INSERT INTO TRANSACTIONS (exchange_name, company_ticker, date, stocktrader_name_from, stocktrader_name_to, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, exchangeName);
            statement.setString(2, companyTicker);
            statement.setDate(3, date);
            statement.setString(4, stockTraderNameFrom);
            statement.setString(5, stockTraderNameTo);
            statement.setDouble(6, price);
            statement.setInt(7, quantity);
            statement.executeUpdate();
        }
    }

    public void addOrder(Order order) throws SQLException {
        String query = "INSERT INTO ORDERS (id, order_action, order_type, stocktrader_name, company_ticker, exchange_name, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setInt(1, (int) order.getId());
        statement.setString(2, order.getOrderAction().toString());
        if (order instanceof MarketOrder) {
            statement.setString(3, "MARKET");
        } else if (order instanceof LimitOrder) {
            statement.setString(3, "LIMIT");
        } else {
            statement.setString(3, "ICEBERG");
        }
        statement.setString(4, order.getStockTrader().getName());
        statement.setString(5, order.getTicker());
        statement.setString(6, order.getExchange().getName());
        statement.setFloat(7, (float) order.getPrice());
        statement.setInt(8, order.getQuantity());

        statement.executeUpdate();
    }

    public void removeOrder(Order order) throws SQLException {
        String query = "DELETE FROM ORDERS WHERE id=?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, order.getId());
        statement.executeUpdate();
    }

    public void eraseAll() throws SQLException {
        String[] tables = {"EXCHANGES", "COMPANIES", "STOCKTRADERS", "LISTED_ON", "ORDERS", "TRANSACTIONS"};
        for (String table : tables) {
            String query = "DELETE FROM " + table;
            Statement statement = connection.createStatement();
            statement.execute(query);
        }
    }
}