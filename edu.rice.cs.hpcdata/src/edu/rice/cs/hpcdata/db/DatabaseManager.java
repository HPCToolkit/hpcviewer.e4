package edu.rice.cs.hpcdata.db;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import edu.rice.cs.hpcdata.db.version4.MetaDbFileParser;
import edu.rice.cs.hpcdata.experiment.ExperimentFile;
import edu.rice.cs.hpcdata.experiment.xml.ExperimentFileXML;

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
	 * @param filename The file name without the absolute path
	 * @return boolean true if it's a recognized database file
	 */
	public static boolean isDatabaseFile(String filename) {
		for (String f: DATABASE_FILENAME) {
			if (f.equals(filename))
				return true;
		}
		return false;
	}

	
	/****
	 * return the list of supported database files.
	 * 
	 * @param separator separator between filenames. Default is "\n"
	 * @return
	 */
	public static String getDatabaseFilenames(Optional<String> separator) {
		String sep  = separator.orElse("\n");
		String name = DATABASE_FILENAME[0] + sep + DATABASE_FILENAME[1];
		
		return name;
	}
	
	
	/***
	 * Return the absolute path of the database file given its directory.
	 * If the directory contains experiment.xml, it will return the 
	 * directory + path_separator + experiment.xml if the file exists. 
	 * 
	 * @param directory
	 * @return optionally the absolute path if the file exists.
	 */
	public static Optional<String> getDatabaseFilePath(String directory) {
		
		for (String dbFile : DatabaseManager.DATABASE_FILENAME) {
			var file = Paths.get(directory, dbFile);
			if (Files.isReadable(file)) {
				return Optional.of(file.toAbsolutePath().toString());
			}						
		}
		return Optional.empty();
	}
	
	
	/****
	 * Return the database filename given its extension.
	 * <br/>
	 * If extension == "xml", it returns "experiment.xml"
	 * 
	 * @param extension
	 * @return
	 */
	public static Optional<String> getDatabaseFilename(String extension) {
		for(String df: DATABASE_FILENAME) {
			if (df.endsWith(extension))
				return Optional.of(df);
		}
		return Optional.empty();
	}
	
	
	/*****
	 * Retrieve the parser for the database given its database filename.
	 * 
	 * @param filename
	 * @return
	 */
	public static ExperimentFile getDatabaseReader(String filename) {
		if (filename.endsWith(DATABASE_FILENAME[0])) {
			return new ExperimentFileXML();
		} else if (filename.endsWith(DATABASE_FILENAME[1])) {
			return new MetaDbFileParser();
		}
		return null;
	}
}
