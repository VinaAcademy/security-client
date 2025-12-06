package vn.vinaacademy.security.config.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.vinaacademy.security.discovery.EurekaServiceDiscovery;
import vn.vinaacademy.security.properties.SecurityClientProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrpcChannelFactory {

  private final SecurityClientProperties properties;

  @Autowired(required = false)
  private EurekaServiceDiscovery eurekaServiceDiscovery;

  private final OAuth2GrpcClientInterceptor interceptor;

  public ManagedChannel createAuthChannel() {
    String grpcAddress = properties.getGrpc().getGrpcAddress();
    if (eurekaServiceDiscovery != null && properties.getEureka().isEnabled()) {
      grpcAddress = eurekaServiceDiscovery.resolveGrpcAddress();
      log.info("✅ [Dynamic] Using Eureka-resolved gRPC address: {}", grpcAddress);
    }
    grpcAddress = grpcAddress.trim().replaceAll("^(https?|grpc)://", "");
    String[] parts = grpcAddress.split(":");
    String host = parts[0];
    int port = Integer.parseInt(parts[1]);

    var builder =
        ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .keepAliveTime(properties.getGrpc().getKeepAliveTime(), TimeUnit.SECONDS)
            .keepAliveTimeout(properties.getGrpc().getKeepAliveTimeout(), TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true);

    builder.intercept(interceptor);

    return builder.build();
  }
}
