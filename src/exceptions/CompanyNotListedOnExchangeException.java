package exceptions;

// Exception thrown when there is an attempt to place an order for a company on an exchange that it is not listed on
public class CompanyNotListedOnExchangeException extends Exception {
    public CompanyNotListedOnExchangeException(String errorMessage) {
        super(errorMessage);
    }
}
