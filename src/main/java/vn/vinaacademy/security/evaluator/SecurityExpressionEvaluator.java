package vn.vinaacademy.security.evaluator;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import vn.vinaacademy.security.authentication.UserContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Evaluates SpEL expressions for @PreAuthorize annotations.
 * Supports custom security expressions and method parameter access.
 */
@Slf4j
@Component
public class SecurityExpressionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Evaluate a SpEL expression in the security context
     */
    public boolean evaluate(String expressionString, UserContext userContext, JoinPoint joinPoint) {
        try {
            Expression expression = parser.parseExpression(expressionString);
            EvaluationContext context = createEvaluationContext(userContext, joinPoint);
            
            Object result = expression.getValue(context);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to evaluate security expression: {}", expressionString, e);
            return false;
        }
    }

    private EvaluationContext createEvaluationContext(UserContext userContext, JoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // Register the user context
        context.setVariable("user", userContext);
        
        // Register method parameters
        if (joinPoint != null) {
            registerMethodParameters(context, joinPoint);
        }
        
        // Register security functions
        registerSecurityFunctions(context, userContext);
        
        return context;
    }

    private void registerMethodParameters(EvaluationContext context, JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length && i < args.length; i++) {
            String paramName = parameters[i].getName();
            context.setVariable(paramName, args[i]);
        }
    }

    private void registerSecurityFunctions(EvaluationContext context, UserContext userContext) {
        // Register security functions
        SecurityFunctions functions = new SecurityFunctions(userContext);
        
        // Register security functions as variables
        context.setVariable("hasRole", (java.util.function.Function<String, Boolean>) functions::hasRole);
        context.setVariable("hasAnyRole", (java.util.function.Function<String[], Boolean>) functions::hasAnyRole);
        context.setVariable("hasAllRoles", (java.util.function.Function<String[], Boolean>) functions::hasAllRoles);
        context.setVariable("isAuthenticated", (java.util.function.Supplier<Boolean>) functions::isAuthenticated);
        
        // For StandardEvaluationContext, we can set the root object
        if (context instanceof StandardEvaluationContext) {
            ((StandardEvaluationContext) context).setRootObject(functions);
        }
    }

    /**
     * Security functions available in SpEL expressions
     */
    public static class SecurityFunctions {
        private final UserContext userContext;

        public SecurityFunctions(UserContext userContext) {
            this.userContext = userContext;
        }

        public boolean hasRole(String role) {
            return userContext != null && userContext.hasRole(role);
        }

        public boolean hasAnyRole(String... roles) {
            return userContext != null && userContext.hasAnyRole(roles);
        }

        public boolean hasAllRoles(String... roles) {
            return userContext != null && userContext.hasAllRoles(roles);
        }

        public boolean isAuthenticated() {
            return userContext != null && userContext.isAuthenticated();
        }
    }
}
