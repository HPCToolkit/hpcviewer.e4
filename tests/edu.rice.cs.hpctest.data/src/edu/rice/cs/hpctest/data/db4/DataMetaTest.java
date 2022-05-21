package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.HierarchicalMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctest.util.TestDatabase;

public class DataMetaTest 
{
	static DataMeta []dataMeta;
	private static final int MAX_DEPTH = 6;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		var paths = TestDatabase.getMetaDatabases();
		dataMeta  = new DataMeta[paths.length];
		
		int i=0;
		
		for(var dbPath: paths) {			
			assertNotNull(dbPath);
			assertTrue(dbPath.isDirectory());
			
			IdTupleType tupleType = new IdTupleType();
			tupleType.initDefaultTypes();
			
			final DataMeta dm = new DataMeta();
			
			assertThrows(RuntimeException.class, ()->{
				dm.open(dbPath.getAbsolutePath());
			});
			dataMeta[i] = dm;
			dataMeta[i].open(new Experiment(), dbPath.getAbsolutePath());
			
			i++;
		}
	}
	
	
	@Test
	public void testGetExperiment() {
		for(DataMeta data: dataMeta) {
			var experiment = data.getExperiment();
			assertNotNull(experiment);
			
			assertTrue(experiment.getName().length()>1);
			assertTrue(experiment.getMaxDepth() > MAX_DEPTH);
			assertTrue(experiment.getMajorVersion() == 4);
		}
	}
	
	
	@Test
	public void testgetKindNames() {
		for(DataMeta data: dataMeta) {
			var kinds = data.getExperiment().getIdTupleType();
			assertNotNull(kinds);
			
			var entry = kinds.entrySet();
			assertNotNull(entry);
			
			entry.forEach(kind -> {
				assertNotNull(kind.getKey());
				assertFalse(kind.getValue().isEmpty());
			});
		}
	}
	
	@Test
	public void testGetMetric() {
		for(DataMeta data: dataMeta) {
			var metrics = data.getExperiment().getMetrics();
			assertNotNull(metrics);
			assertTrue(metrics.size()>=3);
			
			HierarchicalMetric m = (HierarchicalMetric) metrics.get(1);
			assertTrue(m.getDisplayName().length()>1);
			assertTrue(m.getIndex() >= 0);
			assertTrue(m.getMetricType() == MetricType.EXCLUSIVE);
			assertTrue(m.getCombineTypeLabel().equals("sum"));
			
			m = (HierarchicalMetric) metrics.get(2);
			assertTrue(m.getDisplayName().length()>1);
			assertTrue(m.getIndex() >= 1);
			assertTrue(m.getMetricType() == MetricType.INCLUSIVE);
			assertTrue(m.getCombineTypeLabel().equals("sum"));
		}
	}
	
	@Test
	public void testLoadModule() {
		for(DataMeta data: dataMeta) {
			assertTrue(data.getNumLoadModules() >= 3);
			var iterator = data.getLoadModuleIterator();
			iterator.forEachRemaining(lm -> {
				assertNotNull(lm);
				assertNotNull(lm.getName());
			});
		}
	}
	
	
	@Test
	public void testFiles() {
		for(DataMeta data: dataMeta) {
			assertTrue(data.getNumFiles() >= 3);
			var iterator = data.getFileIterator();
			iterator.forEachRemaining(f-> {
				assertNotNull(f);
				assertNotNull(f.getName());
			});
		}
	}
	
	@Test
	public void testProcedure() {
		for(DataMeta data: dataMeta) {
			assertTrue(data.getNumProcedures() >= 9);
			
			Iterator<ProcedureScope> iterator = data.getProcedureIterator();
			assertNotNull(iterator);
			iterator.forEachRemaining(ps -> {
				assertNotNull(ps);
				assertNotNull(ps.getName());
			});
		}
	}
	
	
	@Test
	public void testRoot() {
		for(DataMeta data: dataMeta) {
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
	
	
	@Test
	public void testDepth() {
		for(DataMeta data: dataMeta) {
			var exp   = data.getExperiment();
			var depth = exp.getMaxDepth();
	
			assertTrue(depth > MAX_DEPTH);
			
			var root = ((Experiment)exp).getRootScope(RootScopeType.CallingContextTree);
			Scope s = root.getSubscope(0).getSubscope(0);
			Scope p = s.getParentScope().getParentScope();
			assertTrue(root == p);
		}
	}		
}
