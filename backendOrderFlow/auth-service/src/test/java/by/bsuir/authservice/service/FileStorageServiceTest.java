package by.bsuir.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

	@InjectMocks
	private FileStorageService fileStorageService;

	@Mock
	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(fileStorageService, "documentServiceUrl", "http://localhost:8085");
		ReflectionTestUtils.setField(fileStorageService, "restTemplate", restTemplate);
	}

	@Nested
	@DisplayName("Store File Tests")
	class StoreFileTests {

		@Test
		@DisplayName("Should return null for null file")
		void shouldReturnNullForNullFile() {
			String result = fileStorageService.storeFile(null, "subfolder");
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should return null for empty file")
		void shouldReturnNullForEmptyFile() {
			MockMultipartFile emptyFile = new MockMultipartFile(
					"file", "empty.pdf", "application/pdf", new byte[0]);
			String result = fileStorageService.storeFile(emptyFile, "subfolder");
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should call document-service and return objectKey")
		void shouldCallDocumentServiceAndReturnObjectKey() {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.pdf", "application/pdf", "content".getBytes());

			Map<String, Object> responseBody = Map.of(
					"objectKey", "company/1/documents/abc-123.pdf",
					"originalFilename", "test.pdf");

			when(restTemplate.exchange(
					anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
					.thenReturn(new ResponseEntity<>(responseBody, HttpStatus.CREATED));

			String result = fileStorageService.storeFile(file, "company/1/documents");

			assertThat(result).isEqualTo("company/1/documents/abc-123.pdf");
			verify(restTemplate).exchange(
					contains("/api/documents/upload"),
					eq(HttpMethod.POST),
					any(HttpEntity.class),
					eq(Map.class));
		}

		@Test
		@DisplayName("Should throw exception when document-service fails")
		void shouldThrowExceptionWhenDocumentServiceFails() {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.pdf", "application/pdf", "content".getBytes());

			when(restTemplate.exchange(
					anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
					.thenThrow(new RuntimeException("Connection refused"));

			assertThatThrownBy(() -> fileStorageService.storeFile(file, "subfolder"))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Failed to store file");
		}
	}

	@Nested
	@DisplayName("Get Presigned URL Tests")
	class GetPresignedUrlTests {

		@Test
		@DisplayName("Should return null for null objectKey")
		void shouldReturnNullForNullKey() {
			assertThat(fileStorageService.getPresignedUrl(null)).isNull();
		}

		@Test
		@DisplayName("Should return null for empty objectKey")
		void shouldReturnNullForEmptyKey() {
			assertThat(fileStorageService.getPresignedUrl("")).isNull();
		}

		@Test
		@DisplayName("Should return URL from document-service")
		void shouldReturnUrlFromDocumentService() {
			Map<String, Object> responseBody = Map.of("url", "http://minio:9000/bucket/key?signature=abc");

			when(restTemplate.getForEntity(anyString(), eq(Map.class)))
					.thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

			String result = fileStorageService.getPresignedUrl("company/1/logo.png");
			assertThat(result).isEqualTo("http://minio:9000/bucket/key?signature=abc");
		}

		@Test
		@DisplayName("Should return null when document-service fails")
		void shouldReturnNullWhenServiceFails() {
			when(restTemplate.getForEntity(anyString(), eq(Map.class)))
					.thenThrow(new RuntimeException("Connection refused"));

			String result = fileStorageService.getPresignedUrl("some/key");
			assertThat(result).isNull();
		}
	}

	@Nested
	@DisplayName("Delete File Tests")
	class DeleteFileTests {

		@Test
		@DisplayName("Should not throw exception when deleting null key")
		void shouldNotThrowForNullKey() {
			fileStorageService.deleteFile(null);
			verifyNoInteractions(restTemplate);
		}

		@Test
		@DisplayName("Should not throw exception when deleting empty key")
		void shouldNotThrowForEmptyKey() {
			fileStorageService.deleteFile("");
			verifyNoInteractions(restTemplate);
		}

		@Test
		@DisplayName("Should call document-service delete")
		void shouldCallDocumentServiceDelete() {
			fileStorageService.deleteFile("some/path/file.pdf");
			verify(restTemplate).delete(contains("/api/documents?objectKey=some/path/file.pdf"));
		}

		@Test
		@DisplayName("Should not throw when document-service fails on delete")
		void shouldNotThrowWhenDeleteFails() {
			doThrow(new RuntimeException("fail")).when(restTemplate).delete(anyString());
			fileStorageService.deleteFile("some/key");
			// Should not throw
		}
	}
}
