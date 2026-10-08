package exception;

/** Illegal state change (e.g. approving an already-approved appointment). */
public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String message) {
        super(message);
    }
}
