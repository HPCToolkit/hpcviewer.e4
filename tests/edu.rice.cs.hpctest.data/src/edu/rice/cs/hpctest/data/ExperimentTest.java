package edu.rice.cs.hpctest.data;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.visitors.TraceScopeVisitor;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpctest.util.TestDatabase;

import edu.rice.cs.hpctest.data.util.TestMetricValue;

public class ExperimentTest 
{
	private static final String DB_MULTITHREAD = "multithread";
	private static Experiment []experiments;
	
	public ExperimentTest() {
		// empty, nothing to do
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var database = TestDatabase.getDatabases();
		experiments  = new Experiment[database.length];

		int i=0;

		for (var dbp: database) {
			experiments[i]= new Experiment();
			try {
				experiments[i].open(dbp, null, Experiment.ExperimentOpenFlag.TREE_ALL);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}
			
			assertNotNull(experiments[i].getRootScope());
			
			var experiment = experiments[i];
			var rootCCT = ((BaseExperiment)experiment).getRootScope(RootScopeType.CallingContextTree);
			
			// needs to gather info about cct id and its depth
			// this is needed for traces
			TraceScopeVisitor visitor = new TraceScopeVisitor();
			rootCCT.dfsVisitScopeTree(visitor);
			
			experiment.setMaxDepth(visitor.getMaxDepth());
			experiment.setScopeMap(visitor.getCallPath());

			var attributes = experiment.getTraceAttribute();
			attributes.maxDepth  = experiment.getMaxDepth();
			
			experiment.setTraceAttribute(attributes);

			testGetRootScope(experiments[i]);
			
			i++;
		}		
	}


	@Test
	public void testIsMergedDatabase() {
		for(var exp: experiments) {
			assertFalse(exp.isMergedDatabase());
		}
	}


	@Test
	public void testGetRawMetrics() {
		for(var exp: experiments) {
			List<BaseMetric> metrics = exp.getRawMetrics();
			if (exp.getMajorVersion()<4) {
				if (metrics != null) {
					assertTrue(metrics.size() <= exp.getMetricCount());
				}
			} else {
				assertNotNull(metrics);
				assertTrue(metrics.size()>1);
			}				
		}
	}

	@Test
	public void testGetVisibleMetrics() {
		int []num = new int[] {97, 2, 0, 3, 10, 2, 2, 4, 10, 2};
		int i = 0;
		for(var experiment: experiments) {
			List<BaseMetric> metrics = experiment.getVisibleMetrics();
			assertNotNull(metrics);
			assertTrue(metrics.size() >= num[i]);
			i++;
		}
	}

	@Test
	public void testGetNonEmptyMetricIDs() {
		final int []nmetrics = new int[] {18, 0, 0, 4, 1, 1, 2, 2, 4, 2};
		int i=0;
		for(var experiment: experiments) {
			RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
			List<Integer> metrics = experiment.getNonEmptyMetricIDs(root);
			assertNotNull(metrics);
			assertTrue(metrics.size() >= nmetrics[i]);
			i++;
		}
	}

	@Test
	public void testGetMetricCount() {
		int []counts = new int[] {10, 0, 0, 3, 10, 2, 2, 3, 10, 2};
		int i=0;
		
		for(var experiment: experiments) {
			assertTrue(experiment.getMetricCount() >= counts[i]);
			i++;
		}
	}

	@Test
	public void testGetMetricInt() {
		for(var experiment: experiments) {
			List<BaseMetric> list = experiment.getVisibleMetrics();
			if (list != null && list.size()>0) {
				BaseMetric metric = experiment.getMetric(list.get(0).getIndex());
				assertNotNull(metric);
				assertEquals(list.get(0), metric);
			}
		}
	}

	@Test
	public void testGetMetricString() {
		for(var experiment: experiments) {
			List<BaseMetric> list = experiment.getVisibleMetrics();
			if (list != null && list.size()>0) {
				BaseMetric metric = experiment.getMetric(list.get(0).getShortName());
				assertNotNull(metric);
			}
		}
	}

	@Test
	public void testGetMetricFromOrder() {
		int []order = new int[] {0, 0, 1, 1, 1, 1, 1, 31, 1, 1};
		int i = 0;
		for(var experiment: experiments) {
			if (experiment.getMetricCount()>0) {
				BaseMetric metric = experiment.getMetricFromOrder(order[i]);
				assertNotNull(metric);
				i++;
			}
		}
	}

	@Test
	public void testAddDerivedMetric() {
		for(var experiment: experiments) {
			if (experiment.getMetricCount()==0) 
				continue;
			
			RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
			BaseMetric metric = experiment.getMetricList().get(0);
			int numMetrics = experiment.getMetricCount();
			int index = getUnusedMetricIndex(experiment);
			String ID = String.valueOf(index);
			
			DerivedMetric dm = new DerivedMetric(root, 
										experiment, 
										"$" + metric.getIndex(), 
										"DM " + metric.getDisplayName(), 
										ID, 
										index, AnnotationType.NONE, metric.getMetricType());
			experiment.addDerivedMetric(dm);
			
			assertEquals(experiment.getMetricCount(), numMetrics + 1L);
		}
	}
	
	private int getUnusedMetricIndex(Experiment experiment) {
		int numMetrics = experiment.getMetricCount();
		int index = 0;
		var metrics = experiment.getMetricList();
		for(var metric: metrics) {
			if (metric.getIndex() != index)
				return index;
			index++;
		}
		return numMetrics;
	}
	

