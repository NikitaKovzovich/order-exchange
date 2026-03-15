package by.bsuir.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		ErrorResponse response = new ErrorResponse(
				ex.getMessage(),
				HttpStatus.BAD_REQUEST.value(),
				System.currentTimeMillis()
		);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
		ErrorResponse response = new ErrorResponse(
				"Access denied",
				HttpStatus.FORBIDDEN.value(),
				System.currentTimeMillis()
		);
		return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {

		ErrorResponse response = new ErrorResponse(
				"Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				System.currentTimeMillis()
		);
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
