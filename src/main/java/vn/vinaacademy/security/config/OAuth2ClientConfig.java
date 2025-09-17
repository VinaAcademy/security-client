package vn.vinaacademy.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;
import vn.vinaacademy.security.properties.SecurityClientProperties;

@Configuration
@RequiredArgsConstructor
public class OAuth2ClientConfig {
  private final SecurityClientProperties securityClientProperties;

  public static final String CLIENT_REGISTRATION_ID = "grpc-client";

  @Bean
  OAuth2AuthorizedClientService authorizedClientService(
      ClientRegistrationRepository registrations) {
    return new InMemoryOAuth2AuthorizedClientService(registrations);
  }

  @Bean
  OAuth2AuthorizedClientManager authorizedClientManager(
      ClientRegistrationRepository clients, OAuth2AuthorizedClientService authorizedClientService) {

    var provider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();

    var manager =
        new AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, authorizedClientService);
    manager.setAuthorizedClientProvider(provider);
    return manager;
  }

  @Bean
  ClientRegistrationRepository clientRegistrationRepository() {
    var grpcClient = securityClientProperties.getOauth2().getGrpcClient();
    ClientRegistration registration =
        ClientRegistration.withRegistrationId(CLIENT_REGISTRATION_ID)
            .tokenUri(securityClientProperties.getOauth2().getProvider().getTokenUri())
            .clientId(grpcClient.getClientId())
            .clientSecret(grpcClient.getClientSecret())
            .scope(grpcClient.getScopes())
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .build();

    return new InMemoryClientRegistrationRepository(registration);
  }

  @Bean
  WebClient oauth2WebClient(OAuth2AuthorizedClientManager manager) {
    var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
    oauth.setDefaultClientRegistrationId(CLIENT_REGISTRATION_ID);

    return WebClient.builder().apply(oauth.oauth2Configuration()).build();
  }
}
