package edu.rice.cs.hpctest.local;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcbase.IDatabase.DatabaseStatus;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpclocal.DatabaseLocal;
import edu.rice.cs.hpctest.util.TestDatabase;

public class DatabaseLocalTest 
{
	
	private static DatabaseLocal []databases;

	@BeforeClass
	public static void setUpBeforeClass() {
		File[] directories = TestDatabase.getDatabases();
		databases = new DatabaseLocal[directories.length];
		
		int i=0;
		
		for(var dir: directories) {
			databases[i] = new DatabaseLocal();
			
			var status = databases[i].setDirectory(null);
			assertEquals(DatabaseStatus.INVALID, status);
			
			var msg = databases[i].getErrorMessage();
			assertNotNull(msg);
			
			status = databases[i].setDirectory(dir.getAbsolutePath());
			
			assertEquals(DatabaseStatus.OK, status);
			assertNull(databases[i].getErrorMessage());
			
			var lastStatus = databases[i].getStatus();
			assertEquals(status, lastStatus);
			
			i++;
		}
	}

	@AfterClass
	public static void tearDownAfterClass() {
		for(var db: databases) {
			db.close();
		}
	}

	@Test
	public void testOpen() throws InvalExperimentException, IOException {
		for(var db: databases) {
			var dir = db.getDirectory();
			assertNotNull(dir);
			
			String id = db.getId();
			assertNotNull(id);
			assertFalse(id.isEmpty());
			assertFalse(id.isBlank());
			
			var exp = db.getExperimentObject();
			assertNotNull(exp);
			
			var hasTrace = db.hasTraceData();
			// assume all has traces
			assertTrue(hasTrace);
			
			var traceManager = db.getORCreateTraceManager();
			assertNotNull(traceManager);
		}
	}
}
