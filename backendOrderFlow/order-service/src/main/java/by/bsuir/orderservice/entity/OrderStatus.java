package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Справочник статусов заказа (Read Model)
 * State Machine для заказа
 */
@Entity
@Table(name = "order_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 255)
	private String description;

	/**
	 * Коды статусов заказа (State Machine)
	 *
	 * Жизненный цикл:
	 * CREATED -> PENDING_CONFIRMATION -> CONFIRMED -> AWAITING_PAYMENT
	 * -> PENDING_PAYMENT_VERIFICATION -> PAID -> AWAITING_SHIPMENT
	 * -> SHIPPED -> DELIVERED / AWAITING_CORRECTION -> CLOSED
	 */
	public static class Codes {
		public static final String CREATED = "CREATED";
		public static final String PENDING_CONFIRMATION = "PENDING_CONFIRMATION";
		public static final String CONFIRMED = "CONFIRMED";
		public static final String REJECTED = "REJECTED";
		public static final String AWAITING_PAYMENT = "AWAITING_PAYMENT";
		public static final String PENDING_PAYMENT_VERIFICATION = "PENDING_PAYMENT_VERIFICATION";
		public static final String PAID = "PAID";
		public static final String AWAITING_SHIPMENT = "AWAITING_SHIPMENT";
		public static final String SHIPPED = "SHIPPED";
		public static final String DELIVERED = "DELIVERED";
		public static final String AWAITING_CORRECTION = "AWAITING_CORRECTION";
		public static final String CLOSED = "CLOSED";
		public static final String CANCELLED = "CANCELLED";
	}

	/**
	 * Проверить, можно ли перейти из текущего статуса в целевой
	 */
	public static boolean canTransition(String from, String to) {
		return switch (from) {
			case Codes.CREATED -> to.equals(Codes.PENDING_CONFIRMATION);
			case Codes.PENDING_CONFIRMATION -> to.equals(Codes.CONFIRMED) || to.equals(Codes.REJECTED);
			case Codes.CONFIRMED -> to.equals(Codes.AWAITING_PAYMENT) || to.equals(Codes.CANCELLED);
			case Codes.AWAITING_PAYMENT -> to.equals(Codes.PENDING_PAYMENT_VERIFICATION) || to.equals(Codes.CANCELLED);
			case Codes.PENDING_PAYMENT_VERIFICATION -> to.equals(Codes.PAID) || to.equals(Codes.AWAITING_PAYMENT);
			case Codes.PAID -> to.equals(Codes.AWAITING_SHIPMENT) || to.equals(Codes.SHIPPED);
			case Codes.AWAITING_SHIPMENT -> to.equals(Codes.SHIPPED);
			case Codes.SHIPPED -> to.equals(Codes.DELIVERED) || to.equals(Codes.AWAITING_CORRECTION);
			case Codes.AWAITING_CORRECTION -> to.equals(Codes.DELIVERED);
			case Codes.DELIVERED -> to.equals(Codes.CLOSED);
			default -> false;
		};
	}

	/**
	 * Получить человекочитаемое название статуса
	 */
	public static String getDisplayName(String code) {
		return switch (code) {
			case Codes.CREATED -> "Создан";
			case Codes.PENDING_CONFIRMATION -> "Ожидает подтверждения";
			case Codes.CONFIRMED -> "Подтвержден";
			case Codes.REJECTED -> "Отклонен";
			case Codes.AWAITING_PAYMENT -> "Ожидает оплаты";
			case Codes.PENDING_PAYMENT_VERIFICATION -> "Проверка оплаты";
			case Codes.PAID -> "Оплачен";
			case Codes.AWAITING_SHIPMENT -> "Ожидает отгрузки";
			case Codes.SHIPPED -> "Отгружен";
			case Codes.DELIVERED -> "Доставлен";
			case Codes.AWAITING_CORRECTION -> "Ожидает корректировки";
			case Codes.CLOSED -> "Закрыт";
			case Codes.CANCELLED -> "Отменен";
			default -> code;
		};
	}
}
