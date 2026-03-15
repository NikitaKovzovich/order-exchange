package by.bsuir.orderservice.config;

import by.bsuir.orderservice.entity.OrderStatus;
import by.bsuir.orderservice.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;





@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

	private final OrderStatusRepository statusRepository;

	@Override
	public void run(String... args) {
		initOrderStatuses();
	}

	private void initOrderStatuses() {
		if (statusRepository.count() > 0) {
			log.info("Order statuses already exist, skipping initialization");
			return;
		}

		log.info("Initializing order statuses...");

		createStatus(OrderStatus.Codes.PENDING_CONFIRMATION,
				"Ожидает подтверждения",
				"Заказ создан и ожидает подтверждения поставщиком");

		createStatus(OrderStatus.Codes.CONFIRMED,
				"Подтвержден",
				"Заказ подтвержден поставщиком (транзитный)");

		createStatus(OrderStatus.Codes.REJECTED,
				"Отклонен",
				"Заказ отклонен поставщиком");

		createStatus(OrderStatus.Codes.AWAITING_PAYMENT,
				"Ожидает оплаты",
				"Выставлен счет, ожидается оплата");

		createStatus(OrderStatus.Codes.PENDING_PAYMENT_VERIFICATION,
				"Ожидает проверки оплаты",
				"Загружено платежное поручение, ожидается подтверждение");

		createStatus(OrderStatus.Codes.PAID,
				"Оплачен",
				"Оплата подтверждена (транзитный)");

		createStatus(OrderStatus.Codes.PAYMENT_PROBLEM,
				"Проблема с оплатой",
				"Оплата отклонена поставщиком");

		createStatus(OrderStatus.Codes.AWAITING_SHIPMENT,
				"Ожидает отгрузки",
				"Заказ готовится к отгрузке");

		createStatus(OrderStatus.Codes.SHIPPED,
				"В пути",
				"Заказ отправлен и находится в пути");

		createStatus(OrderStatus.Codes.DELIVERED,
				"Доставлен",
				"Заказ получен покупателем");

		createStatus(OrderStatus.Codes.AWAITING_CORRECTION,
				"Ожидает корректировки",
				"Выявлены расхождения при приемке");

		createStatus(OrderStatus.Codes.CLOSED,
				"Закрыт",
				"Заказ закрыт");

		createStatus(OrderStatus.Codes.CANCELLED,
				"Отменен",
				"Заказ отменен");

		log.info("Order statuses initialized successfully ({} statuses)", statusRepository.count());
	}

	private void createStatus(String code, String name, String description) {
		if (statusRepository.findByCode(code).isEmpty()) {
			statusRepository.save(OrderStatus.builder()
					.code(code)
					.name(name)
					.description(description)
					.build());
		}
	}
}
