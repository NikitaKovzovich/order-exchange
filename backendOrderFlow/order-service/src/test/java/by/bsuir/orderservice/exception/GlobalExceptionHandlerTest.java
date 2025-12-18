package by.bsuir.orderservice.exception;

import by.bsuir.orderservice.dto.ApiResponse;
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
		ResourceNotFoundException ex = new ResourceNotFoundException("Order", "id", 1L);

		ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFound(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
	}

	@Test
	@DisplayName("Should handle InvalidStateTransitionException")
	void shouldHandleInvalidStateTransitionException() {
		InvalidStateTransitionException ex = new InvalidStateTransitionException("PENDING", "SHIPPED");

		ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidStateTransition(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
	}

	@Test
	@DisplayName("Should handle InvalidOperationException")
	void shouldHandleInvalidOperationException() {
		InvalidOperationException ex = new InvalidOperationException("confirm", "Order does not belong to supplier");

		ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidOperation(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
	}

	@Test
	@DisplayName("Should handle MethodArgumentNotValidException")
	void shouldHandleMethodArgumentNotValidException() {
		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		BindingResult bindingResult = mock(BindingResult.class);
		FieldError fieldError = new FieldError("order", "supplierId", "must not be null");

		when(ex.getBindingResult()).thenReturn(bindingResult);
		when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

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
	@DisplayName("Should handle generic Exception")
	void shouldHandleGenericException() {
		Exception ex = new RuntimeException("Unexpected error");

		ResponseEntity<ApiResponse<Void>> response = handler.handleGeneral(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().isSuccess()).isFalse();
		assertThat(response.getBody().getMessage()).contains("Internal server error");
	}
}
