package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.experiment.metric.MetricValueSparse;
import edu.rice.cs.hpctest.util.TestDatabase;

public class DataSummaryTest {

	private static DataSummary []dataProfiles;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		var dbPath = TestDatabase.getMetaDatabases();
		
		assertNotNull(dbPath);
		assertTrue(dbPath.length>0);
		
		IdTupleType tupleType = new IdTupleType();
		tupleType.initDefaultTypes();
		
		dataProfiles = new DataSummary[dbPath.length];
		int i = 0;
		for(var path: dbPath) {
			dataProfiles[i] = new DataSummary(tupleType);
			dataProfiles[i].open(path.getAbsolutePath());
			i++;
		}
	}
	
	
	@Test
	public void testGetIdTuple() {
		for(var profile: dataProfiles) {
			List<IdTuple> list = profile.getIdTuple();
			assertNotNull(list);
			assertTrue(list.size() >= 1);
			var idt = list.get(0);
			assertTrue(idt.getPhysicalIndex(0) >= 0);
		}
	}

	@Test
	public void testGetIdTupleIdTupleOption() {
		for(var profile: dataProfiles) {
			List<IdTuple> listC = profile.getIdTuple(IdTupleOption.COMPLETE);
			List<IdTuple> listB = profile.getIdTuple(IdTupleOption.BRIEF);
			
			assertEquals(listC.size(), listB.size());
		}
	}

	@Test
	public void testGetMetric() throws IOException {
		for(var profile: dataProfiles) {
			try {
				double val = profile.getMetric(IdTuple.PROFILE_SUMMARY, 0, 2);
				assertTrue(val > 250); 
			} catch (IOException e) {
				fail(e.getMessage());
			}
			
			// test for each id tuples
			List<IdTuple> idtuples = profile.getIdTuple();
			for(IdTuple idt: idtuples) {
				double val = profile.getMetric(idt, 0, 2);
				assertTrue(val >= 0); 
			}
		}
	}

	@Test
	public void testGetMetricsInt() throws IOException {
		for(var profile: dataProfiles) {
			List<MetricValueSparse> list = profile.getMetrics(3);
			assertTrue(list.size() >= 1); // 
			
			MetricValueSparse mvs = list.get(0);
			assertTrue(mvs.getIndex() >= 2);
			assertTrue(mvs.getValue() > 250);
		}
	}	

	@Test
	public void testGetMetricsIntInt() throws IOException {
		for(var profile: dataProfiles) {
			List<MetricValueSparse> list = profile.getMetrics(IdTuple.PROFILE_SUMMARY, 2);
			assertTrue(list.size() > 0);
		}
	}

	@Test
	public void testGetStringLabelIdTuples() {
		for(var profile: dataProfiles) {
			String []labels = profile.getStringLabelIdTuples();
			assertNotNull(labels);
			assertTrue(labels.length >= 1);
			assertTrue(labels[0].startsWith("Node"));
			assertTrue(labels[0].contains("Thread"));
		}
	}

	@Test
	public void testGetDoubleLableIdTuples() {
		for(var profile: dataProfiles) {
			double []labels = profile.getDoubleLableIdTuples();
			assertNotNull(labels);
			assertTrue(labels.length >= 1); // [0.0]
			assertTrue(labels[0] >= 0.0);
		}
	}


	@Test
	public void testHasGPU() {
		for(var profile: dataProfiles) {
			boolean gpu = profile.hasGPU();
			assertFalse(gpu);
		}
	}

	@Test
	public void testGetIdTupleType() {
		for(var profile: dataProfiles) {
			IdTupleType type = profile.getIdTupleType();
			assertNotNull(type); // check content for: {0=Summary, 1=Node, 2=Rank, 3=Thread, 4=GPUDevice, 5=GPUContext, 6=GPUStream, 7=Core}
		}
	}

	@Test
	public void testGetParallelismLevels() {
		for(var profile: dataProfiles) {
			int p = profile.getParallelismLevels();
			assertTrue(p >= 1);
		}
	}

}
