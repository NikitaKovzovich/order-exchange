package by.bsuir.authservice.listener;

import by.bsuir.authservice.config.RabbitMQConfig;
import by.bsuir.authservice.entity.Notification;
import by.bsuir.authservice.repository.UserRepository;
import by.bsuir.authservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;







@Slf4j
@Component
@RequiredArgsConstructor
public class SupportEventListener {

	private final NotificationService notificationService;
	private final UserRepository userRepository;

	@RabbitListener(queues = RabbitMQConfig.TICKET_CREATED_QUEUE)
	public void handleTicketCreated(Map<String, Object> event) {
		try {
			Long ticketId = extractLong(event.get("ticketId"));
			String subject = event.get("subject") != null ? event.get("subject").toString() : "Без темы";
			String priority = event.get("priority") != null ? event.get("priority").toString() : "NORMAL";

			log.info("Received TicketCreated event: ticketId={}, subject={}", ticketId, subject);

			List<Long> adminIds = userRepository.findAllAdminUserIds();
			notificationService.notifyAllAdmins(
					"Новое обращение в поддержку",
					"Создано новое обращение: «" + subject + "» (приоритет: " + priority + ")",
					Notification.NotificationType.TICKET_CREATED,
					"SupportTicket", ticketId, adminIds);

		} catch (Exception e) {
			log.error("Failed to process TicketCreated event: {}", e.getMessage(), e);
		}
	}

	@RabbitListener(queues = RabbitMQConfig.TICKET_MESSAGE_QUEUE)
	public void handleTicketMessageAdded(Map<String, Object> event) {
		try {
			Long ticketId = extractLong(event.get("ticketId"));
			Boolean isAdmin = event.get("isAdmin") instanceof Boolean b ? b : false;


			if (!isAdmin) {
				log.info("Received user reply in ticket: ticketId={}", ticketId);

				List<Long> adminIds = userRepository.findAllAdminUserIds();
				notificationService.notifyAllAdmins(
						"Ответ пользователя в тикете #" + ticketId,
						"Пользователь ответил в обращении #" + ticketId,
						Notification.NotificationType.TICKET_USER_REPLIED,
						"SupportTicket", ticketId, adminIds);
			}
		} catch (Exception e) {
			log.error("Failed to process TicketMessageAdded event: {}", e.getMessage(), e);
		}
	}

	private Long extractLong(Object value) {
		if (value instanceof Number n) return n.longValue();
		if (value instanceof String s) return Long.parseLong(s);
		return null;
	}
}
