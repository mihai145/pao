package exceptions;

public class InvalidOrderTypeException extends Exception {
    public InvalidOrderTypeException(String errorMessage) {
        super(errorMessage);
    }
}
