package by.bsuir.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "legal_name", nullable = false)
	private String legalName;

	@Column(name = "name")
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "legal_form", nullable = false)
	private LegalForm legalForm;

	@Column(name = "tax_id", nullable = false, unique = true)
	private String taxId;

	@Column(name = "registration_date", nullable = false)
	private LocalDate registrationDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CompanyStatus status;

	@Column(name = "contact_phone")
	private String contactPhone;

	@Column(name = "verified")
	@Builder.Default
	private Boolean verified = false;

	@Column(name = "inn")
	private String inn;

	public LegalForm getLegalFormValue() {
		return this.legalForm;
	}

	/**
	 * Получает текстовое представление организационно-правовой формы
	 */
	public String getLegalFormText() {
		return switch (this.legalForm) {
			case IE -> "ИП";
			case LLC -> "ООО";
			case OJSC -> "ОАО";
			case CJSC -> "ЗАО";
			case PJSC -> "ПАО";
			case PUE -> "ЧУП";
		};
	}

	/**
	 * Формирует полное юридическое наименование: форма + название
	 * Например: "ООО 'Название компании'"
	 */
	public String buildLegalName() {
		if (this.name == null || this.name.isEmpty()) {
			return this.legalName;
		}
		return getLegalFormText() + " \"" + this.name + "\"";
	}

	/**
	 * Устанавливает название и автоматически формирует legalName
	 */
	public void setNameAndBuildLegalName(String name) {
		this.name = name;
		this.legalName = buildLegalName();
	}

	public enum LegalForm {
		IE, LLC, OJSC, CJSC, PJSC, PUE
	}

	public enum CompanyStatus {
		PENDING_VERIFICATION,
		ACTIVE,
		REJECTED,
		BLOCKED
	}
}
