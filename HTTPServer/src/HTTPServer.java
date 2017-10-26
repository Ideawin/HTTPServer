import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import exception.FileAccessDeniedException;
import exception.NotAbsoluteFilePathException;
import exception.PathNotAllowedException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * This class is the entry point of the HTTP server application.
 * It is responsible for parsing the entry of the command, and creating
 * a HTTP RequestHandler object to listen to the clients
 *
 */
public class HTTPServer {

	// Default port that the server will listen and serve
	public static final int DEFAULT_PORT = 8080;
	public static final String DEFAULT_HOST = "localhost:8080";
	public static final String DEFAULT_DIRECTORY = "/COMP445";
	// Attributes of HTTPServer
	public int port;
	public boolean verbose;
	public String directory;

	// Various arguments accepted by the parser
	public static final String ARG_VERBOSE = "v";
	public static final String ARG_PORT = "p";
	public static final String ARG_DIRECTORY = "d";

	/**
	 * Constructor
	 * @param verbose
	 * @param port
	 * @param directory
	 * @throws PathNotAllowedException 
	 * @throws FileNotFoundException 
	 */
	public HTTPServer(boolean verbose, int port, String directory) {
		// Check if the given working directory exists 
		try {
			File dir = FileManager.getInstance().constructFile(directory);
			if(!dir.exists() || !dir.isDirectory()) {
				throw new FileNotFoundException();
			}
		} catch (Exception e) {
			System.out.println("The given working directory does not exist or the path is invalid.");
			System.exit(1);
		}

		this.verbose = verbose;
		this.port = port;
		this.directory = directory;
	}

	// Uses a single buffer to demonstrate that all clients are running in a single thread
	private final ByteBuffer buffer = ByteBuffer.allocate(1024);

	/**
	 * Method to read the buffer and get the request as a String
	 * @param s
	 */
	private void readAndGetRequest(SelectionKey s) throws FileAccessDeniedException, NotAbsoluteFilePathException, PathNotAllowedException  {
		SocketChannel client = (SocketChannel) s.channel();
		try {
			for (; ; ) {
				int n = client.read(buffer);
				// If the number of bytes read is -1, the peer is closed
				if (n == -1) {
					unregisterClient(s);
					return;
				}
				if (n == 0) {
					return;
				}
				// ByteBuffer is tricky, you have to flip when switch from read to write, or vice-versa
				buffer.flip();

				// Convert buffer into a string
				Charset utf8 = StandardCharsets.UTF_8;
				String request = utf8.decode(buffer).toString();
				buffer.clear();
				if(verbose) {
					System.out.println("Request received:\n" + request);
				}

				// Handle request
				HTTPRequestHandler requestHandler = new HTTPRequestHandler(verbose, port, directory);
				String response = requestHandler.handleRequest(request);
				if(verbose) {
					System.out.println("Response sent:\n" + response);
				}

				// Write response to the socket using a buffer
				buffer.put(utf8.encode(response)); // encode string response into utf8 and add to buffer
				buffer.flip();
				client.write(buffer); // write buffer to the socket
				buffer.clear();
				
				client.close();
				//client.shutdownOutput();
			}
		} catch (IOException e) {
			unregisterClient(s);
			System.out.println("Failed to receive/send data");
		}
	}

	private void newClient(ServerSocketChannel server, Selector selector) {
		try {
			SocketChannel client = server.accept();
			client.configureBlocking(false);
			System.out.println("New client from {" +  client.getRemoteAddress() + "}");
			client.register(selector, OP_READ, client);
		} catch (IOException e) {
			System.out.println("Failed to accept client");
		}
	}

	private void unregisterClient(SelectionKey s) {
		try {
			s.cancel();
			s.channel().close();
		} catch (IOException e) {
			System.out.println("Failed to clean up");
		}
	}

	private void runLoop(ServerSocketChannel server, Selector selector) throws IOException, FileAccessDeniedException, NotAbsoluteFilePathException, PathNotAllowedException  {
		// Check if there is any event (eg. new client or new data) happened
		selector.select();

		for (SelectionKey s : selector.selectedKeys()) {
			// Acceptable means there is a new incoming
			if (s.isAcceptable()) {
				newClient(server, selector);

				// Readable means this client has sent data or closed
			} else if (s.isReadable()) {
				readAndGetRequest(s);
			}
		}
		// We must clear this set, otherwise the select will return the same value again
		selector.selectedKeys().clear();
	}

	private void listenAndServe() throws IOException, FileAccessDeniedException, FileNotFoundException, NotAbsoluteFilePathException, PathNotAllowedException {
		try (ServerSocketChannel server = ServerSocketChannel.open()) {
			server.bind(new InetSocketAddress(port));
			server.configureBlocking(false);
			Selector selector = Selector.open();

			// Register the server socket to be notified when there is a new incoming client
			server.register(selector, OP_ACCEPT, null);
			for (; ; ) {
				runLoop(server, selector);
			}
		}
	}

	/**
	 * Runs the server
	 * @param args set of arguments to define the settings of the server
	 */
	public static void main(String[] args) throws IOException, FileAccessDeniedException, NotAbsoluteFilePathException, PathNotAllowedException  {

		// Define the options that the parser can take
		OptionParser parser = new OptionParser();

		parser.accepts(ARG_VERBOSE, "Prints debugging messages.");

		parser.accepts(ARG_PORT, "Port number that the server will listen and serve at.")
		.withRequiredArg()
		.ofType(Integer.class)
		.defaultsTo(DEFAULT_PORT);

		parser.accepts(ARG_DIRECTORY, "Specifies the directory that the server will use to read/write requested files.")
		.withRequiredArg()
		.defaultsTo(DEFAULT_DIRECTORY);

		// Parse the given arguments
		OptionSet opts = parser.parse(args);
		boolean verbose = opts.has(ARG_VERBOSE);
		int port = (int) opts.valueOf(ARG_PORT);
		String directory = (String) opts.valueOf(ARG_DIRECTORY);

		System.out.println("verbose:" + verbose + "|port:" + port + "|directory:" + directory);

		// Start the server        
		new HTTPServer(verbose, port, directory).listenAndServe();
	}

}
