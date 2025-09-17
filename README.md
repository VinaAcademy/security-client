# Security Client Library

Thư viện Spring Boot gRPC client để xác thực JWT token trong hệ thống microservices VinaAcademy. Thư viện này hoạt động như một shared security client mà các service khác có thể sử dụng để validate JWT tokens thông qua gRPC calls tới platform server trung tâm.

## Tính năng chính

- **JWT Token Validation**: Xác thực JWT tokens thông qua gRPC calls
- **OAuth2 Authentication**: Tự động thêm OAuth2 Bearer tokens vào tất cả gRPC calls  
- **Security Annotations**: Hỗ trợ các annotation như `@PreAuthorize`, `@HasAnyRole`
- **Auto Configuration**: Tự động cấu hình khi được thêm vào Spring Boot application
- **JWT Authentication Filter**: Tự động extract và validate JWT từ Authorization header

## Cài đặt

### 1. Build và install thư viện

```bash
mvn -U clean install
```

### 2. Thêm dependency vào project

```xml
<dependency>
  <groupId>vn.vinaacademy</groupId>
  <artifactId>security-client</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Cấu hình

### 1. Cấu hình trong application.yml/application.properties

```yaml
security:
  oauth2:
    provider:
      token-uri: ${SECURITY_OAUTH2_TOKEN_URI:http://localhost:8080/oauth2/token}
    grpc-client:
      scopes: ${SECURITY_OAUTH2_SCOPE_1:api.read}, ${SECURITY_OAUTH2_SCOPE_2:api.write}
      client-id: ${SECURITY_OAUTH2_GRPC_CLIENT_ID:grpc-client}
      client-secret: ${SECURITY_OAUTH2_GRPC_CLIENT_SECRET:grpc-secret}
  grpc:
    grpc-address: ${SECURITY_JWT_GRPC_ADDRESS:localhost:9090}
    enable-keep-alive: ${SECURITY_JWT_ENABLE_KEEP_ALIVE:true}
    keep-alive-time: ${SECURITY_JWT_KEEP_ALIVE_TIME:30}
    keep-alive-timeout: ${SECURITY_JWT_KEEP_ALIVE_TIMEOUT:5}
```

### 2. Biến môi trường

Bạn có thể cấu hình thông qua các biến môi trường sau:

```bash
# OAuth2 Configuration
SECURITY_OAUTH2_TOKEN_URI=http://your-auth-server:8080/oauth2/token
SECURITY_OAUTH2_GRPC_CLIENT_ID=your-grpc-client-id
SECURITY_OAUTH2_GRPC_CLIENT_SECRET=your-grpc-client-secret
SECURITY_OAUTH2_SCOPE_1=api.read
SECURITY_OAUTH2_SCOPE_2=api.write

# gRPC Configuration  
SECURITY_JWT_GRPC_ADDRESS=your-platform-server:9090
SECURITY_JWT_ENABLE_KEEP_ALIVE=true
SECURITY_JWT_KEEP_ALIVE_TIME=30
SECURITY_JWT_KEEP_ALIVE_TIMEOUT=5
```

## Sử dụng

### 1. Tự động JWT Authentication

Thư viện sẽ tự động:
- Extract JWT token từ `Authorization: Bearer <token>` header
- Validate token thông qua gRPC call tới platform server
- Set up SecurityContext cho request hiện tại
- Skip filter cho health check endpoints (`/actuator/`, `/health`, `/ping`)

```java
// Không cần code thêm - thư viện tự động hoạt động
// JWT sẽ được validate tự động cho mọi request
```

### 2. Sử dụng JwtGrpcClient trực tiếp

```java
import vn.vinaacademy.security.grpc.JwtGrpcClient;
import com.vinaacademy.grpc.ValidateTokenResponse;

@Service
@RequiredArgsConstructor
public class YourService {
    
    private final JwtGrpcClient jwtGrpcClient;
    
    public void validateUserToken(String token) {
        ValidateTokenResponse response = jwtGrpcClient.validateToken(token);
        
        if (response.getIsValid()) {
            System.out.println("User ID: " + response.getUserId());
            System.out.println("Email: " + response.getEmail());
            System.out.println("Full Name: " + response.getFullName());
            System.out.println("Roles: " + response.getRoles());
            System.out.println("Avatar URL: " + response.getAvatarUrl());
        } else {
            System.out.println("Token invalid: " + response.getMessage());
        }
    }
}
```

### 3. Sử dụng Security Annotations

#### @PreAuthorize

```java
import vn.vinaacademy.security.annotation.PreAuthorize;

@RestController
public class AdminController {
    
    // Chỉ ADMIN mới được truy cập
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    // ADMIN hoặc STAFF đều được truy cập
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    @GetMapping("/admin/reports")
    public Report getReports() {
        return reportService.getReports();
    }
    
    // Kiểm tra user ID khớp với parameter
    @PreAuthorize("user.userId == #userId")
    @GetMapping("/users/{userId}/profile")
    public UserProfile getUserProfile(@PathVariable String userId) {
        return userService.getUserProfile(userId);
    }
    
    // Kiểm tra user đã authenticated
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "This is protected";
    }
}
```

#### @HasAnyRole

```java
import vn.vinaacademy.security.annotation.HasAnyRole;

