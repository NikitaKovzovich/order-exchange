package by.bsuir.chatservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String CHAT_EXCHANGE = "chat.events";
	public static final String SUPPORT_EXCHANGE = "support.events";
	public static final String ORDER_EXCHANGE = "order.events";

	public static final String ORDER_CREATED_QUEUE = "chat.order.created";
	public static final String ORDER_CLOSED_QUEUE = "chat.order.closed";

	@Bean
	public TopicExchange chatExchange() {
		return new TopicExchange(CHAT_EXCHANGE);
	}

	@Bean
	public TopicExchange supportExchange() {
		return new TopicExchange(SUPPORT_EXCHANGE);
	}

	@Bean
	public TopicExchange orderExchange() {
		return new TopicExchange(ORDER_EXCHANGE);
	}

	@Bean
	public Queue orderCreatedQueue() {
		return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
	}

	@Bean
	public Queue orderClosedQueue() {
		return QueueBuilder.durable(ORDER_CLOSED_QUEUE).build();
	}

	@Bean
	public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange orderExchange) {
		return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with("ordercreated");
	}

	@Bean
	public Binding orderClosedBinding(Queue orderClosedQueue, TopicExchange orderExchange) {
		return BindingBuilder.bind(orderClosedQueue).to(orderExchange).with("orderclosed");
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

