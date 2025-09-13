package vn.vinaacademy.chat.grpc;

import com.vinaacademy.grpc.JwtServiceGrpc.JwtServiceBlockingStub;
import com.vinaacademy.grpc.TokenRequest;
import com.vinaacademy.grpc.ValidateTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtGrpcClient {
  private final JwtServiceBlockingStub stub;

  public ValidateTokenResponse validateToken(String jwtToken) {
    try {
      TokenRequest request = TokenRequest.newBuilder().setToken(jwtToken).build();
      return stub.validateToken(request);
    } catch (Exception e) {
      log.error("Error validating token via gRPC: {}", e.getMessage());
      return ValidateTokenResponse.newBuilder().setIsValid(false).build();
    }
  }
}
