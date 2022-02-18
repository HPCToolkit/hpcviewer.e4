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
import edu.rice.cs.hpcdata.db.version4.DataSummary.ListCCTAndIndex;
import edu.rice.cs.hpcdata.experiment.metric.MetricValueSparse;

public class DataSummaryTest {

	private static DataSummary data;
	private static File dbPath ;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		Path resource = Paths.get("..", "resources", "prof2", "empty-trace");
		dbPath = resource.toFile();
		
		assertNotNull(dbPath);
		assertTrue(dbPath.isDirectory());
		
		IdTupleType tupleType = new IdTupleType();
		tupleType.initDefaultTypes();
		
		data = new DataSummary(tupleType);
		data.open(dbPath.getAbsolutePath() + File.separatorChar + "profile.db");
	}
	
	
	@Test
	public void testGetCCTIndex() {
		try {
			ListCCTAndIndex list = data.getCCTIndex();
			assertNotNull(list);
			assertNotNull(list.listOfdIndex); // [0..45]
			assertNotNull(list.listOfId);     // [0..74]
			
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetIdTuple() {
		List<IdTuple> list = data.getIdTuple();
		assertNotNull(list);
		assertTrue(list.size() == 1);
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
			double val = data.getMetric(DataSummary.PROFILE_SUMMARY, 0, 512);
			assertTrue(val > 27); // 27.1234130859375
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetMetricsInt() throws IOException {
		List<MetricValueSparse> list = data.getMetrics(0);
		assertTrue(list.size() > 0); // 512: 27.123413
		
		MetricValueSparse mvs = list.get(0);
		assertTrue(mvs.getIndex() == 512);
		assertTrue(mvs.getValue() > 27);
	}	

	@Test
	public void testGetMetricsIntInt() throws IOException {
		List<MetricValueSparse> list = data.getMetrics(0, 1);
		assertTrue(list.size() > 0);
	}

	@Test
	public void testGetStringLabelIdTuples() {
		String []labels = data.getStringLabelIdTuples();
		assertNotNull(labels);
		assertTrue(labels.length == 1);
	}

	@Test
	public void testGetDoubleLableIdTuples() {
		double []labels = data.getDoubleLableIdTuples();
		assertNotNull(labels);
		assertTrue(labels.length == 1); // [0.0]
	}

	@Test
	public void testGetProfileIndexFromOrderIndex() {
		int index = data.getProfileIndexFromOrderIndex(1);
		assertTrue(index == 0);
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
