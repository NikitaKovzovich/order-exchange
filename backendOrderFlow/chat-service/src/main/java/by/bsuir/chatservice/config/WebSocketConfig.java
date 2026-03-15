package by.bsuir.chatservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;





@Configuration
@ConditionalOnProperty(name = "websocket.enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketConfig {

	@Configuration
	@EnableWebSocketMessageBroker
	static class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

		@Override
		public void configureMessageBroker(MessageBrokerRegistry config) {
			config.enableSimpleBroker("/topic");
			config.setApplicationDestinationPrefixes("/app");
		}

		@Override
		public void registerStompEndpoints(StompEndpointRegistry registry) {
			registry.addEndpoint("/ws")
					.setAllowedOriginPatterns("*")
					.withSockJS();
			registry.addEndpoint("/ws")
					.setAllowedOriginPatterns("*");
		}
	}
}
