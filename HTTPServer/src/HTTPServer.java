import static java.util.Arrays.asList;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * This class is the entry point of the HTTP server application.
 * It is responsible for parsing the entry of the command, and creating
 * a HTTP RequestHandler object to listen to the clients
 *
 */
public class HTTPServer {

	// Default port that the server will listen and server
	public static final int DEFAULT_PORT = 8080;
	
	// Various arguments accepted by the parser
	public static final String ARG_VERBOSE = "v";
	public static final String ARG_PORT = "p";
	public static final String ARG_DIRECTORY = "d";
	
	/**
	 * Runs the server
	 * @param args set of arguments to define the settings of the server
	 */
	public static void main(String[] args) {
		
		// Define the options that the parser can take
		OptionParser parser = new OptionParser();
		
		parser.accepts(ARG_VERBOSE, "Prints debugging messages.");
		
		parser.accepts(ARG_PORT, "Port number that the server will listen and serve at.")
			.withRequiredArg()
			.ofType(Integer.class)
			.defaultsTo(DEFAULT_PORT);
		
		parser.accepts(ARG_DIRECTORY, "Specifies the directory that the server will use to read/write requested files.")
			.withRequiredArg();

		// Parse the given arguments
        OptionSet opts = parser.parse(args);
        boolean verbose = opts.has(ARG_VERBOSE);
        int port = (int) opts.valueOf(ARG_PORT);
        String directory = opts.has(ARG_DIRECTORY) ? (String) opts.valueOf(ARG_DIRECTORY) : null;
        
        System.out.println("verbose:" + verbose + "|port:" + port + "|directory:" + directory);
        
        // Start the server        
        // new HTTPRequestHandler(verbose, port, directory);
//        new HTTPRequestHandler().listenAndServe(port);
	}

}
