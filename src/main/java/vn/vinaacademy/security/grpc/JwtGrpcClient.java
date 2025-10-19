package vn.vinaacademy.security.grpc;

import com.vinaacademy.grpc.JwtServiceGrpc;
import com.vinaacademy.grpc.JwtServiceGrpc.JwtServiceBlockingStub;
import com.vinaacademy.grpc.TokenRequest;
import com.vinaacademy.grpc.ValidateTokenResponse;
import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.vinaacademy.security.config.grpc.GrpcChannelFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtGrpcClient {

  private final GrpcChannelFactory channelFactory;

  public ValidateTokenResponse validateToken(String jwtToken) {
    ManagedChannel channel = null;
    try {
      channel = channelFactory.createAuthChannel();

      JwtServiceBlockingStub stub = JwtServiceGrpc.newBlockingStub(channel);
      TokenRequest request = TokenRequest.newBuilder().setToken(jwtToken).build();
      return stub.validateToken(request);
    } catch (Exception e) {
      log.error("Error validating token via gRPC: {}", e.getMessage());
      return ValidateTokenResponse.newBuilder()
          .setIsValid(false)
          .setMessage("Failed to validate token: " + e.getMessage())
          .build();
    } finally {
      if (channel != null) channel.shutdownNow();
    }
  }
}
