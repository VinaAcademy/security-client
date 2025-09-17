package vn.vinaacademy.security.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.vinaacademy.security.annotation.HasAnyRole;
import vn.vinaacademy.security.annotation.PreAuthorize;
import vn.vinaacademy.security.authentication.SecurityContextHolder;
import vn.vinaacademy.security.authentication.UserContext;
import vn.vinaacademy.security.evaluator.SecurityExpressionEvaluator;
import vn.vinaacademy.security.exception.AccessDeniedException;
import vn.vinaacademy.security.exception.AuthenticationException;

import java.lang.reflect.Method;

/**
 * AOP Aspect to handle security authorization for methods and classes
 * annotated with @HasAnyRole or @PreAuthorize.
 */
@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class SecurityAspect {

    private final SecurityExpressionEvaluator expressionEvaluator;

    /**
     * Handle @HasAnyRole annotation on methods
     */
    @Before("@annotation(hasAnyRole)")
    public void checkMethodHasAnyRole(JoinPoint joinPoint, HasAnyRole hasAnyRole) {
        log.debug("Checking @HasAnyRole on method: {}", joinPoint.getSignature().getName());
        checkHasAnyRole(hasAnyRole);
    }

    /**
     * Handle @HasAnyRole annotation on classes
     */
    @Before("@within(hasAnyRole) && execution(public * *(..))")
    public void checkClassHasAnyRole(JoinPoint joinPoint, HasAnyRole hasAnyRole) {
        // Check if method-level annotation exists, if so, skip class-level check
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.isAnnotationPresent(HasAnyRole.class) || method.isAnnotationPresent(PreAuthorize.class)) {
            return;
        }
        
        log.debug("Checking @HasAnyRole on class for method: {}", joinPoint.getSignature().getName());
        checkHasAnyRole(hasAnyRole);
    }

    /**
     * Handle @PreAuthorize annotation on methods
     */
    @Before("@annotation(preAuthorize)")
    public void checkMethodPreAuthorize(JoinPoint joinPoint, PreAuthorize preAuthorize) {
        log.debug("Checking @PreAuthorize on method: {}", joinPoint.getSignature().getName());
        checkPreAuthorize(preAuthorize, joinPoint);
    }

    /**
     * Handle @PreAuthorize annotation on classes
     */
    @Before("@within(preAuthorize) && execution(public * *(..))")
    public void checkClassPreAuthorize(JoinPoint joinPoint, PreAuthorize preAuthorize) {
        // Check if method-level annotation exists, if so, skip class-level check
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.isAnnotationPresent(HasAnyRole.class) || method.isAnnotationPresent(PreAuthorize.class)) {
            return;
        }
        
        log.debug("Checking @PreAuthorize on class for method: {}", joinPoint.getSignature().getName());
        checkPreAuthorize(preAuthorize, joinPoint);
    }

    private void checkHasAnyRole(HasAnyRole hasAnyRole) {
        UserContext userContext = SecurityContextHolder.getContext();
        
        if (userContext == null || !userContext.isAuthenticated()) {
            log.warn("Access denied: User not authenticated");
            throw new AuthenticationException("Authentication required");
        }

        String[] requiredRoles = hasAnyRole.value();
        if (requiredRoles.length == 0) {
            log.debug("No roles required, allowing access");
            return;
        }

        boolean hasAccess = userContext.hasAnyRole(requiredRoles);
        if (!hasAccess) {
            log.warn("Access denied: User {} does not have any of the required roles: {}", 
                     userContext.getUserId(), String.join(", ", requiredRoles));
            throw new AccessDeniedException(hasAnyRole.message());
        }

        log.debug("Access granted: User {} has required role", userContext.getUserId());
    }

    private void checkPreAuthorize(PreAuthorize preAuthorize, JoinPoint joinPoint) {
        UserContext userContext = SecurityContextHolder.getContext();
        
        if (userContext == null || !userContext.isAuthenticated()) {
            log.warn("Access denied: User not authenticated");
            throw new AuthenticationException("Authentication required");
        }

        String expression = preAuthorize.value();
        boolean hasAccess = expressionEvaluator.evaluate(expression, userContext, joinPoint);
        
        if (!hasAccess) {
            log.warn("Access denied: Expression '{}' evaluated to false for user {}", 
                     expression, userContext.getUserId());
            throw new AccessDeniedException(preAuthorize.message());
        }

        log.debug("Access granted: Expression '{}' evaluated to true for user {}", 
                  expression, userContext.getUserId());
    }
}
