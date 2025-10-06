package vn.vinaacademy.security.config;

import com.vinaacademy.grpc.JwtServiceGrpc;
import com.vinaacademy.grpc.JwtServiceGrpc.JwtServiceBlockingStub;
import com.vinaacademy.grpc.UserServiceGrpc;
import com.vinaacademy.grpc.UserServiceGrpc.UserServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.vinaacademy.security.discovery.EurekaServiceDiscovery;
import vn.vinaacademy.security.properties.SecurityClientProperties;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrpcClientConfig {

  private final OAuth2GrpcClientInterceptor interceptor;

  private final SecurityClientProperties properties;

  @Autowired(required = false)
  private EurekaServiceDiscovery eurekaServiceDiscovery;

  @Bean
  JwtServiceBlockingStub stub() {
    return JwtServiceGrpc.newBlockingStub(authServerChannel());
  }

  @Bean
  UserServiceBlockingStub userServiceStub() {
    return UserServiceGrpc.newBlockingStub(authServerChannel());
  }

  @Bean(destroyMethod = "shutdown")
  public ManagedChannel authServerChannel() {
    // Resolve gRPC address - use Eureka if enabled, otherwise use configured value
    String grpcAddress = properties.getGrpc().getGrpcAddress();
    if (eurekaServiceDiscovery != null && properties.getEureka().isEnabled()) {
      grpcAddress = eurekaServiceDiscovery.resolveGrpcAddress();
      log.info("Using Eureka-resolved gRPC address: {}", grpcAddress);
    } else {
      log.info("Using configured gRPC address: {}", grpcAddress);
    }

    boolean enableKeepAlive = properties.getGrpc().isEnableKeepAlive();
    long keepAliveTime = properties.getGrpc().getKeepAliveTime();
    long keepAliveTimeout = properties.getGrpc().getKeepAliveTimeout();

    String[] parts = grpcAddress.split(":");
    if (parts.length < 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
      log.error("Invalid gRPC address format: '{}'. Expected format 'host:port'.", grpcAddress);
      throw new IllegalArgumentException(
          "Invalid gRPC address format: '" + grpcAddress + "'. Expected format 'host:port'.");
    }
    String host = parts[0];
    int port;
    try {
      port = Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      log.error("Invalid port in gRPC address: '{}'. Port must be an integer.", grpcAddress);
      throw new IllegalArgumentException(
          "Invalid port in gRPC address: '" + grpcAddress + "'. Port must be an integer.", e);
    }

    ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(host, port).usePlaintext();

    // Configure gRPC channel properties from application.properties
    if (enableKeepAlive) {
      builder
          .keepAliveTime(keepAliveTime, TimeUnit.SECONDS)
          .keepAliveTimeout(keepAliveTimeout, TimeUnit.SECONDS)
          .keepAliveWithoutCalls(true);
    }

    builder.intercept(interceptor);

    return builder.build();
  }
}
