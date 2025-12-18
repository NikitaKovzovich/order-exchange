package by.bsuir.catalogservice.exception;

import by.bsuir.catalogservice.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

	private GlobalExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new GlobalExceptionHandler();
	}

	@Test
	@DisplayName("Should handle ResourceNotFoundException")
	void shouldHandleResourceNotFoundException() {
		ResourceNotFoundException ex = new ResourceNotFoundException("Product", "id", 1L);

		ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFound(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
		assertThat(response.getBody().getMessage()).contains("Product");
	}

	@Test
	@DisplayName("Should handle DuplicateResourceException")
	void shouldHandleDuplicateResourceException() {
		DuplicateResourceException ex = new DuplicateResourceException("Product", "sku", "SKU-001");

		ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateResource(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
	}

	@Test
	@DisplayName("Should handle InvalidOperationException")
	void shouldHandleInvalidOperationException() {
		InvalidOperationException ex = new InvalidOperationException("publish", "Cannot publish archived product");

		ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidOperation(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
	}

	@Test
	@DisplayName("Should handle InsufficientStockException")
	void shouldHandleInsufficientStockException() {
		InsufficientStockException ex = new InsufficientStockException(1L, 100, 50);

		ResponseEntity<ApiResponse<Void>> response = handler.handleInsufficientStock(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
		assertThat(response.getBody().getMessage()).contains("Insufficient");
	}

	@Test
	@DisplayName("Should handle validation exceptions")
	void shouldHandleValidationExceptions() {
		BindingResult bindingResult = mock(BindingResult.class);
		FieldError fieldError = new FieldError("product", "name", "Name is required");
		when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		when(ex.getBindingResult()).thenReturn(bindingResult);

		ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getErrors()).isNotEmpty();
	}

	@Test
	@DisplayName("Should handle IllegalArgumentException")
	void shouldHandleIllegalArgumentException() {
		IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

		ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgument(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
	}

	@Test
	@DisplayName("Should handle generic exceptions")
	void shouldHandleGenericExceptions() {
		Exception ex = new RuntimeException("Unexpected error");

		ResponseEntity<ApiResponse<Void>> response = handler.handleGeneral(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
	}

	@Test
	@DisplayName("Should handle BusinessLogicException")
	void shouldHandleBusinessLogicException() {
		BusinessLogicException ex = new BusinessLogicException("Business logic error");

		ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessLogic(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
	}
}
