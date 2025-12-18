package by.bsuir.catalogservice.exception;

public class BusinessLogicException extends RuntimeException {
	private final String errorCode;

	public BusinessLogicException(String message) {
		super(message);
		this.errorCode = "BUSINESS_ERROR";
	}

	public BusinessLogicException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public String getErrorCode() { return errorCode; }
}
