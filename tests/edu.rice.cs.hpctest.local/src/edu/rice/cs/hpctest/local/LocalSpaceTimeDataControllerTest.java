package edu.rice.cs.hpctest.local;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpctest.util.TestDatabase;

public class LocalSpaceTimeDataControllerTest 
{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var directories = TestDatabase.getDatabases();
		for(var dir: directories) {
			Experiment e = new Experiment();
			var localDb = new LocalDatabaseRepresentation(dir, null, true);
			e.open(localDb);
			
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetName() {
		fail("Not yet implemented");
	}

	@Test
	public void testSpaceTimeDataControllerLocal() {
		fail("Not yet implemented");
	}

	@Test
	public void testSpaceTimeDataController() {
		fail("Not yet implemented");
	}

}
