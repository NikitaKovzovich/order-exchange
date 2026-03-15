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
	public static final String SUPPORT_EXCHANGE = "support.events";
	public static final String USER_REGISTERED_QUEUE = "auth.user.registered";
	public static final String VERIFICATION_APPROVED_QUEUE = "auth.verification.approved";
	public static final String VERIFICATION_REJECTED_QUEUE = "auth.verification.rejected";
	public static final String TICKET_CREATED_QUEUE = "auth.support.ticket.created";
	public static final String TICKET_MESSAGE_QUEUE = "auth.support.ticket.message";


	public static final String ORDER_EVENTS_EXCHANGE = "order.events";
	public static final String ORDER_NOTIFICATION_QUEUE = "auth.order.notification";


	public static final String RPC_EXCHANGE = "rpc.exchange";
	public static final String RPC_GET_COMPANY_NAME = "rpc.auth.getCompanyName";
	public static final String RPC_GET_ALL_SUPPLIERS = "rpc.auth.getAllSupplierCompanies";

	@Bean
	public DirectExchange rpcExchange() {
		return new DirectExchange(RPC_EXCHANGE);
	}

	@Bean
	public Queue rpcGetCompanyNameQueue() {
		return QueueBuilder.durable(RPC_GET_COMPANY_NAME).build();
	}

	@Bean
	public Binding rpcGetCompanyNameBinding() {
		return BindingBuilder.bind(rpcGetCompanyNameQueue()).to(rpcExchange()).with(RPC_GET_COMPANY_NAME);
	}

	@Bean
	public Queue rpcGetAllSuppliersQueue() {
		return QueueBuilder.durable(RPC_GET_ALL_SUPPLIERS).build();
	}

	@Bean
	public Binding rpcGetAllSuppliersBinding() {
		return BindingBuilder.bind(rpcGetAllSuppliersQueue()).to(rpcExchange()).with(RPC_GET_ALL_SUPPLIERS);
	}

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
	public TopicExchange supportExchange() {
		return new TopicExchange(SUPPORT_EXCHANGE);
	}

	@Bean
	public Queue ticketCreatedQueue() {
		return QueueBuilder.durable(TICKET_CREATED_QUEUE).build();
	}

	@Bean
	public Queue ticketMessageQueue() {
		return QueueBuilder.durable(TICKET_MESSAGE_QUEUE).build();
	}

	@Bean
	public Binding ticketCreatedBinding(Queue ticketCreatedQueue, TopicExchange supportExchange) {
		return BindingBuilder.bind(ticketCreatedQueue).to(supportExchange).with("ticketcreated");
	}

	@Bean
	public Binding ticketMessageBinding(Queue ticketMessageQueue, TopicExchange supportExchange) {
		return BindingBuilder.bind(ticketMessageQueue).to(supportExchange).with("ticketmessageadded");
	}

	@Bean
	public TopicExchange orderEventsExchange() {
		return new TopicExchange(ORDER_EVENTS_EXCHANGE);
	}

	@Bean
	public Queue orderNotificationQueue() {
		return QueueBuilder.durable(ORDER_NOTIFICATION_QUEUE).build();
	}

	@Bean
	public Binding orderNotificationBinding(Queue orderNotificationQueue, TopicExchange orderEventsExchange) {
		return BindingBuilder.bind(orderNotificationQueue).to(orderEventsExchange).with("order.notification");
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
