package model.Company;

import model.Exchange.Exchange;
import model.Exchange.Transaction;
import utils.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class Company {
    private final String name, ticker;
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

    public void listOn(Exchange e) {
        exchanges.add(e);
    }

    public void graphMarketPriceEvolution() {
        Utils.output_separator();

        System.out.println("Market price evolution for " + name + " {" + ticker + "}:");

        ArrayList<Transaction> transactions = new ArrayList<>();
        for (Exchange e : exchanges) {
            transactions.addAll(e.getTransactionsFor(ticker));
        }

        transactions.sort(new Comparator<>() {
            @Override
            public int compare(Transaction o1, Transaction o2) {
                return o1.date().compareTo(o2.date());
            }
        });

        if (transactions.size() == 0) {
            System.out.println(name + " has no transaction history");
        } else {
            String pattern = "#.###";
            DecimalFormat decimalFormat = new DecimalFormat(pattern);

            for (int i = 0; i < transactions.size(); i++) {
                String formattedPrice = decimalFormat.format(transactions.get(i).price());

                System.out.println((i + 1) + ". " + formattedPrice + "$ " + transactions.get(i).date());
            }
        }
    }
}
