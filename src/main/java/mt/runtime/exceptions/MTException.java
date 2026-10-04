package mt.runtime.exceptions;

public class MTException
        extends Exception {

    public MTException(String message) {
        super(message);
    }

    public MTException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}
