package vn.vinaacademy.security.filter;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import com.vinaacademy.grpc.ValidateTokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.vinaacademy.security.authentication.SecurityContextHolder;
import vn.vinaacademy.security.authentication.UserContext;
import vn.vinaacademy.security.grpc.JwtGrpcClient;

/**
 * Filter to extract JWT token from Authorization header and validate it via gRPC. Sets up the
 * security context for the current request.
 */
@Slf4j
@Component
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtGrpcClient jwtGrpcClient;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String token = extractTokenFromRequest(request);

      if (token != null) {
        UserContext userContext = validateTokenAndCreateContext(token);
        SecurityContextHolder.setContext(userContext);
        log.debug("Security context set for user: {}", userContext.getUserId());
      } else {
        log.trace("No token found in request");
      }

      filterChain.doFilter(request, response);

    } catch (Exception e) {
      log.error("Error processing JWT token", e);
      // Continue with empty security context
      filterChain.doFilter(request, response);
    } finally {
      // Always clear context after request
      SecurityContextHolder.clearContext();
    }
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7); // Remove "Bearer " prefix
    }

    return null;
  }

  private UserContext validateTokenAndCreateContext(String token) {
    try {
      ValidateTokenResponse response = jwtGrpcClient.validateToken(token);

      if (response.getIsValid()) {
        return UserContext.builder()
            .userId(response.getUserId())
            .email(response.getEmail())
            .fullName(response.getFullName())
            .avatarUrl(response.getAvatarUrl())
            .roles(UserContext.parseRoles(response.getRoles()))
            .token(token)
            .authenticated(true)
            .build();
      } else {
        log.debug("Token validation failed: {}", response.getMessage());
        return createUnauthenticatedContext(token);
      }
    } catch (Exception e) {
      log.error("Error validating token via gRPC", e);
      return createUnauthenticatedContext(token);
    }
  }

  private UserContext createUnauthenticatedContext(String token) {
    return UserContext.builder().token(token).authenticated(false).build();
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // Skip filter for health check endpoints and actuator endpoints
    String path = request.getRequestURI();
    return path.startsWith("/actuator/") || path.equals("/health") || path.equals("/ping");
  }
}
