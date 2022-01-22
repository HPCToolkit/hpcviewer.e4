package edu.rice.cs.hpcdata.test.db4;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataPlot;
import edu.rice.cs.hpcdata.db.version4.DataPlotEntry;


public class DataPlotTest {

	private static DataPlot data;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Path resource = Paths.get("..", "resources", "prof2");
		File dbPath = resource.toFile();
		
		assertNotNull(dbPath);
		assertTrue(dbPath.isDirectory());
		
		IdTupleType tupleType = new IdTupleType();
		tupleType.initDefaultTypes();
		
		data = new DataPlot();
		data.open(dbPath.getAbsolutePath() + File.separatorChar + "cct.db");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}


	@Test
	public void testGetPlotEntryIntInt() throws IOException {
		DataPlotEntry []dpe = data.getPlotEntry(0, 0);
		assertNotNull(dpe);
		assertTrue(dpe.length > 0);
	}
}
