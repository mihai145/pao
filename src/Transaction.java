import java.util.Date;

record Transaction(Date date, String from, String to, double price, int quantity) {
}