	@Test
	public void testMetricValueDisplay() {

		for(var experiment: experiments) {
			RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
			if (experiment.getMetricCount() == 0)
				continue;
			
			var metrics = experiment.getNonEmptyMetricIDs(root);
			assertNotNull(metrics);
			
			for(var m: metrics) {
				var metric = experiment.getMetric(m);
				var str = metric.getMetricTextValue(root);
				assertNotNull(str);
				assertTrue(str.length() > 1);
				
				// the percentage of the root should be 100%
				if (metric.getAnnotationType() == AnnotationType.PERCENT) {
					String percent = str.substring(str.length()-6, str.length());
					assertEquals("100.0%", percent);
				}
			}
		}
	}


	private static void testGetRootScope(Experiment experiment) {
		
		RootScope rootCCT = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		RootScope rootFlat = experiment.getRootScope(RootScopeType.Flat);
		
		assertNotNull(rootCCT);
		assertNotNull(rootCall);
		assertNotNull(rootFlat);
		
		assertNotSame(rootCCT, rootCall);
		assertNotSame(rootCall, rootFlat);
		
		rootCall = experiment.createCallersView(rootCCT, rootCall);			
		rootFlat = experiment.createFlatView(rootCCT, rootFlat);
		
		assertTrue(rootCCT.getSubscopeCount()  >= 0);
		assertTrue(rootCall.getSubscopeCount() >= 0);
		assertTrue(rootFlat.getSubscopeCount() >= 0);
	}


	@Test
	public void testTree() {
		for(var experiment: experiments) {
			for (var root: experiment.getRootScopeChildren()) {
				assertTrue(root instanceof RootScope);
				
				if (!root.hasChildren())
					continue;
				
				if (((RootScope)root).getType() == RootScopeType.CallerTree) {
					var result = testUniqueProcedure((RootScope) root);
					assertTrue(result);
				} else {					
					for(var child: root.getChildren()) {					
						boolean result = TestMetricValue.testMetricValueCorrectness(experiment, root, child);
						assertTrue( "Tree test fails for: " + experiment.getName() + " scope: " + child.getName(), result);
					}
				}
			}
		}
	}
	
	private boolean testUniqueProcedure(RootScope root) {
		var children = root.getChildren();
		var mapProcedure = new HashMap<String, Scope>(children.size());
		for(var child: children) {
			var name  = child.getName();
			var scope = mapProcedure.get(name);
			if (scope != null) 
				assertNotEquals(scope.getFlatIndex(), child.getFlatIndex());
			mapProcedure.put(name, child);
		}
		return true;
	}
		

	@Test
	public void testGetThreadData() throws IOException {
		for(var experiment: experiments) {
			var name = experiment.getName();
			if (name.contains("loop") || 
				name.contains(DB_MULTITHREAD) || 
				experiment.getMajorVersion() == Constants.EXPERIMENT_SPARSE_VERSION)
				assertNotNull(experiment.getThreadData());
		}
	}

	@Test
	public void testGetMajorVersion() {
		for(var experiment: experiments) {
			assertTrue(experiment.getMajorVersion() >= 0);
		}
	}

	@Test
	public void testGetMinorVersion() {
		for(var experiment: experiments) {
			assertTrue(experiment.getMinorVersion() >= 0);
		}
	}

	@Test
	public void testGetMaxDepth() {
		final int maxdepth[] = new int[] {4, 0, 0, 6, 10, 13, 20, 10, 10, 3};
		int i=0;
		for(var experiment: experiments) {
			assertTrue(experiment.getMaxDepth() >= maxdepth[i]);
			i++;
		}
	}

	@Test
	public void testGetScopeMap() {
		int i=0;
		for(var experiment: experiments) {
			if (i<3)
				assertNotNull(experiment.getScopeMap());
			i++;
		}
	}

	@Test
	public void testGetRootScopeChildren() {
		for(var experiment: experiments) {
			var children = experiment.getRootScopeChildren();
			assertNotNull(children);
			assertEquals(3, children.size());
		}
	}

	@Test
	public void testGetRootScopeRootScopeType() {
		for(var experiment: experiments) {
			RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
			assertNotNull(root);
		}
	}
	

	@Test
	public void testOpenFileIUserDataOfStringStringBoolean() {
		for(var experiment: experiments) {
			Experiment exp = new Experiment();
			try {
				exp.open(experiment.getDefaultDirectory(), null, true);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}
			assertEquals(exp.getName(), experiment.getName());
		}
	}

	@Test
	public void testGetName() {
		final String []names = new String[] {"bandwidthTest", "a.out", "a.out", "inline", "vectorAdd", "loop", "lmp", "inline", "vectorAdd", "a.out"};
		int i=0;
		for(var experiment: experiments) {
			String name = experiment.getName();
			assertEquals(name, names[i]);
			i++;
		}
	}

	@Test
	public void testGetConfiguration() {
		for(var experiment: experiments) {
			assertNotNull(experiment.getConfiguration());
		}
	}

	@Test
	public void testGetDefaultDirectory() {
		for(var experiment: experiments) {
			File dir = experiment.getDefaultDirectory();
			assertNotNull(dir);
		}
	}

	@Test
	public void testGetExperimentFile() {
		for(var experiment: experiments) {
			File file = experiment.getExperimentFile();
			assertNotNull(file);
			
			File dir = file.getParentFile();
			File dbPath = experiment.getDefaultDirectory();
			assertEquals(dir.getAbsolutePath(), dbPath.getAbsolutePath());
		}
	}

}
