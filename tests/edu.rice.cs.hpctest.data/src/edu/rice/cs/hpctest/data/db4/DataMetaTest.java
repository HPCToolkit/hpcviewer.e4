package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.experiment.metric.HierarchicalMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;

public class DataMetaTest 
{
	static DataMeta data;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		Path resource = Paths.get("..", "resources", "prof2", "loop-inline");
		File dbPath = resource.toFile();
		
		assertNotNull(dbPath);
		assertTrue(dbPath.isDirectory());
		
		IdTupleType tupleType = new IdTupleType();
		tupleType.initDefaultTypes();
		
		data = new DataMeta();
		data.open(dbPath.getAbsolutePath() + File.separatorChar + "meta.db");
		data.finalize(null);
	}
	
	
	@Test
	public void testGetTitle() {
		assertTrue(data.getTitle().equals("loop"));
	}
	
	@Test
	public void testGetDescription() {
		// i don't know the exact length. too lazy to find out.
		assertTrue(data.getDescription().length() > 10);
	}
	
	@Test
	public void testgetKindNames() {
		var kinds = data.getKindNames();
		assertTrue(kinds.length == 8);
		assertTrue(kinds[0].equals("SUMMARY"));
		assertTrue(kinds[7].equals("CORE"));
	}
	
	@Test
	public void testGetMetric() {
		var metrics = data.getMetrics();
		assertNotNull(metrics);
		assertTrue(metrics.size()==2);
		
		HierarchicalMetric m = (HierarchicalMetric) metrics.get(0);
		assertTrue(m.getDisplayName().equals("cycles (E)"));
		assertTrue(m.getIndex() == 1);
		assertTrue(m.getMetricType() == MetricType.EXCLUSIVE);
		assertTrue(m.getCombineTypeLabel().equals("sum"));
		
		m = (HierarchicalMetric) metrics.get(1);
		assertTrue(m.getDisplayName().equals("cycles (I)"));
		assertTrue(m.getIndex() == 2);
		assertTrue(m.getMetricType() == MetricType.INCLUSIVE);
		assertTrue(m.getCombineTypeLabel().equals("sum"));
	}
}
