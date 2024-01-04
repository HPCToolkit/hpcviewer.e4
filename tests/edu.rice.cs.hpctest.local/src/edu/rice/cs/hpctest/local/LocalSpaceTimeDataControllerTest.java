package edu.rice.cs.hpctest.local;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpclocal.LocalDBOpener;
import edu.rice.cs.hpclocal.SpaceTimeDataControllerLocal;
import edu.rice.cs.hpctest.util.TestDatabase;


public class LocalSpaceTimeDataControllerTest 
{
	static List<SpaceTimeDataControllerLocal> list;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		list = new ArrayList<>();
		
		var directories = TestDatabase.getDatabases();
		
		for(var dir: directories) {
			Experiment e = new Experiment();
			
			var localDb = new LocalDatabaseRepresentation(dir, null, IProgressReport.dummy());
			e.open(localDb);
			var opener = new LocalDBOpener(e);
			
			if (e.getTraceDataVersion() < 0)
				continue;

			var stdc = opener.openDBAndCreateSTDC(null);
			
			list.add((SpaceTimeDataControllerLocal) stdc);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		for(var controller: list) {
			controller.closeDB();
			controller.dispose();
		}
	}

	@Test
	public void testGetName() {
		for(var controller: list) {
			var name = controller.getName();
			assertNotNull(name);
			assertFalse(name.isEmpty());
		}
	}

	@Test
	public void testSpaceTimeDataControllerLocal() {
		for(var controller: list) {
			var baseData = controller.getBaseData();
			assertNotNull(baseData);
			
			var color = controller.getColorTable();
			assertNotNull(color);
			
			var trace = controller.getCurrentSelectedTraceline();
			assertNull(trace);
		}
	}
}
