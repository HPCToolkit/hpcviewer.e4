package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataPlot;
import edu.rice.cs.hpcdata.db.version4.DataPlotEntry;


public class DataPlotTest {

	private static DataPlot data;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		Path resource = Paths.get("..", "resources", "prof2", "loop-inline");
		File dbPath = resource.toFile();
		
		assertNotNull(dbPath);
		assertTrue(dbPath.isDirectory());
		
		IdTupleType tupleType = new IdTupleType();
		tupleType.initDefaultTypes();
		
		data = new DataPlot();
		data.open(dbPath.getAbsolutePath());
	}



	@Test
	public void testGetPlotEntryIntInt() throws IOException {
		DataPlotEntry []dpe = data.getPlotEntry(0, 0);
		assertNull(dpe);
		
		dpe = data.getPlotEntry(0, 2);
		assertNotNull(dpe);
		assertTrue(dpe.length == 1);
		assertTrue(dpe[0].tid == 1);
		assertTrue(dpe[0].metval >= 1.25E10);
		
		dpe = data.getPlotEntry(6, 0);
		assertTrue(dpe.length == 1 && dpe[0].tid == 1);
		assertTrue(dpe[0].metval >= 5.86e+07);
		
		dpe = data.getPlotEntry(6, 1);
		assertTrue(dpe.length == 1 && dpe[0].tid == 1);
		assertTrue(dpe[0].metval >= 5.86e+07);
		
		dpe = data.getPlotEntry(68, 2);
		assertTrue(dpe.length == 1 && dpe[0].tid == 1);
		assertTrue(dpe[0].metval == 2);
		
		dpe = data.getPlotEntry(69, 0);
		assertTrue(dpe == null);
	}
}
