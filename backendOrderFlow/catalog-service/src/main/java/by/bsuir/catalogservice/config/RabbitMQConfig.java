package by.bsuir.catalogservice.config;

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
	public static final String CATALOG_EXCHANGE = "catalog.exchange";
	public static final String ORDER_EXCHANGE = "order.events";
	public static final String INVENTORY_RESERVED_QUEUE = "inventory.reserved.queue";
	public static final String INVENTORY_RELEASED_QUEUE = "inventory.released.queue";
	public static final String STOCK_RESERVE_QUEUE = "stock.reserve.queue";
	public static final String RPC_EXCHANGE = "rpc.exchange";

	@Bean
	public DirectExchange rpcExchange() {
		return new DirectExchange(RPC_EXCHANGE);
	}

	@Bean
	public TopicExchange catalogExchange() {
		return new TopicExchange(CATALOG_EXCHANGE);
	}

	@Bean
	public TopicExchange orderExchange() {
		return new TopicExchange(ORDER_EXCHANGE);
	}

	@Bean
	public Queue inventoryReservedQueue() {
		return QueueBuilder.durable(INVENTORY_RESERVED_QUEUE).build();
	}

	@Bean
	public Queue inventoryReleasedQueue() {
		return QueueBuilder.durable(INVENTORY_RELEASED_QUEUE).build();
	}

	@Bean
	public Queue stockReserveQueue() {
		return QueueBuilder.durable(STOCK_RESERVE_QUEUE).build();
	}

	@Bean
	public Binding inventoryReservedBinding(Queue inventoryReservedQueue, TopicExchange catalogExchange) {
		return BindingBuilder.bind(inventoryReservedQueue).to(catalogExchange).with("inventory.reserved");
	}

	@Bean
	public Binding inventoryReleasedBinding(Queue inventoryReleasedQueue, TopicExchange catalogExchange) {
		return BindingBuilder.bind(inventoryReleasedQueue).to(catalogExchange).with("inventory.released");
	}

	@Bean
	public Binding stockReserveBinding() {
		return BindingBuilder.bind(stockReserveQueue()).to(orderExchange()).with("stockreserverequest");
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter());
		template.setReplyTimeout(30000);
		return template;
	}
}
