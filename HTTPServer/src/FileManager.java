import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import exception.FileAccessDeniedException;
import exception.PathNotAllowedException;
import exception.NotAbsoluteFilePathException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileManager {
	
	// Mapping files that are being read/written to, to the user that is using the file
	private HashMap <String,String> currentActiveFiles;
	
	// Singleton instance
	private static FileManager instance;
	
	/**
	 * Get instance of a Singleton
	 * @return FileManager singleton instance
	 */
	public static FileManager getInstance() {
		if (instance == null)
			instance = new FileManager();
		return instance;
	}
	
	/**
	 * Method to get the current files in the current directory
	 * @return String representation of the list of files in the directory
	 * @throws FileNotFoundException if the folder does not exist
	 * @throws PathNotAllowedException if the given path is unaccepted
	 */
	public String getCurrentFiles(String dir) throws FileNotFoundException, PathNotAllowedException {
		// File object to retrieve the list of files
		File folder = constructFile("", dir);
		
		// List of files in string format to be returned
		String listOfFilesAndFolders = "";
				
		// If the directory exists, get the list of files in it
		if(folder.exists() && folder.isDirectory()) {
			File[] directoryFiles = folder.listFiles();
			for(File file : directoryFiles) {
				listOfFilesAndFolders += file.getName() + "\n";
			}
		} else {
			// The folder does not exist 
			throw new FileNotFoundException("The folder " + dir + " does not exist.");
		}
		return listOfFilesAndFolders;
	}
	
	/**
	 * Method to get the content of a file in the data directory
	 * @param name fileName
	 * @param dir directory name
	 * @return content of the file as a String value
	 * @throws IOException 
	 * @throws PathNotAllowedException 
	 * @throws NotAbsoluteFilePathException 
	 * @throws FileAccessDeniedException 
	 */
	public String getFile(File file) throws NotAbsoluteFilePathException, IOException, FileAccessDeniedException, FileNotFoundException {
		
		if(file == null || !file.exists() || !file.isFile()) {
			throw new FileNotFoundException("File " + file.getName() + " is not found, or is not a file.");
		}
		
		// Check if the file is available
		// TODO: Replace with actual user identification
		if(this.attemptToAccessFile(file.getAbsolutePath(), "")) {
			try {
				byte[] encodedFileContent = Files.readAllBytes(Paths.get(file.getPath()));
				return new String (encodedFileContent, StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw e;
			} finally {
				this.removeFileFromActiveFiles(file.getAbsolutePath());
			}
		} else {
			throw new FileAccessDeniedException("The file is already being consulted by another user");
		}
	}
	
	/**
	 * Writes to a file
	 * @param filename
	 * @param dir
	 * @param fileContent
	 * @throws FileAccessDeniedException 
	 * @throws IOException 
	 * @throws NotAbsoluteFilePathException 
	 * @throws Exception 
	 */
	public void writeFile(File file, String fileContent, boolean append) throws NotAbsoluteFilePathException, FileAccessDeniedException, FileNotFoundException, IOException {
		
		if(file == null) {
			throw new FileNotFoundException("File is null");
		}
		
		// Check if we have access
		// TODO: Replace with actual user ID
		if(this.attemptToAccessFile(file.getAbsolutePath(), "")) {
			// Create any missing directories
			file.mkdirs();
			
			// Write or append to the file
			try {
				BufferedWriter outputFileWriter;
				if(append) {
					outputFileWriter = new BufferedWriter(new FileWriter(file.getPath(), true));
					outputFileWriter.append(fileContent);
				} else {
					outputFileWriter = new BufferedWriter(new FileWriter(file.getPath()));
					outputFileWriter.write(fileContent);
				}
				outputFileWriter.close();
			} catch (IOException e) {
				throw e;
			} finally {
				this.removeFileFromActiveFiles(file.getAbsolutePath());
			}
			
		} else {
			throw new FileAccessDeniedException("The file is already being consulted by another user");
		}
	}
	
	/**
	 * File constructor given the file name and directory
	 * @param fileName
	 * @param dir
	 * @return
	 * @throws PathNotAllowedException
	 */
	public File constructFile(String fileName, String dir) throws PathNotAllowedException {
		// Construct the path
		// For example /COMP445/directory/test.txt
		String filePath = dir + fileName;
		if(filePath.contains("..")) {
			throw new PathNotAllowedException("The directory path cannot contain \"..\"");
		} else {
			return new File(filePath);
		}
	}
	
	/**
	 * Attempts to gain permission to consult a file. If the file is currently being consulted by someone,
	 * permission is denied and false is returned. Otherwise, permission is provided and true is returned.
	 * @param absoluteFilePath
	 * @param userId 
	 * @return boolean true if permission granted, false otherwise
	 * @throws NotAbsoluteFilePathException
	 */
	public synchronized boolean attemptToAccessFile(String absoluteFilePath, String userId) throws NotAbsoluteFilePathException {
		File file = new File(absoluteFilePath);
		if(!file.isAbsolute()) {
			throw new NotAbsoluteFilePathException("The provided file path is not an absolute path.");
		} else if(this.currentActiveFiles.containsKey(absoluteFilePath)) {
			return false;
		} else {
			this.currentActiveFiles.put(absoluteFilePath, userId);
			return true;
		}
	}
	
	/**
	 * Removes a file from the list of active files
	 * @param absoluteFilePath
	 */
	public synchronized void removeFileFromActiveFiles(String absoluteFilePath) {
		this.currentActiveFiles.remove(absoluteFilePath);	
	}
	
	

}
