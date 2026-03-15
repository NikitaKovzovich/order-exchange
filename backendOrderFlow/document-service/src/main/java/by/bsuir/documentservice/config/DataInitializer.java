package by.bsuir.documentservice.config;

import by.bsuir.documentservice.entity.DocumentType;
import by.bsuir.documentservice.repository.DocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;





@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

	private final DocumentTypeRepository documentTypeRepository;

	@Override
	public void run(String... args) {
		initDocumentTypes();
	}

	private void initDocumentTypes() {
		if (documentTypeRepository.count() > 0) {
			log.info("Document types already exist, skipping initialization");
			return;
		}

		log.info("Initializing document types...");

		createType(DocumentType.Codes.INVOICE,
				"Счет на оплату",
				"Счет на оплату товаров");

		createType(DocumentType.Codes.PAYMENT_PROOF,
				"Платежное поручение",
				"Подтверждение оплаты");

		createType(DocumentType.Codes.UPD,
				"Универсальный передаточный документ",
				"УПД для отгрузки");

		createType(DocumentType.Codes.TTN,
				"Товарно-транспортная накладная",
				"ТТН для доставки");

		createType(DocumentType.Codes.DISCREPANCY_ACT,
				"Акт о расхождении",
				"Акт при несоответствии поставки");

		createType(DocumentType.Codes.SIGNED_UPD,
				"Подписанный УПД",
				"УПД с подписью получателя");

		createType(DocumentType.Codes.LOGO,
				"Логотип компании",
				"Логотип для документов");

		createType(DocumentType.Codes.REGISTRATION_CERT,
				"Свидетельство о регистрации",
				"Регистрационные документы");

		createType(DocumentType.Codes.CHARTER,
				"Устав",
				"Устав организации");

		createType(DocumentType.Codes.EDS,
				"Электронная подпись",
				"Файл ЭЦП");

		log.info("Document types initialized successfully ({} types)", documentTypeRepository.count());
	}

	private void createType(String code, String name, String description) {
		documentTypeRepository.save(DocumentType.builder()
				.code(code)
				.name(name)
				.description(description)
				.build());
	}
}
