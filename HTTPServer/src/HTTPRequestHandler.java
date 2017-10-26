import java.util.HashMap;

import exception.BadRequestException;
import exception.FileAccessDeniedException;
import exception.NoContentException;
import exception.NotAbsoluteFilePathException;
import exception.NotImplementedException;
import exception.PathNotAllowedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

public class HTTPRequestHandler {

	FileManager fileManager;

	private static final String PROTOCOL = "HTTP/1.0";
	public static final String DEFAULT_HOST = "localhost:8080";
	private String statusLine;
	private String requestMethod;
	private HashMap<String,String> requestHeaders;
	private String requestBody;
	private String responseBody;
	String[] errorCode;

	// Server-related
	Boolean verbose;
	int port;
	String requestURI;
	String host;

	/**
	 * Constructor
	 * @param verbose
	 * @param port
	 * @param dir
	 */
	public HTTPRequestHandler(Boolean verbose, int port, String dir) {
		statusLine = "";
		requestMethod = "";
		requestBody = "";
		responseBody = "";
		errorCode = new String[2];
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
		try {
			// Split into one string for request line and header, and another for the body
			String[] strArr = request.split("\r\n\r\n");
			// Get all the headers
			getHeaders(strArr);
			// Get the host
			this.host = requestHeaders.get("Host");
			requestHeaders.remove("Host");
			
			if (strArr.length == 2) {
				requestBody = strArr[1];
			}
			// Split a string into separate strings for each line
			String[] requestLineAndHeaders = strArr[0].split("\r\n");
			// If there is only one line or less, it is not a valid request
			if (requestLineAndHeaders.length <= 1) {
				errorCode = getErrorCode(new BadRequestException());
				statusLine = PROTOCOL + " "  + errorCode[0] + " " + errorCode[1];
			}
			else {
				// Parse the first line (request line)
				getRequest(strArr[0]);
				// Parse request
				parseRequest();
			}
		}
		catch (Exception e) {
			errorCode = getErrorCode(e);
			statusLine = PROTOCOL + " "  + errorCode[0] + " " + errorCode[1];
		}
		// Generate a response and return to the client
		return createResponse();
	}

	/**
	 * Method to get the request line and set the attributes to the corresponding value
	 * @param request request line as a String value
	 */
	public void getRequest(String request) throws BadRequestException, NotImplementedException {
		String[] strArr = request.split(" ");

		if (strArr.length <= 2) {
			throw new BadRequestException();
		}
		else {
			this.requestMethod = strArr[0]; // GET or POST
			if (this.requestMethod != "GET" || this.requestMethod != "POST") {
				throw new NotImplementedException();
			}
			this.requestURI += strArr[1]; // directory should be "/COMP445 + requestURI"
		}
	}

	/**
	 * Method to put headers into the HashMap requestHeaders 
	 * @param strArr array of header lines as String values
	 */
	public void getHeaders(String[] strArr) {
		requestHeaders.put("Server", "Winnor");
		requestHeaders.put("Date", new Date().toString());
		requestHeaders.put("Connection", "close");
		for (int i = 1; i < strArr.length; i++) {
			String[] keyValues = strArr[i].split(" ");
			int keyLength = keyValues[0].length();
			requestHeaders.put(keyValues[0].substring(0, keyLength-1), keyValues[1]); //keyLength - 1 so that you don't take the ':'
		}
	}

	/**
	 * Method that will parse the request and set the response body and/or the status line
	 */
	public void parseRequest() {
		statusLine = PROTOCOL + " 200 OK";
		try {
			if (requestMethod.equalsIgnoreCase("GET")) {
				if (this.requestURI.charAt(requestURI.length() - 1) == '/') // ex. for GET / or GET /dir/ 
					responseBody = fileManager.getCurrentFiles(this.requestURI);
				else {
					responseBody = fileManager.getFile(fileManager.constructFile(this.requestURI)); // ex. for GET /dir/fileName
				}
			}
			else if (requestMethod.equalsIgnoreCase("POST")) {

			}
		}
		catch (Exception e) {
			errorCode = getErrorCode(e);
			statusLine = PROTOCOL + " "  + errorCode[0] + " " + errorCode[1];
		}
	}

	/**
	 * Method to create a response as String format to send to the client
	 * @return
	 */
	public String createResponse() {
		// first line is the status line
		String response = statusLine + "\r\n";
		// next few lines are the header lines (excluding Host which was removed in HandleRequest)
		if (!requestHeaders.isEmpty()) {
			for (String key : requestHeaders.keySet()) {
				response += key + ": " + requestHeaders.get(key) + "\r\n";
			}
		}
		// request body
		if (!responseBody.isEmpty()) {
			response += "\r\n" + responseBody + "\r\n\r\n";
		}
		else
			response += "\r\n";
		System.out.println("Response to client: \n" + response);
		return response;
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
    	} else if (e instanceof BadRequestException) {
    		// If client sends a bad request
    		statusCodeReasonPhrase[0] = "400";
    		statusCodeReasonPhrase[1] = "Bad Request"; 
    	} else if (e instanceof PathNotAllowedException) {
    		// Client put illegal path such as ".."
    		statusCodeReasonPhrase[0] = "401";
    		statusCodeReasonPhrase[1] = "Unauthorized"; 
    	} else if (e instanceof FileNotFoundException) {
    		// Trying to read a file that does not exist
    		statusCodeReasonPhrase[0] = "404";
    		statusCodeReasonPhrase[1] = "Not Found"; 
    	} else if (e instanceof NoContentException) {
    		// The requested folder to display is empty
    		statusCodeReasonPhrase[0] = "204";
    		statusCodeReasonPhrase[1] = "No Content"; 
    	} else if (e instanceof NotImplementedException) {
    		// If the request is not implemented
    		statusCodeReasonPhrase[0] = "501";
    		statusCodeReasonPhrase[1] = "Not Implemented";
    	} else {
    		// IOException or NotAbsoluteFilePathException
    		statusCodeReasonPhrase[0] = "500";
    		statusCodeReasonPhrase[1] = "Internal Server Error";
    	}
    	return statusCodeReasonPhrase;    	
    }

}
