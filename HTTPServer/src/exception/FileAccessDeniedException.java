package exception;

public class FileAccessDeniedException extends Exception {
	/**
	 * Default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public FileAccessDeniedException() {
		super();
	}
	
	/**
	 * Constructor with the error message
	 * @param message
	 */
	public FileAccessDeniedException(String message) {
		super(message);
	}
}
