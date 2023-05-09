package exceptions;

public class CompanyNotListedOnExchangeException extends Exception {
    public CompanyNotListedOnExchangeException(String errorMessage) {
        super(errorMessage);
    }
}
