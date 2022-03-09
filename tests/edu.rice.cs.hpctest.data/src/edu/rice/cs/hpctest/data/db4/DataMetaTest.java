package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.HierarchicalMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;

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
		
		assertThrows(RuntimeException.class, ()->{
			data.open(dbPath.getAbsolutePath());
		});
		data.open(new Experiment(), dbPath.getAbsolutePath());
		data.finalize(null);
	}
	
	
	@Test
	public void testGetExperiment() {
		var experiment = data.getExperiment();
		assertNotNull(experiment);
		
		assertTrue(experiment.getName().equals("loop"));
		assertTrue(experiment.getMaxDepth() > 10);
		assertTrue(experiment.getMajorVersion() == 4);
	}
	
	@Test
	public void testGetDescription() {
		// i don't know the exact length. too lazy to find out.
		assertTrue(data.getDescription().length() > 10);
	}
	
	@Test
	public void testgetKindNames() {
		var kinds = data.getExperiment().getIdTupleType();
		assertNotNull(kinds);
		
		var entry = kinds.entrySet();
		assertNotNull(entry);
		
		entry.forEach(kind -> {
			assertNotNull(kind.getKey());
			assertFalse(kind.getValue().isEmpty());
		});
	}
	
	@Test
	public void testGetMetric() {
		var metrics = data.getExperiment().getMetrics();
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
	
	@Test
	public void testLoadModule() {
		assertTrue(data.getNumLoadModules() == 5);
		var iterator = data.getLoadModuleIterator();
		iterator.forEachRemaining(lm -> {
			assertNotNull(lm);
			assertNotNull(lm.getName());
		});
	}
	
	
	@Test
	public void testFiles() {
		assertTrue(data.getNumFiles() == 5);
		var iterator = data.getFileIterator();
		iterator.forEachRemaining(f-> {
			assertNotNull(f);
			assertNotNull(f.getName());
		});
	}
	
	@Test
	public void testProcedure() {
		assertTrue(data.getNumProcedures() == 10);
		
		Iterator<ProcedureScope> iterator = data.getProcedureIterator();
		assertNotNull(iterator);
		iterator.forEachRemaining(ps -> {
			assertNotNull(ps);
			assertNotNull(ps.getName());
		});
	}
	
	
	@Test
	public void testRoot() {
		var root = data.getExperiment().getRootScope();
		
		assertNotNull(root);
		assertNotNull(root.getName());
		
		var children = root.getChildren();
		assertNotNull(children);
		for(var child: children) {
			assertNotNull(child.getName());
			assertNotNull(child.getExperiment());
		}
	}
}
