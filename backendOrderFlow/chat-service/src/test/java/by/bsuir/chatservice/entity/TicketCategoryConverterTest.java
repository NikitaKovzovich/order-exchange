package by.bsuir.chatservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketCategoryConverterTest {

	private final TicketCategoryConverter converter = new TicketCategoryConverter();

	@Test
	void shouldPersistTechnicalAliasAsDatabaseIssueValue() {
		String value = converter.convertToDatabaseColumn(SupportTicket.TicketCategory.TECHNICAL);

		assertThat(value).isEqualTo("TECHNICAL_ISSUE");
	}

	@Test
	void shouldReadDatabaseIssueValueAsCanonicalEnum() {
		SupportTicket.TicketCategory value = converter.convertToEntityAttribute("TECHNICAL_ISSUE");

		assertThat(value).isEqualTo(SupportTicket.TicketCategory.TECHNICAL_ISSUE);
	}

	@Test
	void shouldReadLegacyAliasValueAsEnum() {
		SupportTicket.TicketCategory value = converter.convertToEntityAttribute("PAYMENT");

		assertThat(value).isEqualTo(SupportTicket.TicketCategory.PAYMENT);
	}
}
