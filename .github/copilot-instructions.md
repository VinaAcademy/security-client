# Security Client Library - AI Coding Instructions

## Architecture Overview
This is a Spring Boot gRPC client library for JWT token validation within the VinaAcademy microservices ecosystem. It acts as a shared security client that other services can use to validate JWT tokens via gRPC calls to a central platform server.

**Key Components:**
- `JwtGrpcClient`: Main service for token validation via `platform-server` gRPC channel
- `OAuth2GrpcClientInterceptor`: Automatically adds OAuth2 Bearer tokens to all gRPC calls
- Generated gRPC stubs from `jwt_service.proto` (located in `src/main/proto/`)

## Critical Integration Patterns

### gRPC + OAuth2 Authentication Flow
All gRPC calls are automatically authenticated using client credentials flow:
```java
// OAuth2GrpcClientInterceptor adds "Authorization: Bearer <token>" header
// TokenRequest -> platform-server:9090 -> ValidateTokenResponse
```
Channel configuration in `application.yml` uses `platform-server` name, not direct URLs.

### Proto-Generated Classes
The `com.vinaacademy.grpc` package is auto-generated from protobuf. Key classes:
- `JwtServiceGrpc.JwtServiceBlockingStub` - synchronous client
- `TokenRequest`/`ValidateTokenResponse` - message types
- Generated via `protobuf-maven-plugin` in Maven build

### Environment Configuration
Service relies on these environment variables:
- `PLATFORM_GRPC_URL` (default: localhost:9090) - gRPC server address
- `GRPC_CLIENT_ID`/`GRPC_CLIENT_SECRET` - OAuth2 credentials
- `AS_VINA_TOKEN_URI` - OAuth2 token endpoint URL

## Development Workflows

### Proto Changes
When modifying `src/main/proto/com/vinaacademy/grpc/jwt_service.proto`:
```bash
mvn clean compile  # Regenerates Java classes in target/generated-sources/protobuf/
```

### Testing gRPC Integration
The `SecurityApplication` includes a CommandLineRunner that validates an "invalid-token" on startup - useful for integration testing.

### Building & Running
```bash
mvn clean package                    # Creates security-client-0.0.1-SNAPSHOT.jar
mvn spring-boot:run                  # Runs on port 8081 with Swagger UI at /
```

## Project-Specific Conventions

### Package Structure
- `vn.vinaacademy.security.grpc` - gRPC client implementations
- `vn.vinaacademy.security.config` - Spring configuration (gRPC channels, OAuth2)
- `com.vinaacademy.grpc` - Auto-generated protobuf classes (don't edit manually)

### Error Handling Pattern
gRPC calls use try-catch with fallback responses:
```java
// JwtGrpcClient.validateToken() returns isValid=false on any gRPC exception
return ValidateTokenResponse.newBuilder().setIsValid(false).build();
```

### OAuth2 Client Registration
Uses Spring Security OAuth2 with "grpc-client" registration ID. The `OAuth2GrpcClientInterceptor` creates anonymous authentication tokens for service-to-service calls.

## Key Dependencies
- Spring Boot 3.4.9 with Java 17
- Spring gRPC 0.8.0 for gRPC integration
- Spring Security OAuth2 Client for authentication
- Lombok for boilerplate reduction
- Springdoc OpenAPI for API documentation

When adding new gRPC methods, update the proto file first, then run Maven compile to regenerate Java classes.
