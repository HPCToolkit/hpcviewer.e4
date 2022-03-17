package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.experiment.metric.MetricValueSparse;

public class DataSummaryTest {

	private static DataSummary data;
	private static File dbPath ;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		Path resource = Paths.get("..", "resources", "prof2", "loop-inline");
		dbPath = resource.toFile();
		
		assertNotNull(dbPath);
		assertTrue(dbPath.isDirectory());
		
		IdTupleType tupleType = new IdTupleType();
		tupleType.initDefaultTypes();
		
		data = new DataSummary(tupleType);
		data.open(dbPath.getAbsolutePath());
	}
	
	
	@Test
	public void testGetIdTuple() {
		List<IdTuple> list = data.getIdTuple();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		var idt = list.get(0);
		assertTrue(idt.getPhysicalIndex(0) == 0x7f0101);
	}

	@Test
	public void testGetIdTupleIdTupleOption() {
		List<IdTuple> listC = data.getIdTuple(IdTupleOption.COMPLETE);
		List<IdTuple> listB = data.getIdTuple(IdTupleOption.BRIEF);
		
		assertTrue(listC.size() == listB.size());
	}

	@Test
	public void testGetMetric() {
		try {
			double val = data.getMetric(0, 0, 2);
			assertTrue(val > 1.25E10); 
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetMetricsInt() throws IOException {
		List<MetricValueSparse> list = data.getMetrics(3);
		assertTrue(list.size() == 2); // 
		
		MetricValueSparse mvs = list.get(1);
		assertTrue(mvs.getIndex() == 2);
		assertTrue(mvs.getValue() > 1.25281e+10);
	}	

	@Test
	public void testGetMetricsIntInt() throws IOException {
		List<MetricValueSparse> list = data.getMetrics(0, 2);
		assertTrue(list.size() > 0);
	}

	@Test
	public void testGetStringLabelIdTuples() {
		String []labels = data.getStringLabelIdTuples();
		assertNotNull(labels);
		assertTrue(labels.length == 1);
		assertTrue(labels[0].startsWith("Node"));
		assertTrue(labels[0].contains("Thread"));
	}

	@Test
	public void testGetDoubleLableIdTuples() {
		double []labels = data.getDoubleLableIdTuples();
		assertNotNull(labels);
		assertTrue(labels.length == 1); // [0.0]
		assertTrue(labels[0] == 0.0);
	}


	@Test
	public void testHasGPU() {
		boolean gpu = data.hasGPU();
		assertFalse(gpu);
	}

	@Test
	public void testGetIdTupleType() {
		IdTupleType type = data.getIdTupleType();
		assertNotNull(type); // check content for: {0=Summary, 1=Node, 2=Rank, 3=Thread, 4=GPUDevice, 5=GPUContext, 6=GPUStream, 7=Core}
	}

	@Test
	public void testGetParallelismLevels() {
		int p = data.getParallelismLevels();
		assertTrue(p == 2);
	}

}