@RestController
public class StaffController {
    
    @HasAnyRole({"ROLE_ADMIN", "ROLE_STAFF"})
    @GetMapping("/staff/dashboard")
    public Dashboard getStaffDashboard() {
        return dashboardService.getStaffDashboard();
    }
    
    @HasAnyRole({"ROLE_MANAGER", "ROLE_SUPERVISOR"})
    @PostMapping("/staff/approve")
    public void approveRequest(@RequestBody ApprovalRequest request) {
        approvalService.approve(request);
    }
}
```

### 4. Truy cập thông tin user hiện tại

```java
import vn.vinaacademy.security.authentication.SecurityContextHolder;
import vn.vinaacademy.security.authentication.UserContext;

@Service
public class UserService {
    
    public void doSomethingWithCurrentUser() {
        UserContext currentUser = SecurityContextHolder.getContext();
        
        if (currentUser != null && currentUser.isAuthenticated()) {
            String userId = currentUser.getUserId();
            String email = currentUser.getEmail();
            String fullName = currentUser.getFullName();
            String avatarUrl = currentUser.getAvatarUrl();
            Set<String> roles = currentUser.getRoles();
            String token = currentUser.getToken();
            
            // Sử dụng thông tin user...
        }
    }
}
```

## Cấu trúc Response từ gRPC

```proto
message ValidateTokenResponse {
  bool isValid = 1;        // Token có hợp lệ không
  string message = 2;      // Thông báo lỗi (nếu có)
  string userId = 3;       // ID của user
  string email = 4;        // Email của user  
  string roles = 5;        // Roles của user (phân cách bằng dấu phẩy)
  string avatarUrl = 6;    // URL avatar của user
  string fullName = 7;     // Tên đầy đủ của user
}
```

## Error Handling

Thư viện sẽ tự động xử lý lỗi:

- Nếu gRPC call thất bại, token sẽ được coi là invalid
- Nếu token không có trong request, user sẽ là unauthenticated
- Logs chi tiết sẽ được ghi ra để debug

```java
// Trong trường hợp lỗi, response sẽ có dạng:
ValidateTokenResponse.newBuilder()
    .setIsValid(false)
    .build();
```

## Supported SpEL Expressions

### Trong @PreAuthorize annotation:

- `hasRole('ROLE_ADMIN')`: Kiểm tra user có role cụ thể
- `hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')`: Kiểm tra user có bất kỳ role nào
- `isAuthenticated()`: Kiểm tra user đã authenticated
- `user.userId == #paramName`: So sánh user ID với parameter
- `user.email == 'admin@example.com'`: So sánh email với giá trị cụ thể

## Endpoint được bỏ qua

Filter sẽ tự động bỏ qua các endpoint sau:
- `/actuator/**` - Spring Boot Actuator endpoints
- `/health` - Health check endpoint
- `/ping` - Ping endpoint

## Requirements

- Java 17+
- Spring Boot 3.4.9+
- gRPC Server chạy để validate JWT tokens
- OAuth2 Authorization Server để lấy access tokens

## Architecture

```
Client Request -> JwtAuthenticationFilter -> gRPC Call -> Platform Server
                       ↓
                SecurityContext được set với UserContext
                       ↓  
                Controller với Security Annotations
```

Thư viện sử dụng OAuth2 Client Credentials flow để authenticate các gRPC calls và tự động thêm `Authorization: Bearer <token>` header vào mọi gRPC request.
