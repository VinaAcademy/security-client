package vn.vinaacademy.security.config.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;
import vn.vinaacademy.security.discovery.EurekaServiceDiscovery;
import vn.vinaacademy.security.properties.SecurityClientProperties;

@Component
@RequiredArgsConstructor
public class EurekaAwareClientRegistrationRepository implements ClientRegistrationRepository {

  private final SecurityClientProperties properties;
  private final EurekaServiceDiscovery eurekaServiceDiscovery;

  @Override
  public ClientRegistration findByRegistrationId(String registrationId) {
    var grpcClient = properties.getOauth2().getGrpcClient();

    String tokenUri = properties.getOauth2().getProvider().getTokenUri();
    if (eurekaServiceDiscovery != null && properties.getEureka().isEnabled()) {
      tokenUri = eurekaServiceDiscovery.resolveOAuth2TokenUri();
    }

    return ClientRegistration.withRegistrationId(registrationId)
        .tokenUri(tokenUri)
        .clientId(grpcClient.getClientId())
        .clientSecret(grpcClient.getClientSecret())
        .scope(grpcClient.getScopes())
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .build();
  }
}
