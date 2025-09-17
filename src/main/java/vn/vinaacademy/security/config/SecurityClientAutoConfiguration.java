package vn.vinaacademy.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import vn.vinaacademy.security.aspect.SecurityAspect;
import vn.vinaacademy.security.evaluator.SecurityExpressionEvaluator;
import vn.vinaacademy.security.filter.JwtAuthenticationFilter;
import vn.vinaacademy.security.grpc.JwtGrpcClient;
import vn.vinaacademy.security.properties.SecurityClientProperties;

/**
 * Autoconfiguration for security client library. This class is automatically loaded when the
 * library is included in a Spring Boot application.
 */
@Slf4j
@AutoConfiguration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "vn.vinaacademy.security")
@ConditionalOnClass({JwtGrpcClient.class})
@EnableConfigurationProperties(SecurityClientProperties.class)
@RequiredArgsConstructor
public class SecurityClientAutoConfiguration {

  private final SecurityClientProperties securityClientProperties;

  @Bean
  @ConditionalOnMissingBean
  public SecurityExpressionEvaluator securityExpressionEvaluator() {
    log.info("Creating SecurityExpressionEvaluator bean");
    return new SecurityExpressionEvaluator();
  }

  @Bean
  @ConditionalOnMissingBean
  public SecurityAspect securityAspect(SecurityExpressionEvaluator securityExpressionEvaluator) {
    log.info("Creating SecurityAspect bean");
    return new SecurityAspect(securityExpressionEvaluator);
  }

  @Bean
  @ConditionalOnMissingBean
  public JwtAuthenticationFilter jwtAuthenticationFilter(JwtGrpcClient jwtGrpcClient) {
    log.info(
        "Creating JwtAuthenticationFilter bean with gRPC address: {}",
        securityClientProperties.getGrpc().getGrpcAddress());
    return new JwtAuthenticationFilter(jwtGrpcClient);
  }
}
