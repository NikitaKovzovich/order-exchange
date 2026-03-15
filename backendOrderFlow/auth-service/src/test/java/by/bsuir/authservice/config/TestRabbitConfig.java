package by.bsuir.authservice.config;

import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;




@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "false")
public class TestRabbitConfig {

	@Bean
	@Primary
	public RabbitTemplate rabbitTemplate() {
		return Mockito.mock(RabbitTemplate.class);
	}
}
