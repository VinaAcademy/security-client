# Security Client Library - AI Coding Instructions

## Architecture Overview

This is a Spring Boot gRPC client library for JWT token validation within the VinaAcademy
microservices ecosystem. It acts as a shared security client that other services can use to validate
JWT tokens via gRPC calls to a central platform server. Supports both direct connection and Eureka
service discovery.

**Key Components:**

- `JwtGrpcClient`: Main service for token validation via `platform-server` gRPC channel
- `UserGrpcClient`: Service for fetching user information via gRPC
- `OAuth2GrpcClientInterceptor`: Automatically adds OAuth2 Bearer tokens to all gRPC calls
- `EurekaServiceDiscovery`: Resolves service addresses from Eureka (optional, uses reflection)
- Generated gRPC stubs from `jwt_service.proto` and `user_service.proto` (located in `src/main/proto/`)
- `GrpcClientConfig`: Configures gRPC channel with optional Eureka service discovery
- `OAuth2ClientConfig`: Configures OAuth2 client with optional Eureka token URI resolution

## Critical Integration Patterns

### gRPC + OAuth2 Authentication Flow

All gRPC calls are automatically authenticated using client credentials flow:

```java
// OAuth2GrpcClientInterceptor adds "Authorization: Bearer <token>" header
// TokenRequest -> platform-server:9090 -> ValidateTokenResponse
```

Channel configuration supports both direct address and Eureka service name resolution.

### Eureka Service Discovery (Optional)

When enabled, the library automatically resolves both gRPC and OAuth2 server addresses via Eureka:

```yaml
security:
  eureka:
    enabled: true
    service-name: platform-server  # Service name for gRPC
    grpc-port: 9090
  oauth2:
    provider:
      service-name: platform-server  # Service name for OAuth2
      token-uri: http://localhost:8080/oauth2/token  # Fallback if Eureka fails
```

`EurekaServiceDiscovery` uses reflection to call `DiscoveryClient.getInstances()` to avoid compile-time
dependency on Spring Cloud. Falls back to configured addresses if Eureka is unavailable or disabled.

Key methods:
- `resolveGrpcAddress()`: Returns `host:port` for gRPC channel
- `resolveOAuth2TokenUri()`: Returns full OAuth2 token URI with resolved host

### Proto-Generated Classes

The `com.vinaacademy.grpc` package is auto-generated from protobuf. Key classes:

- `JwtServiceGrpc.JwtServiceBlockingStub` - synchronous client
- `TokenRequest`/`ValidateTokenResponse` - message types
- Generated via `protobuf-maven-plugin` in Maven build

### Environment Configuration

Service relies on these environment variables:

- `SECURITY_JWT_GRPC_ADDRESS` (default: localhost:9090) - gRPC server address (fallback)
- `SECURITY_OAUTH2_GRPC_CLIENT_ID`/`SECURITY_OAUTH2_GRPC_CLIENT_SECRET` - OAuth2 credentials
- `SECURITY_OAUTH2_TOKEN_URI` (default: http://localhost:8080/oauth2/token) - OAuth2 token endpoint (fallback)
- `SECURITY_OAUTH2_PROVIDER_SERVICE_NAME` (default: platform-server) - OAuth2 service name in Eureka
- `SECURITY_EUREKA_ENABLED` (default: false) - Enable Eureka service discovery
- `SECURITY_EUREKA_SERVICE_NAME` (default: platform-server) - gRPC service name in Eureka
- `SECURITY_EUREKA_GRPC_PORT` (default: 9090) - gRPC port on discovered service
- `EUREKA_SERVER_URL` (default: http://localhost:8761/eureka/) - Eureka server URL

## Development Workflows

### Proto Changes

When modifying `src/main/proto/com/vinaacademy/grpc/jwt_service.proto`:

```bash
mvn clean compile  # Regenerates Java classes in target/generated-sources/protobuf/
```

### Testing gRPC Integration

The library auto-configures when added as a dependency. Test with both direct and Eureka modes.

### Building & Publishing

```bash
mvn clean install                    # Install to local Maven repository
mvn clean package                    # Creates security-client-1.0.0.jar
git tag v1.0.0 && git push --tags   # Tag for JitPack release
```

### JitPack Integration

Library can be consumed via JitPack:

```xml

<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
<groupId>com.github.VinaAcademy</groupId>
<artifactId>security-client-library</artifactId>
<version>1.0.0</version>
</dependency>
```

## Project-Specific Conventions

### Package Structure

- `vn.vinaacademy.security.grpc` - gRPC client implementations
- `vn.vinaacademy.security.config` - Spring configuration (gRPC channels, OAuth2, Eureka)
- `vn.vinaacademy.security.properties` - Configuration properties
- `com.vinaacademy.grpc` - Auto-generated protobuf classes (don't edit manually)

### Error Handling Pattern

gRPC calls use try-catch with fallback responses:

```java
// JwtGrpcClient.validateToken() returns isValid=false on any gRPC exception
return ValidateTokenResponse.newBuilder().

setIsValid(false).

build();
```

Eureka resolution failures fall back to configured address with warning logs.

### OAuth2 Client Registration

Uses Spring Security OAuth2 with "grpc-client" registration ID. The `OAuth2GrpcClientInterceptor`
creates anonymous authentication tokens for service-to-service calls.

### Eureka Integration Pattern

Uses reflection to call DiscoveryClient to avoid compile-time dependency:

```java
// Gracefully handles case when Spring Cloud is not on classpath
var getInstancesMethod = discoveryClient.getClass().getMethod("getInstances", String.class);
var instances = (java.util.List<?>) getInstancesMethod.invoke(discoveryClient, serviceName);
```

## Key Dependencies

- Spring Boot 3.4.9 with Java 17
- Spring gRPC 0.8.0 for gRPC integration
- Spring Cloud 2023.0.3 for Eureka Client (optional)
- Spring Security OAuth2 Client for authentication
- Lombok for boilerplate reduction

## Configuration Properties

All properties under `security` prefix in `SecurityClientProperties`:

- `security.grpc.*` - gRPC connection settings
- `security.oauth2.*` - OAuth2 client credentials
- `security.eureka.*` - Eureka service discovery settings

When adding new gRPC methods, update the proto file first, then run Maven compile to regenerate Java
classes.

## Publishing Notes

- Parent POM: `com.github.VinaAcademy:vinaacademy-parent:2.0.0` (from JitPack)
- JitPack repository already configured in `<repositories>`
- Eureka Client marked as `<optional>true</optional>` to allow usage without Spring Cloud
- Library works standalone or with Eureka based on configuration
- `security.oauth2.*` - OAuth2 client credentials
- `security.eureka.*` - Eureka service discovery settings

When adding new gRPC methods, update the proto file first, then run Maven compile to regenerate Java
classes.

## Publishing Notes

- Parent POM: `com.github.VinaAcademy:vinaacademy-parent:2.0.0` (from JitPack)
- JitPack repository already configured in `<repositories>`
- Eureka Client marked as `<optional>true</optional>` to allow usage without Spring Cloud
- Library works standalone or with Eureka based on configuration
