package hello.integration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 설정
        registry.addEndpoint("/ws")  // WebSocket 연결 엔드포인트
                .setAllowedOrigins("http://localhost:3000")  // React 앱의 주소
                .withSockJS();  // SockJS 지원 활성화
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정
        registry.setApplicationDestinationPrefixes("/app");  // 클라이언트에서 서버로 메시지 보낼 때의 prefix
        registry.enableSimpleBroker("/topic");  // 서버에서 클라이언트로 메시지 보낼 때의 prefix
    }
}
