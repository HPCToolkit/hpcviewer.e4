package edu.rice.cs.hpcdata.db;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/******
 * 
 * Class to manage different types and versions of database.
 *
 */
public class DatabaseManager 
{

	private static final String []DATABASE_FILENAME = { "experiment.xml", "meta.db" };
	
	/***
	 * Check if a file is a database file.
	 * @param filename
	 * @return boolean true if it's a recognized database file
	 */
	public static boolean isDatabaseFile(String filename) {
		for (String f: DATABASE_FILENAME) {
			if (f.equals(filename))
				return true;
		}
		return false;
	}

	public static String getDatabaseFilenames(Optional<String> separator) {
		String sep  = separator.orElse("\n");
		String name = DATABASE_FILENAME[0] + sep + DATABASE_FILENAME[1];
		
		return name;
	}
	
	public static Optional<String> getDatabaseFilePath(String directory) {
		
		for (String dbFile : DatabaseManager.DATABASE_FILENAME) {
			var file = Paths.get(directory, dbFile);
			if (Files.isReadable(file)) {
				return Optional.of(file.toAbsolutePath().toString());
			}						
		}
		return Optional.empty();
	}
	
	public static Optional<String> getDatabaseFilename(String extension) {
		for(String df: DATABASE_FILENAME) {
			if (df.endsWith(extension))
				return Optional.of(df);
		}
		return Optional.empty();
	}
}
