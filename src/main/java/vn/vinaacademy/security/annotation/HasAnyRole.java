package vn.vinaacademy.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to check if the authenticated user has any of the specified roles.
 * Can be applied to methods or classes.
 * 
 * Example usage:
 * @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
 * public void adminOrStaffOnlyMethod() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasAnyRole {
    /**
     * The roles required to access the method/class.
     * User must have at least one of these roles.
     * 
     * @return array of required roles
     */
    String[] value();
    
    /**
     * Optional message to return when access is denied.
     * 
     * @return access denied message
     */
    String message() default "Access denied: insufficient privileges";
}
