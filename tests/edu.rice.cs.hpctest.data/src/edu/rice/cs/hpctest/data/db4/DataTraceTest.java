package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataRecord;
import edu.rice.cs.hpcdata.db.version4.DataTrace;

public class DataTraceTest {

	private static DataTrace data;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		Path resource = Paths.get("..", "resources", "prof2", "loop-inline");
		File dbPath = resource.toFile();
		
		assertNotNull(dbPath);
		assertTrue(dbPath.isDirectory());
		
		IdTupleType tupleType = new IdTupleType();
		tupleType.initDefaultTypes();
		
		data = new DataTrace();
		data.open(dbPath.getAbsolutePath());
	}



	@Test
	public void testGetSampledData() throws IOException {
		DataRecord old = null;
		int samples = data.getNumberOfSamples(0);
		for (int i=0; i<Math.min(samples, 10); i++) {
			DataRecord rec = data.getSampledData(0, i);
			assertNotNull(rec);
			assertTrue(rec.timestamp >= 1642398106543405000L &&
					   rec.timestamp <= 1642398109415400000L);
			assertTrue(rec.cpId > 0 && rec.cpId <= 69);
			
			if (old != null)
				assertTrue(rec.timestamp > old.timestamp);
			old = new DataRecord(rec.timestamp, rec.cpId, 0);
		}
	}

	@Test
	public void testGetNumberOfSamples() {
		int samples = data.getNumberOfSamples(0);
		assertTrue(samples == 838);
	}

	@Test
	public void testGetNumberOfRanks() {
		int ranks = data.getNumberOfRanks();
		assertTrue(ranks == 1);
	}

	@Test
	public void testGetLength() {
		long l = data.getLength(0);
		assertTrue(l == 10056);
	}

}
