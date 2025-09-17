package vn.vinaacademy.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for security client library. Allows services to override default gRPC
 * and OAuth2 settings.
 */
@Data
@ConfigurationProperties(prefix = "security")
public class SecurityClientProperties {

  /** JWT token validation settings */
  private Grpc grpc = new Grpc();

  /** OAuth2 client settings for gRPC authentication */
  private OAuth2 oauth2 = new OAuth2();

  @Data
  public static class Grpc {
    /** gRPC server address for JWT validation */
    private String grpcAddress = "localhost:9090";

    /** Enable keep-alive for gRPC connection */
    private boolean enableKeepAlive = true;

    /** Keep-alive time in seconds */
    private int keepAliveTime = 30;

    /** Keep-alive timeout in seconds */
    private int keepAliveTimeout = 5;
  }

  @Data
  public static class OAuth2 {
    /** gRPC client configuration */
    private GrpcClient grpcClient = new GrpcClient();

    /** Authorization server settings */
    private Provider provider = new Provider();
  }

  @Data
  public static class GrpcClient {
    /** OAuth2 client ID for gRPC authentication */
    private String clientId = "grpc-client";

    /** OAuth2 client secret for gRPC authentication */
    private String clientSecret = "grpc-secret";

    /** OAuth2 scopes */
    private String[] scopes = {"api.read", "api.write"};
  }

  @Data
  public static class Provider {
    /** OAuth2 token URI */
    private String tokenUri = "http://localhost:8080/oauth2/token";
  }
}
