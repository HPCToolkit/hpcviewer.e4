package edu.rice.cs.hpcdata.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Test;

import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.experiment.xml.ExperimentFileXML;

public class DatabaseManagerTest {

	@Test
	public void testIsDatabaseFile() {
		assertFalse(DatabaseManager.isDatabaseFile(null));
		assertFalse(DatabaseManager.isDatabaseFile("file.xml"));
		assertFalse(DatabaseManager.isDatabaseFile("UnknownExperiment.xml"));
		assertTrue(DatabaseManager.isDatabaseFile("experiment.xml"));
		assertTrue(DatabaseManager.isDatabaseFile("meta.db"));
	}

	@Test
	public void testGetDatabaseFilenames() {
		var names = DatabaseManager.getDatabaseFilenames(Optional.empty());
		assertNotNull(names);
		assertTrue(names.length()>1);
	}

	@Test
	public void testGetDatabaseFilePath() {
		var path = DatabaseManager.getDatabaseFilePath("");
		assertTrue(path.isEmpty());
		
		Path resource = Paths.get("..", "resources", "bug-no-gpu-trace");
		var database = resource.toFile();
		path = DatabaseManager.getDatabaseFilePath(database.getAbsolutePath());
		assertTrue(path.isPresent());
		
		String filepath = path.get();
		Path p = Paths.get(filepath);
		assertNotNull(p);
		var f = p.toFile();
		assertNotNull(f);
		assertTrue(f.canRead());
	}

	@Test
	public void testGetDatabaseFilename() {
		var filename = DatabaseManager.getDatabaseFilename("xml");
		assertTrue(filename.isPresent());
		assertTrue(filename.get().equals("experiment.xml"));
		
		filename = DatabaseManager.getDatabaseFilename("db");
		assertTrue(filename.isPresent());
		assertTrue(filename.get().equals("meta.db"));
	}

	
	@Test
	public void testGetDatabaseReader() {
		Path resource = Paths.get("..", "resources", "bug-no-gpu-trace");
		var database = resource.toFile();

		var parser = DatabaseManager.getDatabaseReader(database);
		assertNotNull(parser);
		assertTrue(parser instanceof ExperimentFileXML);
	}
}
