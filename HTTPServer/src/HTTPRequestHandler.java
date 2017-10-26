import java.util.HashMap;

import exception.FileAccessDeniedException;
import exception.NotAbsoluteFilePathException;
import exception.PathNotAllowedException;

import java.io.FileNotFoundException;
import java.util.Date;

public class HTTPRequestHandler {
	
	FileManager fileManager;
	
	private static final String PROTOCOL = "HTTP/1.0";
	public static final String DEFAULT_HOST = "localhost:8080";
	private String statusLine;
	private String requestMethod;
	private HashMap<String,String> requestHeaders;
	private String requestBody;
	private String url = "http://";
	
	// Server-related
	Boolean verbose;
	int port;
	String requestURI;
	
	/**
	 * Constructor
	 * @param verbose
	 * @param port
	 * @param dir
	 */
	public HTTPRequestHandler(Boolean verbose, int port, String dir) {
		this.verbose = verbose;
		this.port = port;
		this.requestURI = dir;
		fileManager = FileManager.getInstance();
		requestHeaders = new HashMap<String,String>();
	}
	
	/**
	 * Method that will handle a request
	 * @param request the request in String format
	 */
    public String handleRequest(String request) {
    	// Split into one string for request line and header, and another for the body
		String[] strArr = request.split("\r\n\r\n");
		if (strArr.length == 2) {
			requestBody = strArr[1];
		}
		// Split a string into separate strings for each line
		String[] requestLineAndHeaders = strArr[0].split("\r\n");
		if (requestLineAndHeaders.length <= 1) {
			return "Bad request"; // TO-DO get status code
		}
		else {
			// Parse the first line (request line)
			getRequest(strArr[0]);
			// Get all the headers
			getHeaders(strArr);
			url += requestHeaders.get("Host") + requestURI; // http://localhost:8080/COMP445/requestURIhere
			return parseRequest(); // return a response
		}
    }
    
    /**
     * Method to get the request line and set the attributes to the corresponding value
     * @param request request line as a String value
     */
    public void getRequest(String request) {
    	String[] strArr = request.split(" ");
    	this.requestMethod = strArr[0]; // GET or POST
    	this.requestURI += strArr[1]; // directory should be "/COMP445 + requestURI"
    }
    
    /**
     * Method to put headers into the HashMap requestHeaders 
     * @param strArr array of header lines as String values
     */
    public void getHeaders(String[] strArr) {
		for (int i = 1; i < strArr.length; i++) {
			String[] keyValues = strArr[i].split(" ");
			int keyLength = keyValues[0].length();
			requestHeaders.put(keyValues[0].substring(0, keyLength-1), keyValues[1]); //keyLength - 1 so that you don't take the ':'
		}
		requestHeaders.put("Server", "");
		requestHeaders.put("Date", "");
		requestHeaders.put("Connection", "close");
    }
    
    /**
     * Method that will generate a response to the request
     */
    public String parseRequest() {
    	String response;
    	if (requestMethod.equalsIgnoreCase("GET")) {
    		
    	}
    	else if (requestMethod.equalsIgnoreCase("POST")) {
    		
    	}
    	return "";
    }
    
    /**
     * Obtain the error status code and reason phrase associated with an Exception
     * @param e Exception
     * @return String array containing the status code at index 0 and reason phrase at index 1
     */
    public String[] getErrorCode(Exception e) {
    	// Index 0 contains the status code
    	// Index 1 contains the reason phrase
    	String [] statusCodeReasonPhrase = new String[2];
    	
    	if(e instanceof FileAccessDeniedException) {
    		// Do not allow concurrent access to the same file
    		statusCodeReasonPhrase[0] = "503";
    		statusCodeReasonPhrase[1] = "Service Unavailable";
    	} else if (e instanceof PathNotAllowedException) {
    		// Client put illegal path such as ".."
    		statusCodeReasonPhrase[0] = "401";
    		statusCodeReasonPhrase[1] = "Unauthorized"; 
    	} else if (e instanceof FileNotFoundException) {
    		// Trying to read a file that does not exist
    		statusCodeReasonPhrase[0] = "404";
    		statusCodeReasonPhrase[1] = "Not Found"; 
    	} else {
    		// IOException or NotAbsoluteFilePathException
    		statusCodeReasonPhrase[0] = "500";
    		statusCodeReasonPhrase[1] = "Internal Server Error";
    	}
    	return statusCodeReasonPhrase;    	
    }

}
