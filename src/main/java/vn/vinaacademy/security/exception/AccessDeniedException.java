package vn.vinaacademy.security.exception;

/**
 * Exception thrown when user doesn't have sufficient privileges
 */
public class AccessDeniedException extends SecurityException {
    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
