# User Service gRPC Client

## Tổng quan

`UserGrpcClient` là một Spring Bean cung cấp khả năng gọi User Service thông qua gRPC để lấy thông tin người dùng từ platform server.

## Cách sử dụng

### 1. Inject UserGrpcClient vào service của bạn

```java
@Service
@RequiredArgsConstructor
public class YourService {
    
    private final UserGrpcClient userGrpcClient;
    
    public void processUser(String userId) {
        // Gọi User Service để lấy thông tin user
        GetUserByIdResponse response = userGrpcClient.getUserById(userId);
        
        if (response.getSuccess()) {
            UserInfo user = response.getUser();
            // Xử lý thông tin user
            System.out.println("User email: " + user.getEmail());
            System.out.println("User name: " + user.getFullName());
            // ... các thông tin khác
        } else {
            System.out.println("Error: " + response.getMessage());
        }
    }
}
```

### 2. Sử dụng trong Controller

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserGrpcClient userGrpcClient;
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfo> getUserInfo(@PathVariable String userId) {
        GetUserByIdResponse response = userGrpcClient.getUserById(userId);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(response.getUser());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
```

## Cấu hình

UserGrpcClient sử dụng cùng cấu hình gRPC với JwtGrpcClient:

```yaml
vn.vinaacademy.security.grpc:
  grpc-address: ${PLATFORM_GRPC_URL:localhost:9090}
  enable-keep-alive: true
  keep-alive-time: 30
  keep-alive-timeout: 5

oauth2:
  client:
    registration:
      grpc-client:
        client-id: ${GRPC_CLIENT_ID:your-client-id}
        client-secret: ${GRPC_CLIENT_SECRET:your-client-secret}
        authorization-grant-type: client_credentials
        provider: as-vina
    provider:
      as-vina:
        token-uri: ${AS_VINA_TOKEN_URI:http://localhost:8080/auth/oauth/token}
```

## UserInfo Structure

```java
public class UserInfo {
    private String id;              // User ID
    private String email;           // Email address
    private String username;        // Username
    private String phone;           // Phone number
    private String avatarUrl;       // Avatar image URL
    private String fullName;        // Full name
    private String description;     // User description
    private boolean isCollaborator; // Collaborator status
    private String birthday;        // Birthday
    private List<String> roles;     // List of user roles
    private boolean enabled;        // Account enabled status
    private boolean isUsing2fa;     // 2FA enabled status
}
```

## Error Handling

UserGrpcClient tự động handle các lỗi gRPC và trả về response với `success = false`:

```java
GetUserByIdResponse response = userGrpcClient.getUserById("invalid-user");

if (!response.getSuccess()) {
    String errorMessage = response.getMessage();
    // Xử lý lỗi
}
```

## Logging

UserGrpcClient sử dụng SLF4J logger với các mức log:
- `DEBUG`: Log chi tiết các gRPC calls
- `WARN`: Log khi gRPC call thành công nhưng không tìm thấy user
- `ERROR`: Log khi có exception xảy ra

## Auto Configuration

UserGrpcClient được tự động configure thông qua `SecurityClientAutoConfiguration`. Không cần cấu hình thêm gì, chỉ cần ensure rằng:

1. Library này được include trong classpath
2. Các environment variables được set đúng
3. Platform server đang chạy và accessible

## Dependencies

UserGrpcClient phụ thuộc vào:
- `UserServiceBlockingStub`: Auto-configured gRPC stub
- `OAuth2GrpcClientInterceptor`: Tự động add Bearer token vào gRPC headers
- Generated protobuf classes từ `user_service.proto`
