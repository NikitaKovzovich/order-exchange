package by.bsuir.catalogservice.config;

import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestRabbitMQConfig {

	@Bean
	@Primary
	public RabbitTemplate rabbitTemplate() {
		return Mockito.mock(RabbitTemplate.class);
	}
}
