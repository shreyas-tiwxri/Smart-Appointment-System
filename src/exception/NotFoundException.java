package exception;

/** Requested patient/doctor/appointment does not exist. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
