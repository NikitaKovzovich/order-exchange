package by.bsuir.documentservice.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TemplateTypeConverter implements AttributeConverter<GeneratedDocument.TemplateType, String> {

	@Override
	public String convertToDatabaseColumn(GeneratedDocument.TemplateType attribute) {
		return attribute != null ? attribute.getTemplateName() : null;
	}

	@Override
	public GeneratedDocument.TemplateType convertToEntityAttribute(String dbData) {
		return GeneratedDocument.TemplateType.fromPersistenceValue(dbData);
	}
}
