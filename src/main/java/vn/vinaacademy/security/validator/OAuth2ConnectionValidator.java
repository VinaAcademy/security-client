package vn.vinaacademy.security.validator;

import static vn.vinaacademy.security.config.OAuth2ClientConfig.CLIENT_REGISTRATION_ID;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2ConnectionValidator {
  private final OAuth2AuthorizedClientManager clientManager;

  @PostConstruct
  public void validateOAuth2Connection() {
    OAuth2AuthorizeRequest request =
        OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_REGISTRATION_ID)
            .principal("startup-check")
            .build();

    try {
      OAuth2AuthorizedClient client = clientManager.authorize(request);
      if (client == null || client.getAccessToken() == null) {
        throw new IllegalStateException("❌ Failed to retrieve access token from OAuth2 server.");
      }
      log.info("✅ Connected to OAuth2 server successfully.");
    } catch (Exception e) {
      log.error("❌ OAuth2 connection failed: {}", e.getMessage());
      throw new IllegalStateException("Cannot connect to OAuth2 server: " + e.getMessage(), e);
    }
  }
}
