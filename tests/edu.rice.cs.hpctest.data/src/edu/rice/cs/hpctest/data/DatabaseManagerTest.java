package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Test;

import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpctest.util.TestDatabase;

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
		var dirs = TestDatabase.getDatabases();
		for (var dir: dirs) {
			var path = DatabaseManager.getDatabaseFilePath(dir.getAbsolutePath());
			checkPath(path.orElse(""));
			
			Optional<String> t = Optional.of("\n");
			DatabaseManager.getDatabaseFilenames(t);
			
			var parser = DatabaseManager.getDatabaseReader(dir);
			assertNotNull(parser);
		}
	}
	
	private void checkPath(String filepath) {		
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
		assertTrue(filename.orElse("").equals("experiment.xml"));
		
		filename = DatabaseManager.getDatabaseFilename("db");
		assertTrue(filename.isPresent());
		assertTrue(filename.orElse("").equals("meta.db"));
	}
}
