package exception;

/** Bad or missing input (e.g. empty text, missing/invalid date-time). */
public class InvalidInputException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
