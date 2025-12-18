package by.bsuir.documentservice.controller;

import by.bsuir.documentservice.dto.DocumentResponse;
import by.bsuir.documentservice.exception.ResourceNotFoundException;
import by.bsuir.documentservice.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DocumentService documentService;

	private DocumentResponse testDocumentResponse;

	@BeforeEach
	void setUp() {
		testDocumentResponse = new DocumentResponse(
				1L, "TTN", "ТТН", "order", 100L,
				"test.pdf", "order/100/uuid.pdf", 1024L,
				"application/pdf", 1L, LocalDateTime.now()
		);
	}

	@Test
	@DisplayName("Should upload document")
	void shouldUploadDocument() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "test.pdf", "application/pdf", "content".getBytes());

		when(documentService.uploadDocument(any(), any(), eq(1L))).thenReturn(testDocumentResponse);

		mockMvc.perform(multipart("/api/documents")
						.file(file)
						.param("documentTypeCode", "TTN")
						.param("entityType", "order")
						.param("entityId", "100")
						.header("X-User-Id", "1"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.fileName").value("test.pdf"));
	}

	@Test
	@DisplayName("Should get document by ID")
	void shouldGetDocumentById() throws Exception {
		when(documentService.getDocument(1L)).thenReturn(testDocumentResponse);

		mockMvc.perform(get("/api/documents/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.documentTypeCode").value("TTN"));
	}

	@Test
	@DisplayName("Should get documents by entity")
	void shouldGetDocumentsByEntity() throws Exception {
		when(documentService.getDocumentsByEntity("order", 100L))
				.thenReturn(List.of(testDocumentResponse));

		mockMvc.perform(get("/api/documents/entity/order/100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].entityId").value(100));
	}

	@Test
	@DisplayName("Should get download URL")
	void shouldGetDownloadUrl() throws Exception {
		when(documentService.getDownloadUrl(1L)).thenReturn("http://minio/presigned-url");

		mockMvc.perform(get("/api/documents/1/url"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value("http://minio/presigned-url"));
	}

	@Test
	@DisplayName("Should download document")
	void shouldDownloadDocument() throws Exception {
		when(documentService.getDocument(1L)).thenReturn(testDocumentResponse);
		when(documentService.downloadDocument(1L))
				.thenReturn(new ByteArrayInputStream("content".getBytes()));

		mockMvc.perform(get("/api/documents/1/download"))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Disposition", "attachment; filename=\"test.pdf\""));
	}

	@Test
	@DisplayName("Should delete document")
	void shouldDeleteDocument() throws Exception {
		doNothing().when(documentService).deleteDocument(1L);

		mockMvc.perform(delete("/api/documents/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("Should return 404 when document not found")
	void shouldReturn404WhenDocumentNotFound() throws Exception {
		when(documentService.getDocument(999L))
				.thenThrow(new ResourceNotFoundException("Document", "id", 999L));

		mockMvc.perform(get("/api/documents/999"))
				.andExpect(status().isNotFound());
	}
}
