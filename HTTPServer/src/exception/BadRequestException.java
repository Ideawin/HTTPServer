package exception;

public class BadRequestException extends Exception {
	/**
	 * Default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public BadRequestException() {
		super();
	}
	
	/**
	 * Constructor with the error message
	 * @param message
	 */
	public BadRequestException(String message) {
		super(message);
	}
}
