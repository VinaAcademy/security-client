package vn.vinaacademy.security.config;

import com.vinaacademy.grpc.JwtServiceGrpc;
import com.vinaacademy.grpc.JwtServiceGrpc.JwtServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.vinaacademy.security.properties.SecurityClientProperties;

@Configuration
@RequiredArgsConstructor
public class GrpcClientConfig {

  private final OAuth2GrpcClientInterceptor interceptor;

  private final SecurityClientProperties properties;

  @Bean
  JwtServiceBlockingStub stub() {
    return JwtServiceGrpc.newBlockingStub(authServerChannel());
  }

  @Bean(destroyMethod = "shutdown")
  public ManagedChannel authServerChannel() {
    String grpcAddress = properties.getGrpc().getGrpcAddress();
    boolean enableKeepAlive = properties.getGrpc().isEnableKeepAlive();
    long keepAliveTime = properties.getGrpc().getKeepAliveTime();
    long keepAliveTimeout = properties.getGrpc().getKeepAliveTimeout();

    String[] parts = grpcAddress.split(":");
    String host = parts[0];
    int port = Integer.parseInt(parts[1]);

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
