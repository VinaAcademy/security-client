package vn.vinaacademy.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for pre-authorization using SpEL expressions.
 * Can be applied to methods or classes.
 * 
 * Supported expressions:
 * - hasRole('ROLE_ADMIN'): Check if user has specific role
 * - hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF'): Check if user has any of the roles
 * - isAuthenticated(): Check if user is authenticated
 * - user.userId == #userId: Check if user ID matches parameter
 * - user.email == 'admin@example.com': Check if email matches
 * 
 * Example usage:
 * @PreAuthorize("hasRole('ROLE_ADMIN')")
 * @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
 * @PreAuthorize("isAuthenticated() and user.userId == #userId")
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PreAuthorize {
    /**
     * The SpEL expression to evaluate for authorization.
     * 
     * @return SpEL expression
     */
    String value();
    
    /**
     * Optional message to return when access is denied.
     * 
     * @return access denied message
     */
    String message() default "Access denied: authorization failed";
}
