package by.bsuir.catalogservice.exception;

public class InvalidOperationException extends RuntimeException {
	private final String operation;
	private final String reason;

	public InvalidOperationException(String operation, String reason) {
		super(String.format("Cannot perform operation '%s': %s", operation, reason));
		this.operation = operation;
		this.reason = reason;
	}

	public String getOperation() { return operation; }
	public String getReason() { return reason; }
}
