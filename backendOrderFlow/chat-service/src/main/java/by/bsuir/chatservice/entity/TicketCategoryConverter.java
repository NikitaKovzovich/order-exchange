package by.bsuir.chatservice.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TicketCategoryConverter implements AttributeConverter<SupportTicket.TicketCategory, String> {

	@Override
	public String convertToDatabaseColumn(SupportTicket.TicketCategory attribute) {
		return attribute != null ? attribute.getPersistenceValue() : null;
	}

	@Override
	public SupportTicket.TicketCategory convertToEntityAttribute(String dbData) {
		return SupportTicket.TicketCategory.fromPersistenceValue(dbData);
	}
}
