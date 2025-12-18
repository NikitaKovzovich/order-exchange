package by.bsuir.documentservice.integration;

import by.bsuir.documentservice.entity.Document;
import by.bsuir.documentservice.entity.DocumentType;
import by.bsuir.documentservice.repository.DocumentRepository;
import by.bsuir.documentservice.repository.DocumentTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DocumentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    private Document testDocument;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();

        DocumentType invoiceType = documentTypeRepository.findByCode("INVOICE")
                .orElseGet(() -> documentTypeRepository.save(DocumentType.builder()
                        .code("INVOICE")
                        .name("Счёт-фактура")
                        .description("Счёт-фактура для заказа")
                        .build()));

        testDocument = Document.builder()
                .documentType(invoiceType)
                .entityType("order")
                .entityId(1L)
                .fileName("test-invoice.pdf")
                .fileKey("documents/test-invoice.pdf")
                .mimeType("application/pdf")
                .fileSize(1024L)
                .uploadedBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        testDocument = documentRepository.save(testDocument);
    }

    @Test
    @DisplayName("Should get documents by entity")
    void shouldGetDocumentsByEntity() throws Exception {
        mockMvc.perform(get("/api/documents/entity/order/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should get document by ID")
    void shouldGetDocumentById() throws Exception {
        mockMvc.perform(get("/api/documents/" + testDocument.getId())
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
