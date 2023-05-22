package model.company;

import database.DatabaseConnection;
import model.exchange.Exchange;
import model.exchange.Transaction;
import utils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class Company {
    private final String name;
    private final String ticker;
    private final HashSet<Exchange> exchanges;

    public Company(String name, String ticker) {
        this.name = name;
        this.ticker = ticker;
        exchanges = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getTicker() {
        return ticker;
    }

    public boolean isListedOn(Exchange e) {
        return exchanges.contains(e);
    }

    // list company on the exchange
    public void listOn(Exchange e) throws SQLException {
        DatabaseConnection.getInstance().listOn(e.getName(), this.ticker);
        exchanges.add(e);
    }

    // graph the market price evolution of the company
    public void graphMarketPriceEvolution() {
        System.out.println("Market price evolution for " + name + " {" + ticker + "}:");

        // collect all the transactions for the company on all the exchanges that it is listed on
        ArrayList<Transaction> transactions = new ArrayList<>();
        for (Exchange e : exchanges) {
            transactions.addAll(e.getTransactionsFor(ticker));
        }

        // sort the transactions ascending by date
        transactions.sort(Comparator.comparing(Transaction::date));

        // print the evolution of prices
        if (transactions.size() == 0) {
            System.out.println(name + " has no transaction history");
        } else {
            transactions.forEach(t -> System.out.println(Utils.format_decimal(t.price(), "#.###") + "$ " + t.date()));
        }
    }
}
