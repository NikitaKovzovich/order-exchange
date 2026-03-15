package by.bsuir.documentservice.config;

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

	public static final String RPC_EXCHANGE = "rpc.exchange";


	public static final String RPC_GENERATE_INVOICE = "rpc.document.generateInvoice";
	public static final String RPC_GENERATE_TTN = "rpc.document.generateTtn";
	public static final String RPC_GENERATE_DISCREPANCY_ACT = "rpc.document.generateDiscrepancyAct";
	public static final String RPC_FILE_UPLOAD = "rpc.document.uploadFile";
	public static final String RPC_FILE_URL = "rpc.document.getPresignedUrl";
	public static final String RPC_FILE_DELETE = "rpc.document.deleteFile";

	@Bean
	public DirectExchange rpcExchange() {
		return new DirectExchange(RPC_EXCHANGE);
	}

	@Bean
	public Queue rpcGenerateInvoiceQueue() {
		return QueueBuilder.durable(RPC_GENERATE_INVOICE).build();
	}

	@Bean
	public Queue rpcGenerateTtnQueue() {
		return QueueBuilder.durable(RPC_GENERATE_TTN).build();
	}

	@Bean
	public Queue rpcGenerateDiscrepancyActQueue() {
		return QueueBuilder.durable(RPC_GENERATE_DISCREPANCY_ACT).build();
	}

	@Bean
	public Queue rpcFileUploadQueue() {
		return QueueBuilder.durable(RPC_FILE_UPLOAD).build();
	}

	@Bean
	public Queue rpcFileUrlQueue() {
		return QueueBuilder.durable(RPC_FILE_URL).build();
	}

	@Bean
	public Queue rpcFileDeleteQueue() {
		return QueueBuilder.durable(RPC_FILE_DELETE).build();
	}

	@Bean
	public Binding rpcGenerateInvoiceBinding() {
		return BindingBuilder.bind(rpcGenerateInvoiceQueue()).to(rpcExchange()).with(RPC_GENERATE_INVOICE);
	}

	@Bean
	public Binding rpcGenerateTtnBinding() {
		return BindingBuilder.bind(rpcGenerateTtnQueue()).to(rpcExchange()).with(RPC_GENERATE_TTN);
	}

	@Bean
	public Binding rpcGenerateDiscrepancyActBinding() {
		return BindingBuilder.bind(rpcGenerateDiscrepancyActQueue()).to(rpcExchange()).with(RPC_GENERATE_DISCREPANCY_ACT);
	}

	@Bean
	public Binding rpcFileUploadBinding() {
		return BindingBuilder.bind(rpcFileUploadQueue()).to(rpcExchange()).with(RPC_FILE_UPLOAD);
	}

	@Bean
	public Binding rpcFileUrlBinding() {
		return BindingBuilder.bind(rpcFileUrlQueue()).to(rpcExchange()).with(RPC_FILE_URL);
	}

	@Bean
	public Binding rpcFileDeleteBinding() {
		return BindingBuilder.bind(rpcFileDeleteQueue()).to(rpcExchange()).with(RPC_FILE_DELETE);
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
