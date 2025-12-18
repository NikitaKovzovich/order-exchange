package by.bsuir.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ставка НДС (Read Model для CQRS)
 * Справочная таблица
 */
@Entity
@Table(name = "vat_rate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VatRate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "rate_percentage", nullable = false, precision = 5, scale = 2)
	private BigDecimal ratePercentage;

	@Column(nullable = false, unique = true, length = 50)
	private String description;

	/**
	 * Рассчитать сумму НДС от цены без НДС
	 */
	public BigDecimal calculateVat(BigDecimal priceWithoutVat) {
		return priceWithoutVat.multiply(ratePercentage).divide(BigDecimal.valueOf(100));
	}

	/**
	 * Рассчитать цену с НДС
	 */
	public BigDecimal calculatePriceWithVat(BigDecimal priceWithoutVat) {
		return priceWithoutVat.add(calculateVat(priceWithoutVat));
	}

	/**
	 * Извлечь НДС из цены с НДС
	 */
	public BigDecimal extractVatFromPrice(BigDecimal priceWithVat) {
		BigDecimal divisor = BigDecimal.valueOf(100).add(ratePercentage);
		return priceWithVat.multiply(ratePercentage).divide(divisor, 2, java.math.RoundingMode.HALF_UP);
	}
}
