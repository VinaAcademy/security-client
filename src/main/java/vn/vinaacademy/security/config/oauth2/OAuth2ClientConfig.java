package vn.vinaacademy.security.config.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import vn.vinaacademy.security.discovery.EurekaServiceDiscovery;
import vn.vinaacademy.security.properties.SecurityClientProperties;

@Configuration
@RequiredArgsConstructor
public class OAuth2ClientConfig {
  @Getter private final SecurityClientProperties securityClientProperties;

  @Setter
  @Getter
  @Autowired(required = false)
  private EurekaServiceDiscovery eurekaServiceDiscovery;

  public static final String CLIENT_REGISTRATION_ID = "grpc-client";

  @Bean
  public OAuth2AuthorizedClientManager authorizedClientManager(
      ClientRegistrationRepository clients, OAuth2AuthorizedClientService authorizedClientService) {

    var provider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();

    var manager =
        new AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, authorizedClientService);
    manager.setAuthorizedClientProvider(provider);
    return manager;
  }

  @Bean
  WebClient oauth2WebClient(OAuth2AuthorizedClientManager manager) {
    var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
    oauth.setDefaultClientRegistrationId(CLIENT_REGISTRATION_ID);

    return WebClient.builder().apply(oauth.oauth2Configuration()).build();
  }
}
