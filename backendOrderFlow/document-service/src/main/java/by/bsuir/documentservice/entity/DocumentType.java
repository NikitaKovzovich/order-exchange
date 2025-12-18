package by.bsuir.documentservice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Тип документа (Read Model)
 * Справочная таблица
 */
@Entity
@Table(name = "document_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String code;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	/**
	 * Коды типов документов
	 */
	public static class Codes {
		public static final String INVOICE = "INVOICE";
		public static final String PAYMENT_PROOF = "PAYMENT_PROOF";
		public static final String UPD = "UPD";
		public static final String TTN = "TTN";
		public static final String DISCREPANCY_ACT = "DISCREPANCY_ACT";
		public static final String SIGNED_UPD = "SIGNED_UPD";
		public static final String LOGO = "LOGO";
		public static final String REGISTRATION_CERT = "REGISTRATION_CERT";
		public static final String CHARTER = "CHARTER";
		public static final String EDS = "EDS";
	}
}
