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
		final long []minTimeStamp = new long[] {1405000L, 153000L};
		final long []maxTimeStamp = new long[] {6642398109415400000L, 6650425285586293000L};
		final int  []maxCpid = new int[] {699999, 42399999};
		for(int j=0; j<data.length; j++) {
			DataTrace d = data[j];
			DataRecord old = null;
			int samples = d.getNumberOfSamples(0);
			
			for (int i=0; i<Math.min(samples, 10); i++) {
				DataRecord rec = d.getSampledData(0, i);
				assertNotNull(rec);
				assertTrue(rec.timestamp >= minTimeStamp[j] &&
						   rec.timestamp <= maxTimeStamp[j]);
				assertTrue(rec.cpId >= 0 && rec.cpId <= maxCpid[j]);
				
				if (old != null)
					assertTrue(rec.timestamp > old.timestamp);
				
				old = new DataRecord(rec.timestamp, rec.cpId, 0);
			}
		}
	}
	

	@Test
	public void testGetNumberOfSamples() {
		int []numSample = new int[] {90, 442}; // 
		for(int j=0; j<data.length; j++) {
			var d = data[j];
			int samples = d.getNumberOfSamples(0);
			assertTrue(samples >= numSample[j]);
		}
	}

	@Test
	public void testGetNumberOfRanks() {
		final int []numRanks = new int[] {1, 3};
		for (int i=0; i<data.length; i++) {
			int ranks = data[i].getNumberOfRanks();
			assertTrue(ranks >= numRanks[i]);
		}
	}

	@Test
	public void testGetLength() {
		final int []lengths = new int[] {106, 304}; // {6732,  }
		for (int i=0; i<data.length; i++) {
			long l = data[i].getLength(0);
			assertTrue(l >= lengths[i]);
		}
	}

}
