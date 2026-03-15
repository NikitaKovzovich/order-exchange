package by.bsuir.orderservice.service;

import by.bsuir.orderservice.dto.NotificationResponse;
import by.bsuir.orderservice.dto.PageResponse;
import by.bsuir.orderservice.entity.Notification;
import by.bsuir.orderservice.entity.Notification.NotificationType;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.exception.ResourceNotFoundException;
import by.bsuir.orderservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final EventPublisher eventPublisher;



	@Transactional(readOnly = true)
	public PageResponse<NotificationResponse> getNotifications(Long recipientId, Boolean unreadOnly, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Notification> notifications;

		if (Boolean.TRUE.equals(unreadOnly)) {
			notifications = notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(recipientId, false, pageable);
		} else {
			notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
		}

		return new PageResponse<>(
				notifications.getContent().stream().map(this::mapToResponse).toList(),
				notifications.getNumber(),
				notifications.getSize(),
				notifications.getTotalElements(),
				notifications.getTotalPages(),
				notifications.isFirst(),
				notifications.isLast()
		);
	}

	@Transactional(readOnly = true)
	public long getUnreadCount(Long recipientId) {
		return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
	}

	@Transactional
	public NotificationResponse markAsRead(Long notificationId, Long recipientId) {
		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
		if (!notification.getRecipientId().equals(recipientId)) {
			throw new ResourceNotFoundException("Notification", "id", notificationId);
		}
		notification.markAsRead();
		notification = notificationRepository.save(notification);
		return mapToResponse(notification);
	}

	@Transactional
	public int markAllAsRead(Long recipientId) {
		return notificationRepository.markAllAsRead(recipientId);
	}







	private void createNotification(Long recipientId, NotificationType type, String title, String message,
			Long orderId, String orderNumber) {
		try {
			Notification notification = Notification.builder()
					.recipientId(recipientId)
					.type(type)
					.title(title)
					.message(message)
					.orderId(orderId)
					.orderNumber(orderNumber)
					.createdAt(LocalDateTime.now())
					.build();
			notificationRepository.save(notification);
			log.debug("Notification created: type={}, recipient={}, order={}", type, recipientId, orderNumber);


			eventPublisher.publishNotificationEvent(recipientId, type.name(), title, message, orderId, orderNumber);
		} catch (Exception e) {
			log.error("Failed to create notification: type={}, recipient={}", type, recipientId, e);
		}
	}




	public void notifySupplierNewOrder(Order order) {
		createNotification(order.getSupplierId(), NotificationType.NEW_ORDER,
				"Новый заказ " + order.getOrderNumber(),
				"Поступил новый заказ №" + order.getOrderNumber() +
						" на сумму " + order.getTotalAmount() + " руб.",
				order.getId(), order.getOrderNumber());
	}


	public void notifySupplierPaymentProofUploaded(Order order) {
		createNotification(order.getSupplierId(), NotificationType.PAYMENT_PROOF_UPLOADED,
				"Подтверждение оплаты",
				"Торговая сеть загрузила подтверждение оплаты по заказу №" + order.getOrderNumber(),
				order.getId(), order.getOrderNumber());
	}


	public void notifySupplierDeliveryConfirmed(Order order) {
		createNotification(order.getSupplierId(), NotificationType.DELIVERY_CONFIRMED,
				"Товар получен",
				"Торговая сеть подтвердила получение товара по заказу №" + order.getOrderNumber(),
				order.getId(), order.getOrderNumber());
	}


	public void notifySupplierAcceptanceProblem(Order order) {
		createNotification(order.getSupplierId(), NotificationType.ACCEPTANCE_PROBLEM,
				"Акт о расхождении",
				"Торговая сеть сформировала Акт о расхождении по заказу №" + order.getOrderNumber(),
				order.getId(), order.getOrderNumber());
	}


	public void notifySupplierCorrectionDeliveryConfirmed(Order order) {
		createNotification(order.getSupplierId(), NotificationType.CORRECTION_DELIVERY_CONFIRMED,
				"Корректировка получена",
				"Торговая сеть подтвердила получение по корректировочной ТТН. Заказ №" + order.getOrderNumber(),
				order.getId(), order.getOrderNumber());
	}




	public void notifyCustomerOrderConfirmed(Order order) {
		createNotification(order.getCustomerId(), NotificationType.ORDER_CONFIRMED,
				"Заказ подтверждён",
				"Поставщик подтвердил ваш заказ №" + order.getOrderNumber(),
				order.getId(), order.getOrderNumber());
	}


	public void notifyCustomerOrderRejected(Order order, String reason) {
		createNotification(order.getCustomerId(), NotificationType.ORDER_REJECTED,
				"Заказ отклонён",
				"Поставщик отклонил заказ №" + order.getOrderNumber() +
						(reason != null ? ". Причина: " + reason : ""),
				order.getId(), order.getOrderNumber());
	}


	public void notifyCustomerInvoiceIssued(Order order) {
		createNotification(order.getCustomerId(), NotificationType.INVOICE_ISSUED,
				"Выставлен счёт",
				"Сформирован счёт на оплату по заказу №" + order.getOrderNumber() +
						". Сумма: " + order.getTotalAmount() + " руб.",
				order.getId(), order.getOrderNumber());
	}


	public void notifyCustomerPaymentConfirmed(Order order) {
		createNotification(order.getCustomerId(), NotificationType.PAYMENT_CONFIRMED_RETAIL,
				"Оплата подтверждена",
				"Поставщик подтвердил оплату по заказу №" + order.getOrderNumber(),
				order.getId(), order.getOrderNumber());
	}


	public void notifyCustomerPaymentRejected(Order order, String reason) {
		createNotification(order.getCustomerId(), NotificationType.PAYMENT_REJECTED,
				"Оплата отклонена",
				"Поставщик отклонил оплату по заказу №" + order.getOrderNumber() +
						(reason != null ? ". Причина: " + reason : ""),
				order.getId(), order.getOrderNumber());
	}


	public void notifyCustomerOrderShipped(Order order) {
		createNotification(order.getCustomerId(), NotificationType.ORDER_SHIPPED,
				"Заказ отгружен",
				"Заказ №" + order.getOrderNumber() + " отгружен и находится в пути.",
				order.getId(), order.getOrderNumber());
	}


	public void notifyCustomerTtnFormed(Order order) {
		createNotification(order.getCustomerId(), NotificationType.TTN_FORMED,
				"ТТН сформирована",
				"По заказу №" + order.getOrderNumber() + " сформирована товарно-транспортная накладная (ТТН).",
				order.getId(), order.getOrderNumber());
	}


	public void notifyCustomerCorrectionResponse(Order order) {
		createNotification(order.getCustomerId(), NotificationType.CORRECTION_RESPONSE,
				"Корректировка",
				"Поставщик сформировал корректировочную ТТН по заказу №" + order.getOrderNumber(),
				order.getId(), order.getOrderNumber());
	}


	public void notifySupplierCorrectionTtnFormed(Order order) {
		createNotification(order.getSupplierId(), NotificationType.CORRECTION_TTN_FORMED,
				"Корректировочная ТТН",
				"Сформирована корректировочная ТТН по заказу №" + order.getOrderNumber(),
				order.getId(), order.getOrderNumber());
	}


	public void notifyCustomerOrderClosed(Order order) {
		createNotification(order.getCustomerId(), NotificationType.ORDER_CLOSED,
				"Заказ закрыт",
				"Заказ №" + order.getOrderNumber() + " закрыт.",
				order.getId(), order.getOrderNumber());
	}



	private NotificationResponse mapToResponse(Notification n) {
		return new NotificationResponse(
				n.getId(),
				n.getRecipientId(),
				n.getType().name(),
				n.getType().getDisplayName(),
				n.getTitle(),
				n.getMessage(),
				n.getOrderId(),
				n.getOrderNumber(),
				n.isRead(),
				n.getCreatedAt(),
				n.getReadAt()
		);
	}
}
