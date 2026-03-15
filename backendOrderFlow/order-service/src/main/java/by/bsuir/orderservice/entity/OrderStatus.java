package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;





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









	public static class Codes {
		public static final String PENDING_CONFIRMATION = "PENDING_CONFIRMATION";
		public static final String CONFIRMED = "CONFIRMED";
		public static final String REJECTED = "REJECTED";
		public static final String AWAITING_PAYMENT = "AWAITING_PAYMENT";
		public static final String PENDING_PAYMENT_VERIFICATION = "PENDING_PAYMENT_VERIFICATION";
		public static final String PAID = "PAID";
		public static final String PAYMENT_PROBLEM = "PAYMENT_PROBLEM";
		public static final String AWAITING_SHIPMENT = "AWAITING_SHIPMENT";
		public static final String SHIPPED = "SHIPPED";
		public static final String DELIVERED = "DELIVERED";
		public static final String AWAITING_CORRECTION = "AWAITING_CORRECTION";
		public static final String CLOSED = "CLOSED";
		public static final String CANCELLED = "CANCELLED";
	}




	public static boolean canTransition(String from, String to) {
		return switch (from) {
			case Codes.PENDING_CONFIRMATION -> to.equals(Codes.CONFIRMED) || to.equals(Codes.REJECTED) || to.equals(Codes.CANCELLED);
			case Codes.CONFIRMED -> to.equals(Codes.AWAITING_PAYMENT) || to.equals(Codes.CANCELLED);
			case Codes.AWAITING_PAYMENT -> to.equals(Codes.PENDING_PAYMENT_VERIFICATION) || to.equals(Codes.CANCELLED);
			case Codes.PENDING_PAYMENT_VERIFICATION -> to.equals(Codes.PAID) || to.equals(Codes.PAYMENT_PROBLEM);
			case Codes.PAYMENT_PROBLEM -> to.equals(Codes.PENDING_PAYMENT_VERIFICATION) || to.equals(Codes.CANCELLED);
			case Codes.PAID -> to.equals(Codes.AWAITING_SHIPMENT);
			case Codes.AWAITING_SHIPMENT -> to.equals(Codes.SHIPPED);
			case Codes.SHIPPED -> to.equals(Codes.DELIVERED) || to.equals(Codes.AWAITING_CORRECTION);
			case Codes.AWAITING_CORRECTION -> to.equals(Codes.SHIPPED);
			case Codes.DELIVERED -> to.equals(Codes.CLOSED);
			default -> false;
		};
	}




	public static String getDisplayName(String code) {
		return switch (code) {
			case Codes.PENDING_CONFIRMATION -> "Ожидает подтверждения";
			case Codes.CONFIRMED -> "Подтвержден";
			case Codes.REJECTED -> "Отклонен";
			case Codes.AWAITING_PAYMENT -> "Ожидает оплаты";
			case Codes.PENDING_PAYMENT_VERIFICATION -> "Ожидает проверки оплаты";
			case Codes.PAID -> "Оплачен";
			case Codes.PAYMENT_PROBLEM -> "Проблема с оплатой";
			case Codes.AWAITING_SHIPMENT -> "Ожидает отгрузки";
			case Codes.SHIPPED -> "В пути";
			case Codes.DELIVERED -> "Доставлен";
			case Codes.AWAITING_CORRECTION -> "Ожидает корректировки";
			case Codes.CLOSED -> "Закрыт";
			case Codes.CANCELLED -> "Отменен";
			default -> code;
		};
	}
}
