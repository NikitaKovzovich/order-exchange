package by.bsuir.authservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

	public static final String AUTH_EXCHANGE = "auth.events";
	public static final String USER_REGISTERED_QUEUE = "auth.user.registered";
	public static final String VERIFICATION_APPROVED_QUEUE = "auth.verification.approved";
	public static final String VERIFICATION_REJECTED_QUEUE = "auth.verification.rejected";

	@Bean
	public TopicExchange authExchange() {
		return new TopicExchange(AUTH_EXCHANGE);
	}

	@Bean
	public Queue userRegisteredQueue() {
		return QueueBuilder.durable(USER_REGISTERED_QUEUE).build();
	}

	@Bean
	public Queue verificationApprovedQueue() {
		return QueueBuilder.durable(VERIFICATION_APPROVED_QUEUE).build();
	}

	@Bean
	public Queue verificationRejectedQueue() {
		return QueueBuilder.durable(VERIFICATION_REJECTED_QUEUE).build();
	}

	@Bean
	public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange authExchange) {
		return BindingBuilder.bind(userRegisteredQueue).to(authExchange).with("userregistered");
	}

	@Bean
	public Binding verificationApprovedBinding(Queue verificationApprovedQueue, TopicExchange authExchange) {
		return BindingBuilder.bind(verificationApprovedQueue).to(authExchange).with("verificationapproved");
	}

	@Bean
	public Binding verificationRejectedBinding(Queue verificationRejectedQueue, TopicExchange authExchange) {
		return BindingBuilder.bind(verificationRejectedQueue).to(authExchange).with("verificationrejected");
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}
}

