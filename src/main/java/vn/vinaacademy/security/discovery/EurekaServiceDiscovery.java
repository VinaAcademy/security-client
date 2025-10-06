package vn.vinaacademy.security.discovery;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
   * Resolves the service address from Eureka by service name.
   *
   * @param serviceName the service name to resolve
   * @param fallbackAddress fallback address if Eureka resolution fails
   * @return resolved address or fallback
   */
  public String resolveServiceAddress(String serviceName, String fallbackAddress) {
    if (!properties.getEureka().isEnabled() || discoveryClient == null) {
      log.debug(
          "Eureka discovery disabled or unavailable, using fallback address: {}", fallbackAddress);
      return fallbackAddress;
    }

    try {
      // Use reflection to call discoveryClient.getInstances(serviceName)
      Method getInstancesMethod =
          discoveryClient.getClass().getMethod("getInstances", String.class);
      java.util.List<?> instances =
          (java.util.List<?>) getInstancesMethod.invoke(discoveryClient, serviceName);

      if (instances == null || instances.isEmpty()) {
        log.warn(
            "No instances found for service '{}' in Eureka, using fallback: {}",
            serviceName,
            fallbackAddress);
        return fallbackAddress;
      }

      // Get the first instance
      Object serviceInstance = instances.get(0);
      Method getHostMethod = serviceInstance.getClass().getMethod("getHost");
      String host = (String) getHostMethod.invoke(serviceInstance);

      log.info("Resolved service '{}' to host: {}", serviceName, host);
      return host;

    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      log.error(
          "Failed to resolve service '{}' from Eureka: {}, using fallback: {}",
          serviceName,
          e.getMessage(),
          fallbackAddress);
      return fallbackAddress;
    }
  }

  /**
   * Resolves the gRPC server address (host:port) from Eureka.
   *
   * @return resolved gRPC address or fallback from properties
   */
  public String resolveGrpcAddress() {
    String serviceName = properties.getEureka().getServiceName();
    int grpcPort = properties.getEureka().getGrpcPort();
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

    String host = resolveServiceAddress(serviceName, fallbackHost);

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
