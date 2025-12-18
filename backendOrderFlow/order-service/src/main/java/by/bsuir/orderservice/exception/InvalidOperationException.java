package by.bsuir.orderservice.exception;

public class InvalidOperationException extends RuntimeException {
	private final String operation;

	public InvalidOperationException(String operation, String message) {
		super(String.format("Cannot %s: %s", operation, message));
		this.operation = operation;
	}

	public String getOperation() {
		return operation;
	}
}
