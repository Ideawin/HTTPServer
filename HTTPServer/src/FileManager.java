import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FileManager {
	
	HashMap <String,String> currentDirectories;
	private static FileManager instance;
	
	/**
	 * Get instance of a Singleton
	 * @return
	 */
	public static FileManager getInstance() {
		if (instance == null)
			instance = new FileManager();
		return instance;
	}
	
	/**
	 * Given the path and the encoding of a file, this method reads the entire content of the file and decodes it into a string
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encodedFileContent = Files.readAllBytes(Paths.get(path));
		return new String (encodedFileContent, encoding);
	}
	
	/**
	 * Method to get the current files in the current directory
	 * @return
	 */
	String getCurrentFiles(String dir) {
		if (dir == null) {
			// default directory
		}
		else {
			// -d directory
		}
		return "";
	}
	
	/**
	 * Method to get the content of a file in the data directory
	 * @param name fileName
	 * @param dir directory name
	 * @return content of the file as a String value
	 */
	String getFile(String fileName, String dir) {
		String filePath = dir;
		// For example /COMP445/directory/test.txt
		
		try {
			String fileContent = readFile(filePath, StandardCharsets.UTF_8);
			return fileContent;
		} catch (IOException e) {
			String errorMessage = "HTTP ERROR xxx";
			System.err.println(e.getMessage());
			return errorMessage;
		}
	}
	
	

}
