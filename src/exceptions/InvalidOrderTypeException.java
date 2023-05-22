package exceptions;

// Exception thrown when an invalid order type is detected
public class InvalidOrderTypeException extends Exception {
    public InvalidOrderTypeException(String errorMessage) {
        super(errorMessage);
    }
}
