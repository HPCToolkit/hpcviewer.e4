package edu.rice.cs.hpctest.viewer.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.db.version4.FileDB4;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpctest.util.TestDatabase;

public class DataTraceReaderTest {
	static FileDB4 []dataDB;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		var paths = TestDatabase.getMetaDatabases();
		dataDB = new FileDB4[paths.length];
		int i=0;
		for(var dbPath: paths) {			
			assertNotNull(dbPath);
			assertTrue(dbPath.canRead());
			
			IdTupleType tupleType = new IdTupleType();
			tupleType.initDefaultTypes();
			
			DataSummary ds = new DataSummary(IdTupleType.createTypeWithOldFormat());
			ds.open(dbPath.getAbsolutePath());
			
			dataDB[i] = new FileDB4(new Experiment(), ds);
			dataDB[i].open(dbPath.getAbsolutePath(), 0, 0);
			i++;
		}
	}

	@Test
	public void testNumberOfRanks() {
		for(var data: dataDB) {
			int numRanks = data.getNumberOfRanks();
			assertTrue(numRanks >= 1);
		}
	}
	
	@Test
	public void testgetRankLabels() {
		for(var data: dataDB) {
			var labels = data.getRankLabels();
			assertTrue(labels != null);
		}
	}
	
	@Test
	public void testIsGPU() {
		for(var data: dataDB) {
			var types = data.getIdTupleTypes();
			var idTuples = data.getIdTuple(IdTupleOption.COMPLETE);
			var hasgpu   = idTuples.stream().anyMatch(idt -> idt.isGPU(types));
			
			assertEquals(data.hasGPU(), hasgpu);
			
			for(int i=0; i<idTuples.size(); i++) {
				assertFalse(data.isGPU(i) != idTuples.get(i).isGPU(types));
			}
		}
	}

	@Test
	public void testMinLoc() {
		for(var data: dataDB) {
			var ml = data.getMinLoc(0);
			assertTrue(ml >= 88);
		}
	}
	
	@Test
	public void testMaxLoc() {
		for(var data: dataDB) {
			var ml = data.getMaxLoc(0);
			assertTrue(ml >= 64);
		}
	}
	
	@Test
	public void testIdTuple() {
		for(var data: dataDB) {
			var idt = data.getIdTuple(IdTupleOption.BRIEF);
			assertNotNull(idt);
			assertTrue(!idt.isEmpty());
			
			var id = idt.get(0);
			var label = id.toLabel();
			assertNotNull(label);
			
			assertTrue(id.getLength() >= 1);
			var str = id.toString(data.getIdTupleTypes());
			assertNotNull(str);
			
			var types = data.getIdTupleTypes();
			assertNotNull(types);
		}
	}
}
