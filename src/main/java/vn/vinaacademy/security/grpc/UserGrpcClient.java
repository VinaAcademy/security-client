package vn.vinaacademy.security.grpc;

import com.vinaacademy.grpc.GetUserByIdRequest;
import com.vinaacademy.grpc.GetUserByIdResponse;
import com.vinaacademy.grpc.GetUserByIdsRequest;
import com.vinaacademy.grpc.GetUserByIdsResponse;
import com.vinaacademy.grpc.UserServiceGrpc;
import com.vinaacademy.grpc.UserServiceGrpc.UserServiceBlockingStub;
import io.grpc.ManagedChannel;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.vinaacademy.security.config.grpc.GrpcChannelFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGrpcClient {
  private final GrpcChannelFactory channelFactory;

  /**
   * Get user information by user ID via gRPC call to platform server
   *
   * @param userId the user ID to fetch information for
   * @return GetUserByIdResponse containing user information or error response
   */
  public GetUserByIdResponse getUserById(String userId) {
    ManagedChannel channel;
    try {
      channel = channelFactory.createAuthChannel();
      UserServiceBlockingStub userServiceStub = UserServiceGrpc.newBlockingStub(channel);

      GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setUserId(userId).build();

      log.debug("Fetching user info for userId: {}", userId);
      GetUserByIdResponse response = userServiceStub.getUserById(request);

      if (response.getSuccess()) {
        log.debug("Successfully fetched user info for userId: {}", userId);
      } else {
        log.warn(
            "Failed to fetch user info for userId: {}. Message: {}", userId, response.getMessage());
      }

      return response;
    } catch (Exception e) {
      log.error(
          "Error fetching user info via gRPC for userId: {}. Error: {}", userId, e.getMessage());

      // Return error response
      return GetUserByIdResponse.newBuilder()
          .setSuccess(false)
          .setMessage("Failed to fetch user information: " + e.getMessage())
          .build();
    }
  }

  public GetUserByIdResponse getUserById(UUID userId) {
    return getUserById(String.valueOf(userId));
  }

  /**
   * Get multiple users information by user IDs via gRPC call to platform server
   *
   * @param userIds the list of user IDs to fetch information for
   * @return GetUserByIdsResponse containing users information or error response
   */
  public GetUserByIdsResponse getUserByIds(List<String> userIds) {
    ManagedChannel channel;
    try {
      channel = channelFactory.createAuthChannel();
      UserServiceBlockingStub userServiceStub = UserServiceGrpc.newBlockingStub(channel);

      GetUserByIdsRequest request = GetUserByIdsRequest.newBuilder().addAllUserIds(userIds).build();
      log.debug("Fetching user info for userIds: {}", userIds);
      GetUserByIdsResponse response = userServiceStub.getUserByIds(request);

      if (response.getSuccess()) {
        log.debug("Successfully fetched user info for {} users", response.getUsersCount());
        if (!response.getNotFoundIdsList().isEmpty()) {
          log.warn("Some user IDs were not found: {}", response.getNotFoundIdsList());
        }
      } else {
        log.warn(
            "Failed to fetch user info for userIds: {}. Message: {}",
            userIds,
            response.getMessage());
      }

      return response;
    } catch (Exception e) {
      log.error(
          "Error fetching users info via gRPC for userIds: {}. Error: {}", userIds, e.getMessage());

      // Return error response
      return GetUserByIdsResponse.newBuilder()
          .setSuccess(false)
          .setMessage("Failed to fetch users information: " + e.getMessage())
          .build();
    }
  }

  public GetUserByIdsResponse getUserByIdList(List<UUID> userIds) {
    List<String> stringIds = userIds.stream().map(String::valueOf).toList();
    return getUserByIds(stringIds);
  }
}
