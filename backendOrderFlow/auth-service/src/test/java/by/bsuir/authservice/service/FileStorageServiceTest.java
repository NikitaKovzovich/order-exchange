package by.bsuir.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class FileStorageServiceTest {

	private FileStorageService fileStorageService;

	@BeforeEach
	void setUp() {
		fileStorageService = new FileStorageService();
		ReflectionTestUtils.setField(fileStorageService, "uploadDir", "/test/uploads");
	}

	@Nested
	@DisplayName("Store File Tests")
	class StoreFileTests {

		@Test
		@DisplayName("Should store file and return path")
		void shouldStoreFileAndReturnPath() {
			MockMultipartFile file = new MockMultipartFile(
					"file",
					"test-document.pdf",
					"application/pdf",
					"test content".getBytes()
			);

			String result = fileStorageService.storeFile(file, "company/1/documents");

			assertThat(result).isNotNull();
			assertThat(result).startsWith("company/1/documents/");
			assertThat(result).endsWith(".pdf");
		}

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
					"file",
					"empty.pdf",
					"application/pdf",
					new byte[0]
			);

			String result = fileStorageService.storeFile(emptyFile, "subfolder");

			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should handle file without extension")
		void shouldHandleFileWithoutExtension() {
			MockMultipartFile file = new MockMultipartFile(
					"file",
					"noextension",
					"application/octet-stream",
					"test content".getBytes()
			);

			String result = fileStorageService.storeFile(file, "company/1");

			assertThat(result).isNotNull();
			assertThat(result).startsWith("company/1/");
			assertThat(result).doesNotContain(".");
		}

		@Test
		@DisplayName("Should handle file with null original filename")
		void shouldHandleFileWithNullOriginalFilename() {
			MockMultipartFile file = new MockMultipartFile(
					"file",
					null,
					"application/pdf",
					"test content".getBytes()
			);

			String result = fileStorageService.storeFile(file, "subfolder");

			assertThat(result).isNotNull();
			assertThat(result).startsWith("subfolder/");
		}

		@Test
		@DisplayName("Should generate unique file names")
		void shouldGenerateUniqueFileNames() {
			MockMultipartFile file1 = new MockMultipartFile(
					"file",
					"document.pdf",
					"application/pdf",
					"content1".getBytes()
			);
			MockMultipartFile file2 = new MockMultipartFile(
					"file",
					"document.pdf",
					"application/pdf",
					"content2".getBytes()
			);

			String result1 = fileStorageService.storeFile(file1, "uploads");
			String result2 = fileStorageService.storeFile(file2, "uploads");

			assertThat(result1).isNotEqualTo(result2);
		}
	}

	@Nested
	@DisplayName("Load File Tests")
	class LoadFileTests {

		@Test
		@DisplayName("Should return full path for file")
		void shouldReturnFullPathForFile() {
			String result = fileStorageService.loadFile("company/1/document.pdf");

			assertThat(result).isEqualTo("/test/uploads/company/1/document.pdf");
		}
	}

	@Nested
	@DisplayName("Delete File Tests")
	class DeleteFileTests {

		@Test
		@DisplayName("Should not throw exception when deleting file")
		void shouldNotThrowExceptionWhenDeletingFile() {
			fileStorageService.deleteFile("some/path/file.pdf");
		}
	}
}
