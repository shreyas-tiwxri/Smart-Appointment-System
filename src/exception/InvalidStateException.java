package exception;

/**
 * Operation violates business rules (e.g. approving a non-pending appointment).
 */
public class InvalidStateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidStateException(String message) {
        super(message);
    }
}