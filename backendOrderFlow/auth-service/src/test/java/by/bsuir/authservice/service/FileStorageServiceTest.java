package by.bsuir.authservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

	@InjectMocks
	private FileStorageService fileStorageService;

	@Mock
	private RabbitTemplate rabbitTemplate;

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
		@DisplayName("Should call document-service via RabbitMQ RPC and return objectKey")
		void shouldCallDocumentServiceViaRpcAndReturnObjectKey() {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.pdf", "application/pdf", "content".getBytes());

			Map<String, Object> rpcResponse = Map.of(
					"success", true,
					"objectKey", "company/1/documents/abc-123.pdf",
					"originalFilename", "test.pdf",
					"fileSize", 7,
					"contentType", "application/pdf");

			when(rabbitTemplate.convertSendAndReceive(
					eq("rpc.exchange"), eq("rpc.document.uploadFile"), any(Map.class)))
					.thenReturn(rpcResponse);

			String result = fileStorageService.storeFile(file, "company/1/documents");

			assertThat(result).isEqualTo("company/1/documents/abc-123.pdf");
			verify(rabbitTemplate).convertSendAndReceive(
					eq("rpc.exchange"), eq("rpc.document.uploadFile"), any(Map.class));
		}

		@Test
		@DisplayName("Should return placeholder path when RabbitMQ RPC fails")
		void shouldReturnPlaceholderWhenRpcFails() {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.pdf", "application/pdf", "content".getBytes());

			when(rabbitTemplate.convertSendAndReceive(
					eq("rpc.exchange"), eq("rpc.document.uploadFile"), any(Map.class)))
					.thenThrow(new RuntimeException("Connection refused"));

			String result = fileStorageService.storeFile(file, "subfolder");
			assertThat(result).isEqualTo("subfolder/test.pdf");
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
		@DisplayName("Should return URL from document-service via RabbitMQ RPC")
		void shouldReturnUrlFromDocumentServiceViaRpc() {
			Map<String, Object> rpcResponse = Map.of(
					"success", true,
					"url", "http://minio:9000/bucket/key?signature=abc",
					"objectKey", "company/1/logo.png");

			when(rabbitTemplate.convertSendAndReceive(
					eq("rpc.exchange"), eq("rpc.document.getPresignedUrl"), any(Map.class)))
					.thenReturn(rpcResponse);

			String result = fileStorageService.getPresignedUrl("company/1/logo.png");
			assertThat(result).isEqualTo("http://minio:9000/bucket/key?signature=abc");
		}

		@Test
		@DisplayName("Should return null when RabbitMQ RPC fails")
		void shouldReturnNullWhenRpcFails() {
			when(rabbitTemplate.convertSendAndReceive(
					eq("rpc.exchange"), eq("rpc.document.getPresignedUrl"), any(Map.class)))
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
			verifyNoInteractions(rabbitTemplate);
		}

		@Test
		@DisplayName("Should not throw exception when deleting empty key")
		void shouldNotThrowForEmptyKey() {
			fileStorageService.deleteFile("");
			verifyNoInteractions(rabbitTemplate);
		}

		@Test
		@DisplayName("Should call document-service delete via RabbitMQ RPC")
		void shouldCallDocumentServiceDeleteViaRpc() {
			Map<String, Object> rpcResponse = Map.of("success", true);

			when(rabbitTemplate.convertSendAndReceive(
					eq("rpc.exchange"), eq("rpc.document.deleteFile"), any(Map.class)))
					.thenReturn(rpcResponse);

			fileStorageService.deleteFile("some/path/file.pdf");
			verify(rabbitTemplate).convertSendAndReceive(
					eq("rpc.exchange"), eq("rpc.document.deleteFile"), any(Map.class));
		}

		@Test
		@DisplayName("Should not throw when RabbitMQ RPC fails on delete")
		void shouldNotThrowWhenDeleteFails() {
			when(rabbitTemplate.convertSendAndReceive(
					eq("rpc.exchange"), eq("rpc.document.deleteFile"), any(Map.class)))
					.thenThrow(new RuntimeException("fail"));

			fileStorageService.deleteFile("some/key");

		}
	}
}
