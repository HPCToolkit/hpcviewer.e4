package edu.rice.cs.hpctest.viewer.trace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpctraceviewer.data.version4.FileDB4;

class DataTraceReaderTest {
	static FileDB4 data;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Path resource = Paths.get("..", "resources", "prof2", "loop-inline");
		File dbPath = resource.toFile();
		
		assertNotNull(dbPath);
		assertTrue(dbPath.canRead());
		
		IdTupleType tupleType = new IdTupleType();
		tupleType.initDefaultTypes();
		
		DataSummary ds = new DataSummary(IdTupleType.createTypeWithOldFormat());
		ds.open(dbPath.getAbsolutePath());
		
		data = new FileDB4(ds);
		data.open(dbPath.getAbsolutePath(), 0, 0);
	}

	@Test
	public void testNumberOfRanks() {
		int numRanks = data.getNumberOfRanks();
		assertTrue(numRanks == 1);
	}
	
	@Test
	public void testgetRankLabels() {
		var labels = data.getRankLabels();
		assertTrue(labels != null && labels.length == 1 && labels[0].equals("Node 8323329 Thread 0"));		
	}
	
	@Test
	public void testIsGPU() {
		assertFalse(data.hasGPU());
		assertFalse(data.isGPU(0));
	}

	@Test
	public void testMinLoc() {
		var ml = data.getMinLoc(0);
		assertTrue(ml == 104);
	}
	
	@Test
	public void testMaxLoc() {
		var ml = data.getMaxLoc(0);
		assertTrue(ml == 10160);
	}
	
	@Test
	public void testIdTuple() {
		var idt = data.getIdTuple(IdTupleOption.BRIEF);
		assertNotNull(idt);
		assertTrue(idt.size() == 1);
		
		var id = idt.get(0);
		var label = id.toLabel();
		assertNotNull(label);
		assertTrue(label.equals("0.0"));
		
		assertTrue(id.getLength() == 2);
		var str = id.toString(data.getIdTupleTypes());
		assertNotNull(str);
		assertTrue(str.equals("Node 8323329 Thread 0"));
		
		var types = data.getIdTupleTypes();
		assertNotNull(types);
	}
}
