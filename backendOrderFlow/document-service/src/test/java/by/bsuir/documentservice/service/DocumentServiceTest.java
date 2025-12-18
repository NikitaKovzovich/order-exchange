package by.bsuir.documentservice.service;

import by.bsuir.documentservice.dto.DocumentResponse;
import by.bsuir.documentservice.dto.UploadDocumentRequest;
import by.bsuir.documentservice.entity.Document;
import by.bsuir.documentservice.entity.DocumentType;
import by.bsuir.documentservice.exception.ResourceNotFoundException;
import by.bsuir.documentservice.repository.DocumentRepository;
import by.bsuir.documentservice.repository.DocumentTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

	@Mock
	private DocumentRepository documentRepository;

	@Mock
	private DocumentTypeRepository documentTypeRepository;

	@Mock
	private MinioService minioService;

	@InjectMocks
	private DocumentService documentService;

	private DocumentType testDocType;
	private Document testDocument;

	@BeforeEach
	void setUp() {
		testDocType = DocumentType.builder()
				.id(1L)
				.code("TTN")
				.name("Товарно-транспортная накладная")
				.build();

		testDocument = Document.builder()
				.id(1L)
				.documentType(testDocType)
				.entityType("order")
				.entityId(100L)
				.fileName("test.pdf")
				.fileKey("order/100/uuid.pdf")
				.fileSize(1024L)
				.mimeType("application/pdf")
				.uploadedBy(1L)
				.createdAt(LocalDateTime.now())
				.build();
	}

	@Nested
	@DisplayName("Upload Document Tests")
	class UploadDocumentTests {

		@Test
		@DisplayName("Should upload document successfully")
		void shouldUploadDocumentSuccessfully() throws Exception {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.pdf", "application/pdf", "test content".getBytes());
			UploadDocumentRequest request = new UploadDocumentRequest("TTN", "order", 100L);

			when(documentTypeRepository.findByCode("TTN")).thenReturn(Optional.of(testDocType));
			when(minioService.uploadFile(any(), any())).thenReturn("order/100/uuid.pdf");
			when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

			DocumentResponse response = documentService.uploadDocument(file, request, 1L);

			assertThat(response.fileName()).isEqualTo("test.pdf");
			assertThat(response.documentTypeCode()).isEqualTo("TTN");
			verify(minioService).uploadFile(file, "order/100");
		}

		@Test
		@DisplayName("Should throw when document type not found")
		void shouldThrowWhenDocumentTypeNotFound() {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.pdf", "application/pdf", "test".getBytes());
			UploadDocumentRequest request = new UploadDocumentRequest("INVALID", "order", 100L);

			when(documentTypeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> documentService.uploadDocument(file, request, 1L))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("Get Document Tests")
	class GetDocumentTests {

		@Test
		@DisplayName("Should get document by ID")
		void shouldGetDocumentById() {
			when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

			DocumentResponse response = documentService.getDocument(1L);

			assertThat(response.id()).isEqualTo(1L);
			assertThat(response.fileName()).isEqualTo("test.pdf");
		}

		@Test
		@DisplayName("Should throw when document not found")
		void shouldThrowWhenDocumentNotFound() {
			when(documentRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> documentService.getDocument(999L))
					.isInstanceOf(ResourceNotFoundException.class);
		}

		@Test
		@DisplayName("Should get documents by entity")
		void shouldGetDocumentsByEntity() {
			when(documentRepository.findByEntityTypeAndEntityId("order", 100L))
					.thenReturn(List.of(testDocument));

			List<DocumentResponse> response = documentService.getDocumentsByEntity("order", 100L);

			assertThat(response).hasSize(1);
			assertThat(response.get(0).entityId()).isEqualTo(100L);
		}
	}

	@Nested
	@DisplayName("Download Document Tests")
	class DownloadDocumentTests {

		@Test
		@DisplayName("Should get download URL")
		void shouldGetDownloadUrl() throws Exception {
			when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
			when(minioService.getPresignedUrl("order/100/uuid.pdf", 3600))
					.thenReturn("http://minio/presigned-url");

			String url = documentService.getDownloadUrl(1L);

			assertThat(url).isEqualTo("http://minio/presigned-url");
		}
	}

	@Nested
	@DisplayName("Delete Document Tests")
	class DeleteDocumentTests {

		@Test
		@DisplayName("Should delete document")
		void shouldDeleteDocument() throws Exception {
			when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
			doNothing().when(minioService).deleteFile("order/100/uuid.pdf");
			doNothing().when(documentRepository).delete(testDocument);

			documentService.deleteDocument(1L);

			verify(minioService).deleteFile("order/100/uuid.pdf");
			verify(documentRepository).delete(testDocument);
		}
	}
}
