package vn.vinaacademy.security.authentication;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local storage for the current user's security context. Provides access to authenticated
 * user information throughout the request lifecycle.
 */
@Slf4j
@UtilityClass
public class SecurityContextHolder {
  private static final ThreadLocal<UserContext> contextHolder = new ThreadLocal<>();

  /** Set the current user context */
  public static void setContext(UserContext userContext) {
    contextHolder.set(userContext);
    log.trace(
        "Security context set for user: {}",
        userContext != null ? userContext.getUserId() : "null");
  }

  /** Get the current user context */
  public static UserContext getContext() {
    return contextHolder.get();
  }

  /** Clear the current user context */
  public static void clearContext() {
    UserContext context = contextHolder.get();
    contextHolder.remove();
    log.trace(
        "Security context cleared for user: {}", context != null ? context.getUserId() : "null");
  }

  /** Check if current user is authenticated */
  public static boolean isAuthenticated() {
    UserContext context = getContext();
    return context != null && context.isAuthenticated();
  }

  /** Get current user ID */
  public static String getCurrentUserId() {
    UserContext context = getContext();
    return context != null ? context.getUserId() : null;
  }

  /** Get current user email */
  public static String getCurrentUserEmail() {
    UserContext context = getContext();
    return context != null ? context.getEmail() : null;
  }

  /** Check if current user has a specific role */
  public static boolean hasRole(String role) {
    UserContext context = getContext();
    return context != null && context.hasRole(role);
  }

  /** Check if current user has any of the specified roles */
  public static boolean hasAnyRole(String... roles) {
    UserContext context = getContext();
    return context != null && context.hasAnyRole(roles);
  }
}
