package vn.vinaacademy.security.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Context class to hold authenticated user information from JWT token validation.
 * This class is populated from ValidateTokenResponse and stored in ThreadLocal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private String userId;
    private String email;
    private String fullName;
    private String avatarUrl;
    private Set<String> roles;
    private String token;
    private boolean authenticated;

    /**
     * Parse roles from comma-separated string format (e.g., "ROLE_admin,ROLE_student")
     */
    public static Set<String> parseRoles(String rolesString) {
        if (rolesString == null || rolesString.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        return new HashSet<>(Arrays.asList(rolesString.split(",")));
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String role) {
        if (roles == null) return false;
        // Support both ROLE_admin and admin formats
        return roles.contains(role) || roles.contains("ROLE_" + role);
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(String... rolesToCheck) {
        if (roles == null || rolesToCheck == null) return false;
        
        for (String role : rolesToCheck) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has all of the specified roles
     */
    public boolean hasAllRoles(String... rolesToCheck) {
        if (roles == null || rolesToCheck == null) return false;
        
        for (String role : rolesToCheck) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }
}
