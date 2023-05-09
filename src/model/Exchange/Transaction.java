package model.Exchange;

import java.util.Date;

public record Transaction(Date date, String from, String to, double price, int quantity) {
}
