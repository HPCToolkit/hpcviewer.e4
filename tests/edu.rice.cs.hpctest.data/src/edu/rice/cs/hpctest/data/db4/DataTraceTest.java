package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataRecord;
import edu.rice.cs.hpcdata.db.version4.DataTrace;
import edu.rice.cs.hpctest.util.TestDatabase;

public class DataTraceTest {

	private static DataTrace []data;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		final var dbPaths = TestDatabase.getMetaDatabases();
		data = new DataTrace[dbPaths.length];
		
		for(int i=0; i<dbPaths.length; i++) {
			File dbPath = dbPaths[i];
			
			assertNotNull(dbPath);
			assertTrue(dbPath.isDirectory());
			
			IdTupleType tupleType = new IdTupleType();
			tupleType.initDefaultTypes();
			
			data[i] = new DataTrace();
			data[i].open(dbPath.getAbsolutePath());
		}
	}


	@Test
	public void testGetSampledData() throws IOException {
		for(int j=0; j<data.length; j++) {
			DataTrace d = data[j];
			DataRecord old = null;
			int samples = d.getNumberOfSamples(0);
			
			for (int i=0; i<Math.min(samples, 10); i++) {
				DataRecord rec = d.getSampledData(0, i);
				assertNotNull(rec);
				assertTrue(rec.timestamp >= 100 &&
						   rec.timestamp <= Long.MAX_VALUE);
				assertTrue(rec.cpId >= 0 && rec.cpId <= Integer.MAX_VALUE);
				
				if (old != null)
					assertTrue(rec.timestamp > old.timestamp);
				
				old = new DataRecord(rec.timestamp, rec.cpId, 0);
			}
		}
	}
	

	@Test
	public void testGetNumberOfSamples() {
		for(int j=0; j<data.length; j++) {
			var d = data[j];
			int samples = d.getNumberOfSamples(0);
			assertTrue(samples >= 0);
		}
	}

	@Test
	public void testGetNumberOfRanks() {
		for (int i=0; i<data.length; i++) {
			int ranks = data[i].getNumberOfRanks();
			assertTrue(ranks >= 0);
		}
	}

	@Test
	public void testGetLength() {
		for (int i=0; i<data.length; i++) {
			long l = data[i].getLength(0);
			assertTrue(l >= 0);
			
			l = data[i].getOffset(0);
			assertTrue(l >= 0);
			
			int rs = data[i].getRecordSize();
			assertTrue(rs >= Long.BYTES + Integer.BYTES);
		}
	}
	
	
	@Test
	public void testMinMax() {
		for (var d: data) {
			var max = d.getMaxTime();
			var min = d.getMinTime();
			
			if (min < Long.MAX_VALUE) {
				assertTrue(max > 0 && min > 0);
				assertTrue(min < max);
			}
		}
	}

}
