package edu.rice.cs.hpctest.data;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
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
import edu.rice.cs.hpctest.util.TestDatabase;


public class ExperimentTest {
	private static final String DB_MULTITHREAD = "multithread";
	private static final String DB_LOOP_INLINE = "loop-inline";

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
		int []num = new int[] {97, 2, 0, 3, 3, 3, 6};
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
		final int []nmetrics = new int[] {18, 0, 0, 1, 1, 2, 2};
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
		int []counts = new int[] {10, 0, 0, 2, 2, 3, 3};
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
		int []order = new int[] {0, 0, 1, 1, 1, 1};
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
			
			assertTrue(experiment.getMetricCount() == numMetrics + 1);
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
					assertTrue(percent.equals("100.0%"));
				}
			}
		}
	}


	@Test
	public void testGetRootScope() {
		int i=0;
		final int []children   = new int[] {1, 0, 0, 1, 2, 1, 1};
		
		for(var experiment: experiments) {
			RootScope rootCCT = experiment.getRootScope(RootScopeType.CallingContextTree);
			RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
			RootScope rootFlat = experiment.getRootScope(RootScopeType.Flat);
			
			assertNotNull(rootCCT);
			assertNotNull(rootCall);
			assertNotNull(rootFlat);
			
			assertTrue(rootCCT != rootCall);
			assertTrue(rootCall != rootFlat);
			
			rootCall = experiment.createCallersView(rootCCT, rootCall);
			
			rootFlat = experiment.createFlatView(rootCCT, rootFlat);
			
			assertTrue(rootCCT.getSubscopeCount()  >= children[i]);
			assertTrue(rootCall.getSubscopeCount() >= children[i]);
			assertTrue(rootFlat.getSubscopeCount() >= children[i]);

			i++;
		}
	}


	@Test
	public void testTree() {
		for(var experiment: experiments) {
			for (var root: experiment.getRootScopeChildren()) {
				if (!root.hasChildren())
					continue;
				
				for(var child: root.getChildren()) {
					boolean result = testFlatContext(experiment, root, child);
					assertTrue( "Tree test fails for: " + experiment.getName() + " scope: " + child.getName(), result);
				}
			}
		}
	}
	
	private boolean testFlatContext(Experiment exp, Scope parent, Scope context) {
		var mvc = context.getMetricValues();
		if (mvc == null)
			return true;
		
		var mvcParent = parent.getMetricValues();
		var metrics = exp.getMetricList();
		
		for(var metric: metrics) {
			var partner = exp.getMetric(metric.getPartner());
			var mv1 = mvc.getValue(context, metric);
			var mv2 = mvc.getValue(context, partner);
			
			// test the exclusive vs inclusive
			if (metric.getMetricType() == MetricType.INCLUSIVE) {
				int c = floatCompare(mv1.getValue(), mv2.getValue());
				assertTrue(c>=0);
			} else {
				int c = floatCompare(mv1.getValue(), mv2.getValue());
				assertTrue(c<=0);
			}
			
			// test comparison with the parent
			var pv1 = mvcParent.getValue(parent, metric);
			
			assertTrue(checkChildValue(metric, pv1, mv1));
		}
		
		// traverse all the children
		if (context.hasChildren()) {
			for(var child: context.getChildren()) {
				var result = testFlatContext(exp, context, child);
				if (!result)
					return false;
			}
		}
		return true;
	}
	
	private static int floatCompare(float f1, float f2) {
		final float EPSILON = 0.000001f;
		final float delta = f1 - f2;
		final float diffEps = Math.abs(delta) / f1;
		if (diffEps < EPSILON)
			return 0;
		else return (int) delta;
	}
	
	
	private boolean checkChildValue(BaseMetric metric, MetricValue mv1, MetricValue mv2) {
		if (metric.getMetricType() == MetricType.INCLUSIVE) { 
			final int  c = floatCompare(mv1.getValue(), mv2.getValue());
			return c>=0;
		}
		// exclusive metric: anything can happen
		return true;
	}
	

	@Test
	public void testGetThreadData() throws IOException {
		for(var experiment: experiments) {
			var name = experiment.getName();
			if (name.contains("loop") || name.contains(DB_MULTITHREAD))
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
		final int maxdepth[] = new int[] {4, 0, 0, 6, 13, 20, 10};
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
			assertTrue(children.size() == 3);
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
			assertTrue(exp.getName().equals(experiment.getName()));
		}
	}

	@Test
	public void testGetName() {
		final String []names = new String[] {"bandwidthTest", "a.out", "a.out", DB_LOOP_INLINE, DB_MULTITHREAD, "qs", "inline"};
		int i=0;
		for(var experiment: experiments) {
			String name = experiment.getName();
			assertNotNull(name.equals(names[i]));
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
			assertTrue(dir.getAbsolutePath().equals(dbPath.getAbsolutePath()));
		}
	}

}
