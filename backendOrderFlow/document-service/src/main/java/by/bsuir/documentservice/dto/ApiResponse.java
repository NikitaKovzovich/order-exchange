package by.bsuir.documentservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private boolean success;
	private String message;
	private T data;
	private List<FieldError> errors;
	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now();

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FieldError {
		private String field;
		private String message;
	}

	public static <T> ApiResponse<T> success(T data) {
		return ApiResponse.<T>builder().success(true).data(data).build();
	}

	public static <T> ApiResponse<T> success(T data, String message) {
		return ApiResponse.<T>builder().success(true).message(message).data(data).build();
	}

	public static <T> ApiResponse<T> error(String message) {
		return ApiResponse.<T>builder().success(false).message(message).build();
	}

	public static <T> ApiResponse<T> error(String message, List<FieldError> errors) {
		return ApiResponse.<T>builder().success(false).message(message).errors(errors).build();
	}

	public static <T> ApiResponse<T> validationError(List<FieldError> errors) {
		return ApiResponse.<T>builder().success(false).message("Validation failed").errors(errors).build();
	}
}
