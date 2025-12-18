package by.bsuir.orderservice.config;

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

	public static final String ORDER_EXCHANGE = "order.events";
	public static final String CATALOG_EXCHANGE = "catalog.events";

	public static final String ORDER_CREATED_QUEUE = "order.created.queue";
	public static final String ORDER_CONFIRMED_QUEUE = "order.confirmed.queue";
	public static final String STOCK_RESERVE_QUEUE = "stock.reserve.queue";

	@Bean
	public TopicExchange orderExchange() {
		return new TopicExchange(ORDER_EXCHANGE);
	}

	@Bean
	public Queue orderCreatedQueue() {
		return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
	}

	@Bean
	public Queue orderConfirmedQueue() {
		return QueueBuilder.durable(ORDER_CONFIRMED_QUEUE).build();
	}

	@Bean
	public Queue stockReserveQueue() {
		return QueueBuilder.durable(STOCK_RESERVE_QUEUE).build();
	}

	@Bean
	public Binding orderCreatedBinding() {
		return BindingBuilder.bind(orderCreatedQueue())
				.to(orderExchange())
				.with("ordercreated");
	}

	@Bean
	public Binding orderConfirmedBinding() {
		return BindingBuilder.bind(orderConfirmedQueue())
				.to(orderExchange())
				.with("orderconfirmed");
	}

	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter());
		return template;
	}
}
