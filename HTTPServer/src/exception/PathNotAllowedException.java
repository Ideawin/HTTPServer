package exception;

public class PathNotAllowedException extends Exception {

	/**
	 * Default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public PathNotAllowedException() {
		super();
	}
	
	/**
	 * Constructor with the error message
	 * @param message
	 */
	public PathNotAllowedException(String message) {
		super(message);
	}

}
