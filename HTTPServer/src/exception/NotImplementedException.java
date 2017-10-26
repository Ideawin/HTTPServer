package exception;

public class NotImplementedException extends Exception {
	/**
	 * Default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public NotImplementedException() {
		super();
	}
	
	/**
	 * Constructor with the error message
	 * @param message
	 */
	public NotImplementedException(String message) {
		super(message);
	}
}
