package exceptions;

// Exception thrown when there is an attempt to place a market order for a company that has no outstanding orders on a given exchange
// The market price cannot be determined in this case
public class NoDataFoundForCompanyException extends Exception {
    public NoDataFoundForCompanyException(String errorMessage) {
        super(errorMessage);
    }
}
