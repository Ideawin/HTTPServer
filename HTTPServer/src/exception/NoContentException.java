package exception;

public class NoContentException extends Exception {
	/**
	 * Default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public NoContentException() {
		super();
	}
	
	/**
	 * Constructor with the error message
	 * @param message
	 */
	public NoContentException(String message) {
		super(message);
	}
}
