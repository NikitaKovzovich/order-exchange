package by.bsuir.documentservice.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThymeleafPdfService {

	private static final String FONT_PATH = "/fonts/DejaVuSans.ttf";

	private final TemplateEngine templateEngine;

	public byte[] renderToPdf(String templateName, Map<String, Object> variables) {
		try {
			Context context = new Context();
			variables.forEach(context::setVariable);
			String html = templateEngine.process(templateName, context);

			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				PdfRendererBuilder builder = new PdfRendererBuilder();
				builder.useFastMode();
				attachFontIfPresent(builder);
				builder.withHtmlContent(html, null);
				builder.toStream(out);
				builder.run();
				return out.toByteArray();
			}
		} catch (Exception e) {
			log.error("Failed to render PDF from template {}: {}", templateName, e.getMessage(), e);
			throw new RuntimeException("Failed to render PDF: " + e.getMessage(), e);
		}
	}

	private void attachFontIfPresent(PdfRendererBuilder builder) {
		try (InputStream fontStream = getClass().getResourceAsStream(FONT_PATH)) {
			if (fontStream == null) {
				log.warn("Font {} not found on classpath; Cyrillic text may render incorrectly. Place a Unicode TTF (e.g. DejaVuSans.ttf) here.", FONT_PATH);
				return;
			}
			byte[] bytes = fontStream.readAllBytes();
			builder.useFont(() -> new ByteArrayInputStream(bytes), "DejaVu Sans");
		} catch (Exception e) {
			log.warn("Failed to attach PDF font: {}", e.getMessage());
		}
	}
}
