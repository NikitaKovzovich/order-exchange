package by.bsuir.catalogservice.exception;

import by.bsuir.catalogservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.error(ex.getMessage()));
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(DuplicateResourceException ex) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(ApiResponse.error(ex.getMessage()));
	}

	@ExceptionHandler(BusinessLogicException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessLogic(BusinessLogicException ex) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error(ex.getMessage()));
	}

	@ExceptionHandler(InsufficientStockException.class)
	public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(ApiResponse.error(ex.getMessage()));
	}

	@ExceptionHandler(InvalidOperationException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidOperation(InvalidOperationException ex) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error(ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
		List<ApiResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> ApiResponse.FieldError.builder()
						.field(e.getField())
						.message(e.getDefaultMessage())
						.build())
				.collect(Collectors.toList());

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error("Validation failed", errors));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error(ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("Internal server error: " + ex.getMessage()));
	}
}
