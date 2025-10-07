package vn.vinaacademy.security.discovery;

import java.lang.reflect.Method;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import vn.vinaacademy.security.properties.SecurityClientProperties;

/**
 * Eureka service discovery helper. Uses reflection to avoid compile-time dependency on Spring Cloud
 * Netflix Eureka Client.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "security.eureka", name = "enabled", havingValue = "true")
public class EurekaServiceDiscovery {

  private final ApplicationContext applicationContext;
  private final SecurityClientProperties properties;
  private final Object discoveryClient;

  @Autowired
  public EurekaServiceDiscovery(
      ApplicationContext applicationContext, SecurityClientProperties properties) {
    this.applicationContext = applicationContext;
    this.properties = properties;
    this.discoveryClient = initializeDiscoveryClient();
  }

  private Object initializeDiscoveryClient() {
    try {
      // Try to get DiscoveryClient bean using reflection
      Class<?> discoveryClientClass =
          Class.forName("org.springframework.cloud.client.discovery.DiscoveryClient");
      return applicationContext.getBean(discoveryClientClass);
    } catch (ClassNotFoundException e) {
      log.warn(
          "Eureka is enabled but Spring Cloud Discovery Client is not on the classpath. "
              + "Falling back to configured addresses.");
      return null;
    } catch (Exception e) {
      log.error("Failed to initialize DiscoveryClient: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Resolves the service host from Eureka.
   *
   * @param serviceName the service name to resolve
   * @param fallbackAddress fallback address if Eureka resolution fails
   * @return resolved host or fallback
   */
  public String resolveServiceAddress(String serviceName, String fallbackAddress) {
    Object instance = findServiceInstance(serviceName);
    if (instance == null) {
      return fallbackAddress;
    }

    try {
      Method getHostMethod = instance.getClass().getMethod("getHost");
      String host = (String) getHostMethod.invoke(instance);
      log.info("Resolved service '{}' to host: {}", serviceName, host);
      return host;
    } catch (Exception e) {
      log.error(
          "Error resolving host for service '{}': {}, using fallback: {}",
          serviceName,
          e.getMessage(),
          fallbackAddress);
      return fallbackAddress;
    }
  }

  /**
   * Resolves the service host:port from Eureka.
   *
   * @param serviceName the service name to resolve
   * @param fallbackAddress fallback address if Eureka resolution fails
   * @return resolved host:port or fallback
   */
  public String resolveServiceHostPort(String serviceName, String fallbackAddress) {
    Object instance = findServiceInstance(serviceName);
    if (instance == null) {
      return fallbackAddress;
    }

    try {
      Method getHostMethod = instance.getClass().getMethod("getHost");
      String host = (String) getHostMethod.invoke(instance);

      int port;
      if (properties.getEureka().isUseSecurePort()) {
        Method getSecurePort = instance.getClass().getMethod("getSecurePort");
        port = (int) getSecurePort.invoke(instance);
      } else {
        Method getPort = instance.getClass().getMethod("getPort");
        port = (int) getPort.invoke(instance);
      }

      String hostPort = host + ":" + port;
      log.info("Resolved service '{}' to host: {}", serviceName, hostPort);
      return hostPort;
    } catch (Exception e) {
      log.error(
          "Error resolving host/port for service '{}': {}, using fallback: {}",
          serviceName,
          e.getMessage(),
          fallbackAddress);
      return fallbackAddress;
    }
  }

  /** Common method to find a service instance from Eureka using reflection. */
  private Object findServiceInstance(String serviceName) {
    if (!properties.getEureka().isEnabled() || discoveryClient == null) {
      log.debug("Eureka discovery disabled or unavailable for '{}'", serviceName);
      return null;
    }

    try {
      Method getInstances = discoveryClient.getClass().getMethod("getInstances", String.class);
      List<?> instances = (List<?>) getInstances.invoke(discoveryClient, serviceName);

      if (instances == null || instances.isEmpty()) {
        log.warn("No instances found for service '{}'", serviceName);
        return null;
      }

      return instances.get(0);
    } catch (Exception e) {
      log.error("Failed to get instances for service '{}': {}", serviceName, e.getMessage());
      return null;
    }
  }

  /**
   * Resolves the gRPC server address (host:port) from Eureka.
   *
   * @return resolved gRPC address or fallback from properties
   */
  public String resolveGrpcAddress() {
    String serviceName = properties.getGrpc().getServiceName();
    int grpcPort = properties.getGrpc().getGrpcPort();
    String fallbackAddress = properties.getGrpc().getGrpcAddress();

    String host;
    if (fallbackAddress != null && fallbackAddress.contains(":")) {
      host = resolveServiceAddress(serviceName, fallbackAddress.split(":")[0]);
    } else {
      host = resolveServiceAddress(serviceName, fallbackAddress);
    }
    return host + ":" + grpcPort;
  }

  /**
   * Resolves the OAuth2 token URI from Eureka.
   *
   * @return resolved token URI or fallback from properties
   */
  public String resolveOAuth2TokenUri() {
    String serviceName = properties.getOauth2().getProvider().getServiceName();
    String fallbackUri = properties.getOauth2().getProvider().getTokenUri();

    // Extract host from fallback URI
    String fallbackHost = extractHostFromUri(fallbackUri);

    String host = resolveServiceHostPort(serviceName, fallbackHost);

    // Reconstruct the token URI with resolved host
    return fallbackUri.replace(fallbackHost, host);
  }

  private String extractHostFromUri(String uri) {
    try {
      java.net.URI parsedUri = new java.net.URI(uri);
      String host = parsedUri.getHost();
      int port = parsedUri.getPort();
      if (port != -1) {
        return host + ":" + port;
      }
      return host;
    } catch (Exception e) {
      log.warn("Failed to parse URI: {}, using as-is", uri);
      return uri;
    }
  }
}
