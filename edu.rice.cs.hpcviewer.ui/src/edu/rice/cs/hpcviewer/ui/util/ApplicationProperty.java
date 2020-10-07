package edu.rice.cs.hpcviewer.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;


public class ApplicationProperty 
{
	private static final String FILE_VERSION = "platform:/plugin/edu.rice.cs.hpcviewer.ui/release.txt";
	private static final String FILE_LICENSE = "platform:/plugin/edu.rice.cs.hpcviewer.ui/License.txt";
	
	private static final String FILE_LOG_NAME = "hpcviewer.log";

	/***
	 * Get the application version 
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String getVersion() throws MalformedURLException, IOException {
		return getFileContent(FILE_VERSION);
	}
	

	/***
	 * Return the whole content of the license file
	 * @return String
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String getLicense() throws MalformedURLException, IOException {
		return getFileContent(FILE_LICENSE);
	}

	
	/***
	 * Load and return the whole content of a file.
	 *  
	 * @param urlPathToFile String
	 * @return String
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String getFileContent(String urlPathToFile) throws MalformedURLException, IOException {
		
		URL url = FileLocator.toFileURL(new URL(urlPathToFile));
		String filePath = url.getFile();
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		
		String content = new String(data, "UTF-8");				
		fis.close();
		
		return content;
	}
	
	
	/***
	 * Retrieve the absolute path of the log file
	 * @return String
	 */
	public static String getFileLogLocation() {
		String locInstance = Platform.getInstanceLocation().getURL().getFile() + File.separator + FILE_LOG_NAME;
		return locInstance;
	}
}
