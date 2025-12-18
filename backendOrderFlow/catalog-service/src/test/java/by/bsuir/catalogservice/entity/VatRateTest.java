package by.bsuir.catalogservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class VatRateTest {

	@Test
	@DisplayName("Should calculate price with VAT")
	void shouldCalculatePriceWithVat() {
		VatRate vatRate = VatRate.builder()
				.ratePercentage(new BigDecimal("20.00"))
				.build();

		BigDecimal result = vatRate.calculatePriceWithVat(new BigDecimal("100.00"));
		assertThat(result).isEqualByComparingTo(new BigDecimal("120.00"));
	}

	@Test
	@DisplayName("Should handle zero VAT rate")
	void shouldHandleZeroVatRate() {
		VatRate vatRate = VatRate.builder()
				.ratePercentage(BigDecimal.ZERO)
				.build();

		BigDecimal result = vatRate.calculatePriceWithVat(new BigDecimal("100.00"));
		assertThat(result).isEqualByComparingTo(new BigDecimal("100.00"));
	}

	@Test
	@DisplayName("Should handle 10% VAT rate")
	void shouldHandle10PercentVatRate() {
		VatRate vatRate = VatRate.builder()
				.ratePercentage(new BigDecimal("10.00"))
				.build();

		BigDecimal result = vatRate.calculatePriceWithVat(new BigDecimal("50.00"));
		assertThat(result).isEqualByComparingTo(new BigDecimal("55.00"));
	}
}
