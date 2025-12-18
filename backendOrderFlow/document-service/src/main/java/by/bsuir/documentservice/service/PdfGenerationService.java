package by.bsuir.documentservice.service;

import by.bsuir.documentservice.dto.*;
import by.bsuir.documentservice.entity.GeneratedDocument;
import by.bsuir.documentservice.repository.GeneratedDocumentRepository;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

	private final MinioService minioService;
	private final GeneratedDocumentRepository generatedDocumentRepository;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	private static final String FONT_PATH = "/fonts/DejaVuSans.ttf";

	@Transactional
	public GeneratedDocumentResponse generateTTN(TtnGenerationRequest request, Long userId) {
		log.info("Generating TTN for order: {}", request.orderId());

		try {
			String documentNumber = generateDocumentNumber("TTN");
			byte[] pdfContent = createTtnPdf(request, documentNumber);

			String fileKey = uploadPdf(pdfContent, "ttn", request.orderId(), documentNumber);

			GeneratedDocument document = GeneratedDocument.builder()
					.templateType(GeneratedDocument.TemplateType.TTN)
					.orderId(request.orderId())
					.fileKey(fileKey)
					.generatedBy(userId)
					.documentNumber(documentNumber)
					.documentDate(request.documentDate())
					.build();

			document = generatedDocumentRepository.save(document);
			log.info("TTN generated successfully: {}", document.getId());

			return toResponse(document);
		} catch (Exception e) {
			log.error("Failed to generate TTN: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to generate TTN: " + e.getMessage(), e);
		}
	}

	@Transactional
	public GeneratedDocumentResponse generateDiscrepancyAct(DiscrepancyActRequest request, Long userId) {
		log.info("Generating Discrepancy Act for order: {}", request.orderId());

		try {
			String documentNumber = generateDocumentNumber("DA");
			byte[] pdfContent = createDiscrepancyActPdf(request, documentNumber);

			String fileKey = uploadPdf(pdfContent, "discrepancy-act", request.orderId(), documentNumber);

			GeneratedDocument document = GeneratedDocument.builder()
					.templateType(GeneratedDocument.TemplateType.DISCREPANCY_ACT)
					.orderId(request.orderId())
					.fileKey(fileKey)
					.generatedBy(userId)
					.documentNumber(documentNumber)
					.documentDate(request.actDate())
					.build();

			document = generatedDocumentRepository.save(document);
			log.info("Discrepancy Act generated successfully: {}", document.getId());

			return toResponse(document);
		} catch (Exception e) {
			log.error("Failed to generate Discrepancy Act: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to generate Discrepancy Act: " + e.getMessage(), e);
		}
	}

	public InputStream downloadGeneratedDocument(Long documentId) {
		GeneratedDocument document = generatedDocumentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Generated document not found: " + documentId));

		try {
			return minioService.downloadFile(document.getFileKey());
		} catch (Exception e) {
			log.error("Failed to download document: {}", e.getMessage());
			throw new RuntimeException("Failed to download document: " + e.getMessage(), e);
		}
	}

	public List<GeneratedDocumentResponse> getDocumentsByOrder(Long orderId) {
		return generatedDocumentRepository.findByOrderId(orderId).stream()
				.map(this::toResponse)
				.toList();
	}

	public GeneratedDocumentResponse getDocument(Long documentId) {
		GeneratedDocument document = generatedDocumentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Generated document not found: " + documentId));
		return toResponse(document);
	}

	private byte[] createTtnPdf(TtnGenerationRequest request, String documentNumber) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdf = new PdfDocument(writer);
		Document document = new Document(pdf, PageSize.A4);

		PdfFont font = createFont();
		PdfFont boldFont = createFont();

		document.setFont(font);
		document.setFontSize(10);

		addTtnHeader(document, boldFont, request, documentNumber);
		addPartiesInfo(document, font, request);
		addItemsTable(document, font, boldFont, request.items());
		addTtnTotals(document, font, boldFont, request);
		addTtnSignatures(document, font, request);

		document.close();
		return outputStream.toByteArray();
	}

	private byte[] createDiscrepancyActPdf(DiscrepancyActRequest request, String documentNumber) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdf = new PdfDocument(writer);
		Document document = new Document(pdf, PageSize.A4);

		PdfFont font = createFont();
		PdfFont boldFont = createFont();

		document.setFont(font);
		document.setFontSize(10);

		addDiscrepancyActHeader(document, boldFont, request, documentNumber);
		addDiscrepancyPartiesInfo(document, font, request);
		addDiscrepancyTable(document, font, boldFont, request.items());
		addDiscrepancyConclusion(document, font, boldFont, request);
		addDiscrepancySignatures(document, font, request);

		document.close();
		return outputStream.toByteArray();
	}

	private void addTtnHeader(Document document, PdfFont boldFont, TtnGenerationRequest request, String documentNumber) {
		Paragraph formCode = new Paragraph("Форма ТТН-1")
				.setTextAlignment(TextAlignment.RIGHT)
				.setFontSize(8);
		document.add(formCode);

		Paragraph title = new Paragraph("ТОВАРНО-ТРАНСПОРТНАЯ НАКЛАДНАЯ")
				.setFont(boldFont)
				.setFontSize(14)
				.setTextAlignment(TextAlignment.CENTER)
				.setMarginTop(10);
		document.add(title);

		String dateStr = request.documentDate().format(DATE_FORMATTER);
		String seriesStr = request.series() != null ? "Серия " + request.series() + " " : "";

		Paragraph docInfo = new Paragraph(seriesStr + "№ " + documentNumber + " от " + dateStr)
				.setTextAlignment(TextAlignment.CENTER)
				.setMarginBottom(15);
		document.add(docInfo);
	}

	private void addPartiesInfo(Document document, PdfFont font, TtnGenerationRequest request) {
		Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
				.useAllAvailableWidth()
				.setMarginBottom(10);

		addInfoRow(infoTable, font, "Грузоотправитель:", formatCompanyInfo(request.shipper()));
		addInfoRow(infoTable, font, "Грузополучатель:", formatCompanyInfo(request.consignee()));

		if (request.payer() != null) {
			addInfoRow(infoTable, font, "Плательщик:", formatCompanyInfo(request.payer()));
		}

		addInfoRow(infoTable, font, "Пункт погрузки:", request.loadingPoint());
		addInfoRow(infoTable, font, "Пункт разгрузки:", request.unloadingPoint());

		if (request.transport() != null) {
			addInfoRow(infoTable, font, "Автомобиль:",
					request.transport().vehicleModel() + " " + request.transport().vehicleNumber());
			addInfoRow(infoTable, font, "Водитель:", request.transport().driverName());
		}

		if (request.releaseReason() != null) {
			addInfoRow(infoTable, font, "Основание:", request.releaseReason());
		}

		document.add(infoTable);
	}

	private void addItemsTable(Document document, PdfFont font, PdfFont boldFont, List<ProductItemDto> items) {
		Table table = new Table(UnitValue.createPercentArray(new float[]{5, 25, 10, 8, 12, 12, 12, 16}))
				.useAllAvailableWidth()
				.setMarginTop(10)
				.setMarginBottom(10);

		String[] headers = {"№", "Наименование", "Ед.изм.", "Кол-во", "Цена без НДС", "Сумма без НДС", "НДС", "Сумма с НДС"};
		for (String header : headers) {
			table.addHeaderCell(createHeaderCell(header, boldFont));
		}

		int lineNum = 1;
		for (ProductItemDto item : items) {
			table.addCell(createDataCell(String.valueOf(lineNum++), font));
			table.addCell(createDataCell(item.name(), font).setTextAlignment(TextAlignment.LEFT));
			table.addCell(createDataCell(item.unitOfMeasure(), font));
			table.addCell(createDataCell(String.valueOf(item.quantity()), font));
			table.addCell(createDataCell(formatMoney(item.priceWithoutVat()), font));
			table.addCell(createDataCell(formatMoney(item.totalWithoutVat()), font));
			table.addCell(createDataCell(formatMoney(item.vatAmount()), font));
			table.addCell(createDataCell(formatMoney(item.totalWithVat()), font));
		}

		document.add(table);
	}

	private void addTtnTotals(Document document, PdfFont font, PdfFont boldFont, TtnGenerationRequest request) {
		Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
				.useAllAvailableWidth()
				.setMarginTop(10);

		addTotalRow(totalsTable, boldFont, "Итого без НДС:", formatMoney(request.totalWithoutVat()));
		addTotalRow(totalsTable, boldFont, "НДС:", formatMoney(request.totalVat()));
		addTotalRow(totalsTable, boldFont, "Всего с НДС:", formatMoney(request.totalWithVat()));

		if (request.totalWeight() != null) {
			addTotalRow(totalsTable, font, "Масса груза:", request.totalWeight() + " кг");
		}
		if (request.totalPackages() != null) {
			addTotalRow(totalsTable, font, "Количество мест:", String.valueOf(request.totalPackages()));
		}

		document.add(totalsTable);

		if (request.notes() != null && !request.notes().isEmpty()) {
			document.add(new Paragraph("Примечание: " + request.notes())
					.setFontSize(9)
					.setMarginTop(10));
		}
	}

	private void addTtnSignatures(Document document, PdfFont font, TtnGenerationRequest request) {
		document.add(new Paragraph("\n"));

		Table sigTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
				.useAllAvailableWidth()
				.setMarginTop(20);

		Cell leftCell = new Cell()
				.setBorder(Border.NO_BORDER)
				.add(new Paragraph("Отпуск разрешил:").setFont(font))
				.add(new Paragraph("___________________ / " +
						(request.shipper().directorName() != null ? request.shipper().directorName() : "_______________") + " /")
						.setMarginTop(20));

		Cell rightCell = new Cell()
				.setBorder(Border.NO_BORDER)
				.add(new Paragraph("Груз принял:").setFont(font))
				.add(new Paragraph("___________________ / _______________ /")
						.setMarginTop(20));

		sigTable.addCell(leftCell);
		sigTable.addCell(rightCell);

		document.add(sigTable);
		document.add(new Paragraph("М.П.").setTextAlignment(TextAlignment.LEFT).setMarginTop(10).setFontSize(8));
	}

	private void addDiscrepancyActHeader(Document document, PdfFont boldFont, DiscrepancyActRequest request, String documentNumber) {
		Paragraph title = new Paragraph("АКТ О РАСХОЖДЕНИИ")
				.setFont(boldFont)
				.setFontSize(14)
				.setTextAlignment(TextAlignment.CENTER)
				.setMarginTop(10);
		document.add(title);

		String dateStr = request.actDate().format(DATE_FORMATTER);
		Paragraph docInfo = new Paragraph("№ " + documentNumber + " от " + dateStr)
				.setTextAlignment(TextAlignment.CENTER)
				.setMarginBottom(10);
		document.add(docInfo);

		Paragraph ttnRef = new Paragraph("к ТТН № " + request.ttnNumber() + " от " + request.ttnDate().format(DATE_FORMATTER))
				.setTextAlignment(TextAlignment.CENTER)
				.setMarginBottom(15);
		document.add(ttnRef);
	}

	private void addDiscrepancyPartiesInfo(Document document, PdfFont font, DiscrepancyActRequest request) {
		Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
				.useAllAvailableWidth()
				.setMarginBottom(10);

		addInfoRow(infoTable, font, "Поставщик:", formatCompanyInfo(request.supplier()));
		addInfoRow(infoTable, font, "Покупатель:", formatCompanyInfo(request.buyer()));
		addInfoRow(infoTable, font, "Номер заказа:", request.orderNumber());

		document.add(infoTable);

		document.add(new Paragraph("Комиссия в составе:").setMarginTop(10).setMarginBottom(5));
		if (request.commissionMembers() != null) {
			for (int i = 0; i < request.commissionMembers().size(); i++) {
				document.add(new Paragraph((i + 1) + ". " + request.commissionMembers().get(i))
						.setFontSize(9)
						.setMarginLeft(20));
			}
		}
		document.add(new Paragraph("установила следующие расхождения:").setMarginTop(10));
	}

	private void addDiscrepancyTable(Document document, PdfFont font, PdfFont boldFont, List<DiscrepancyItemDto> items) {
		Table table = new Table(UnitValue.createPercentArray(new float[]{5, 25, 8, 12, 12, 12, 12, 14}))
				.useAllAvailableWidth()
				.setMarginTop(10)
				.setMarginBottom(10);

		String[] headers = {"№", "Наименование", "Ед.изм.", "По док.", "Факт", "Разница", "Цена", "Сумма расх."};
		for (String header : headers) {
			table.addHeaderCell(createHeaderCell(header, boldFont));
		}

		int lineNum = 1;
		for (DiscrepancyItemDto item : items) {
			table.addCell(createDataCell(String.valueOf(lineNum++), font));
			table.addCell(createDataCell(item.productName(), font).setTextAlignment(TextAlignment.LEFT));
			table.addCell(createDataCell(item.unitOfMeasure(), font));
			table.addCell(createDataCell(String.valueOf(item.expectedQuantity()), font));
			table.addCell(createDataCell(String.valueOf(item.actualQuantity()), font));
			table.addCell(createDataCell(String.valueOf(item.discrepancy()), font));
			table.addCell(createDataCell(formatMoney(item.unitPrice()), font));
			table.addCell(createDataCell(formatMoney(item.discrepancyAmount()), font));
		}

		document.add(table);
	}

	private void addDiscrepancyConclusion(Document document, PdfFont font, PdfFont boldFont, DiscrepancyActRequest request) {
		Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
				.useAllAvailableWidth();

		addTotalRow(totalsTable, boldFont, "Общая сумма расхождения:", formatMoney(request.totalDiscrepancyAmount()));
		document.add(totalsTable);

		if (request.conclusion() != null && !request.conclusion().isEmpty()) {
			document.add(new Paragraph("Заключение комиссии:").setFont(boldFont).setMarginTop(15));
			document.add(new Paragraph(request.conclusion()).setFontSize(9).setMarginLeft(10));
		}

		if (request.resolutionProposal() != null && !request.resolutionProposal().isEmpty()) {
			document.add(new Paragraph("Предложения по урегулированию:").setFont(boldFont).setMarginTop(10));
			document.add(new Paragraph(request.resolutionProposal()).setFontSize(9).setMarginLeft(10));
		}

		if (request.notes() != null && !request.notes().isEmpty()) {
			document.add(new Paragraph("Примечание: " + request.notes())
					.setFontSize(9)
					.setMarginTop(10));
		}
	}

	private void addDiscrepancySignatures(Document document, PdfFont font, DiscrepancyActRequest request) {
		document.add(new Paragraph("\nПодписи членов комиссии:").setMarginTop(20));

		if (request.commissionMembers() != null) {
			for (String member : request.commissionMembers()) {
				document.add(new Paragraph("_________________ / " + member + " /")
						.setMarginTop(15)
						.setMarginLeft(20));
			}
		} else {
			for (int i = 0; i < 3; i++) {
				document.add(new Paragraph("_________________ / _______________ /")
						.setMarginTop(15)
						.setMarginLeft(20));
			}
		}

		document.add(new Paragraph("\nДата: " + LocalDate.now().format(DATE_FORMATTER)).setMarginTop(20));
	}

	private PdfFont createFont() throws Exception {
		try {
			InputStream fontStream = getClass().getResourceAsStream(FONT_PATH);
			if (fontStream != null) {
				byte[] fontBytes = fontStream.readAllBytes();
				return PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
			}
		} catch (Exception e) {
			log.warn("Failed to load custom font, using default: {}", e.getMessage());
		}
		return PdfFontFactory.createFont();
	}

	private String formatCompanyInfo(CompanyInfoDto company) {
		StringBuilder sb = new StringBuilder();
		sb.append(company.name());
		if (company.taxId() != null) {
			sb.append(", УНП: ").append(company.taxId());
		}
		if (company.legalAddress() != null) {
			sb.append(", ").append(company.legalAddress());
		}
		return sb.toString();
	}

	private String formatMoney(BigDecimal amount) {
		if (amount == null) return "-";
		return String.format("%.2f", amount);
	}

	private Cell createHeaderCell(String text, PdfFont font) {
		return new Cell()
				.add(new Paragraph(text).setFont(font).setFontSize(8))
				.setBackgroundColor(ColorConstants.LIGHT_GRAY)
				.setTextAlignment(TextAlignment.CENTER)
				.setBorder(new SolidBorder(0.5f));
	}

	private Cell createDataCell(String text, PdfFont font) {
		return new Cell()
				.add(new Paragraph(text != null ? text : "-").setFont(font).setFontSize(9))
				.setTextAlignment(TextAlignment.CENTER)
				.setBorder(new SolidBorder(0.5f));
	}

	private void addInfoRow(Table table, PdfFont font, String label, String value) {
		table.addCell(new Cell().add(new Paragraph(label).setFont(font).setFontSize(9)).setBorder(Border.NO_BORDER));
		table.addCell(new Cell().add(new Paragraph(value != null ? value : "-").setFontSize(9)).setBorder(Border.NO_BORDER));
	}

	private void addTotalRow(Table table, PdfFont font, String label, String value) {
		table.addCell(new Cell().add(new Paragraph(label).setFont(font).setFontSize(10)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
		table.addCell(new Cell().add(new Paragraph(value).setFont(font).setFontSize(10)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
	}

	private String generateDocumentNumber(String prefix) {
		return prefix + "-" + System.currentTimeMillis() % 100000000;
	}

	private String uploadPdf(byte[] content, String docType, Long orderId, String docNumber) throws Exception {
		String fileName = docType + "_" + docNumber + ".pdf";
		String folder = "generated/" + orderId;
		return minioService.uploadPdfBytes(content, folder, fileName);
	}

	private GeneratedDocumentResponse toResponse(GeneratedDocument document) {
		return new GeneratedDocumentResponse(
				document.getId(),
				document.getTemplateType().name(),
				document.getTemplateType().getDisplayName(),
				document.getOrderId(),
				document.getDocumentNumber(),
				document.getDocumentDate(),
				document.getFileKey(),
				document.getGeneratedAt(),
				document.getGeneratedBy()
		);
	}
}
