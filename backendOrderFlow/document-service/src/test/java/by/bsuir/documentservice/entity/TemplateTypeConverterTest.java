package by.bsuir.documentservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateTypeConverterTest {

	private final TemplateTypeConverter converter = new TemplateTypeConverter();

	@Test
	void shouldReadLowercaseLegacyValueFromDatabase() {
		GeneratedDocument.TemplateType value = converter.convertToEntityAttribute("invoice");

		assertThat(value).isEqualTo(GeneratedDocument.TemplateType.INVOICE);
	}

	@Test
	void shouldReadUppercaseEnumNameFromDatabase() {
		GeneratedDocument.TemplateType value = converter.convertToEntityAttribute("TTN");

		assertThat(value).isEqualTo(GeneratedDocument.TemplateType.TTN);
	}

	@Test
	void shouldWriteLowercaseTemplateNameToDatabase() {
		String value = converter.convertToDatabaseColumn(GeneratedDocument.TemplateType.DISCREPANCY_ACT);

		assertThat(value).isEqualTo("discrepancy_act");
	}
}
