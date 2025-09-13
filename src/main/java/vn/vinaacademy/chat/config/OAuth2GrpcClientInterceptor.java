package vn.vinaacademy.chat.config;

import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import java.util.List;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2GrpcClientInterceptor implements ClientInterceptor {
  private static final String TOKEN_KEY = "chat-service-key";
  private static final String TOKEN_PRINCIPAL = "internal-service";
  private static final String TOKEN_ROLE = "ROLE_INTERNAL_SERVICE";

  private final OAuth2AuthorizedClientManager manager;

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
    log.debug("Intercepting gRPC call to method: {}", methodDescriptor.getFullMethodName());
    var principal =
        new AnonymousAuthenticationToken(
            TOKEN_KEY, TOKEN_PRINCIPAL, List.of(new SimpleGrantedAuthority(TOKEN_ROLE)));

    var request =
        OAuth2AuthorizeRequest.withClientRegistrationId(OAuth2ClientConfig.CLIENT_REGISTRATION_ID)
            .principal(principal)
            .build();

    OAuth2AuthorizedClient client = manager.authorize(request);
    if (client == null || client.getAccessToken() == null) {
      log.error("Failed to authorize gRPC client");
      throw new IllegalStateException("Failed to obtain access token for gRPC call");
    }

    String tokenValue = client.getAccessToken().getTokenValue();

    // add Authorization header
    Metadata headers = new Metadata();
    Metadata.Key<String> AUTHORIZATION =
        Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    headers.put(AUTHORIZATION, "Bearer " + tokenValue);

    CallCredentials callCredentials =
        new CallCredentials() {
          @Override
          public void applyRequestMetadata(
              RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
            executor.execute(() -> metadataApplier.apply(headers));
          }
        };

    return channel.newCall(methodDescriptor, callOptions.withCallCredentials(callCredentials));
  }
}
