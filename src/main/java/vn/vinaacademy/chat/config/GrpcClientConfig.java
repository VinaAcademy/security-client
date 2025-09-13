package vn.vinaacademy.chat.config;

import com.vinaacademy.grpc.JwtServiceGrpc;
import com.vinaacademy.grpc.JwtServiceGrpc.JwtServiceBlockingStub;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
@RequiredArgsConstructor
public class GrpcClientConfig {
  private final OAuth2GrpcClientInterceptor interceptor;

  @Bean
  JwtServiceBlockingStub stub(GrpcChannelFactory channels) {
    return JwtServiceGrpc.newBlockingStub(
        channels.createChannel(
            "platform-server",
            ChannelBuilderOptions.defaults().withInterceptors(List.of(interceptor))));
  }
}
