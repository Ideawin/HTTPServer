package exception;

public class NotAbsoluteFilePathException extends Exception {
	/**
	 * Default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public NotAbsoluteFilePathException() {
		super();
	}
	
	/**
	 * Constructor with the error message
	 * @param message
	 */
	public NotAbsoluteFilePathException(String message) {
		super(message);
	}
}
