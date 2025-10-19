package vn.vinaacademy.security.validator;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.vinaacademy.security.grpc.UserGrpcClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrpcConnectionValidator {
  private final UserGrpcClient userGrpcClient;

  @PostConstruct
  public void validateGrpcConnection() {
    log.info("🔍 Validating gRPC connection to Auth Server...");
    var response = userGrpcClient.getUserByIds(List.of());

    if (response.getSuccess()) {
      log.info("✅ gRPC connection to Auth Server is healthy.");
    } else {
      log.error("❌ gRPC connection to Auth Server failed: {}", response.getMessage());
      throw new IllegalStateException(
          "Cannot connect to Auth Server via gRPC: " + response.getMessage());
    }
  }
}
