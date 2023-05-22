package model.exchange;

import java.util.Date;

// Record class that stores the details of a generated transaction on the stock market
public record Transaction(Date date, String from, String to, double price, int quantity) {
}
