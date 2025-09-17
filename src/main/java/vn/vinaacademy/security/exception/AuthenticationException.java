package vn.vinaacademy.security.exception;

/**
 * Exception thrown when authentication is required but not provided
 */
public class AuthenticationException extends SecurityException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
