package Caesar.exception;

/**
 * Exception used for user-facing validation and persistence errors.
 */
public class CaesarException extends Exception {
    private static final long serialVersionUID = 1L;

    public CaesarException(String message) {
        super(message);
    }
}
